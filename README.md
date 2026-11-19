# DigitalFix

Plataforma cloud-native para gestionar órdenes de trabajo de mantención eléctrica.
La solución usa React, Azure AD/MSAL, microservicios Java con Spring Boot, PostgreSQL,
AWS API Gateway y Docker Compose sobre AWS EC2.

## Arquitectura actual

```text
React + MSAL
      │ JWT
      ▼
AWS API Gateway
      │
      ▼
Spring Boot BFF
      │
      ├── ms-digitalfix-workorders ──┐  eventos (RabbitMQ)
      ├── ms-digitalfix-catalog     │
      ├── ms-digitalfix-audit       ▼
      └── ms-digitalfix-report   TopicExchange: digitalfix.workorder.exchange
                                   ├── workorder.created.queue ──► notificaciones
                                   ├── workorder.status.queue  ──► notificaciones
                                   ├── workorder.completed.queue ► notificaciones
                                   └── workorder.ticket.queue   ──► generación de tickets
                                          (cada cola con su DLQ en digitalfix.workorder.dlx)
                │
                ▼
            PostgreSQL
```

La mensajería asíncrona con RabbitMQ desacopla las tareas de notificaciones y
generación de tickets de la lógica de órdenes: el productor publica eventos y los
consumidores procesan de forma independiente. Si un procesamiento falla tras los
reintentos, el mensaje se enruta a su DLQ sin afectar la operación principal.
Kafka queda preparado como módulo independiente para una incorporación posterior.

## Servicios

| Servicio | Puerto | Base de datos | Responsabilidad |
|---|---:|---|---|
| `digitalfix-react` | 3000 | — | SPA React servida por nginx |
| `ms-digitalfix-bff` | 8080 | — | BFF, seguridad JWT y proxy |
| `ms-digitalfix-workorders` | 8081 | PostgreSQL | CRUD de órdenes |
| `ms-digitalfix-catalog` | 8082 | PostgreSQL | Servicios, repuestos y stock |
| `ms-digitalfix-notify` | 8083 | — | Consumidor RabbitMQ: notificaciones y generación de tickets |
| `ms-digitalfix-rabbitadmin` | 8086 | — | Administrador RabbitMQ: colas, exchanges y bindings vía REST |
| `ms-digitalfix-kafkaadmin` | 8087 | — | Administrador Kafka: topics, configs y consumer groups vía REST |
| `ms-digitalfix-audit` | 8084 | PostgreSQL | Consumidor Kafka: auditoría (event sourcing) |
| `ms-digitalfix-report` | 8085 | PostgreSQL | Consumidor Kafka: reportes y KPIs |

## Tecnologías

- Frontend: React 18, MSAL React, MSAL Browser y axios.
- Backend: Java 21, Spring Boot 3.3, Spring Security y Spring Data JPA.
- Seguridad: OAuth 2.0 / OpenID Connect, Azure AD y JWT.
- API Gateway: AWS API Gateway con JWT Authorizer.
- Base de datos: PostgreSQL 16 mediante el driver oficial JDBC.
- Mensajería: RabbitMQ 3.13 (clúster de 2 nodos) con Spring AMQP.
- Despliegue: AWS EC2, Docker y Docker Compose.

## Mensajería asíncrona (RabbitMQ)

Productor: `ms-digitalfix-workorders` publica un `WorkOrderEvent` al topic exchange
`digitalfix.workorder.exchange` al crear una orden y al cambiar su estado (si la
orden pasa a `CERRADA` se publica `WORK_ORDER_CLOSED`). Consumidor:
`ms-digitalfix-notify` procesa las tareas asíncronas.

| Exchange (topic) | Routing key | Cola principal | Tarea asíncrona | DLQ |
|---|---|---|---|---|
| `digitalfix.workorder.exchange` | `workorder.created` | `workorder.created.queue` | Notificación email + push | `workorder.created.queue.dlq` |
| `digitalfix.workorder.exchange` | `workorder.status.#` | `workorder.status.queue` | Notificación email + push | `workorder.status.queue.dlq` |
| `digitalfix.workorder.exchange` | `workorder.completed` | `workorder.completed.queue` | Notificación email | `workorder.completed.queue.dlq` |
| `digitalfix.workorder.exchange` | `workorder.created` | `workorder.ticket.queue` | Generación de ticket | `workorder.ticket.queue.dlq` |

- Las 4 colas declaran `x-dead-letter-exchange = digitalfix.workorder.dlx` con su
  propio `x-dead-letter-routing-key`, y las DLQs se enlazan al DLX.
- La cola de tickets se enlaza a `workorder.created` (topic exchange): un mismo
  evento de creación alimenta notificaciones **y** generación de tickets sin tocar
  el productor.
- El consumidor usa `default-requeue-rejected=false` + reintentos (`max-attempts: 3`):
  si la tarea falla definitivamente, el mensaje queda en la DLQ.
- La publicación es *best-effort* (try/catch en `WorkOrderEventPublisher`): un fallo
  del broker nunca revierte ni afecta la lógica de órdenes.
- Topología completa en `ms-digitalfix-notify/src/main/java/com/digitalfix/notify/config/RabbitMQConfig.java`.

### Demo de la mensajería

1. Levantar RabbitMQ y observar la topología en la UI (`digitalfix` / `digitalfix`):

```powershell
docker network create digitalfix-net
docker compose -f infrastructure\compose.rabbitmq.yml --env-file infrastructure\.env up -d
```

2. Con el stack arriba, crear una orden desde el frontend (o `POST /api/workorders` vía BFF).
3. Ver los mensajes procesados en los logs de `ms-digitalfix-notify`
   (`docker logs -f <contenedor-notify>`): `[EMAIL]`, `[PUSH]` y `[TICKET]`.
4. En la UI de RabbitMQ (http://localhost:15672) verificar las 4 colas y sus DLQs
   (mensajes muertos visibles si se fuerza un error).

### Política de confirmación de mensajes (ACK/NACK)

Los consumidores operan en `acknowledge-mode: manual` con una política explícita y
documentada en cada listener:

- Éxito → `channel.basicAck(tag, false)`.
- Error transitorio (`RecoverableMessagingException`, p. ej. email del cliente aún
  no disponible) → se relanza: el contenedor reintenta 3 veces con backoff y, si
  persiste, hace NACK sin requeue → el mensaje va a la DLQ.
- Mensaje inválido (`InvalidMessageException`, p. ej. evento sin `eventId`) →
  `channel.basicNack(tag, false, false)` directo a la DLQ, sin reintentos.
- Todos los caminos quedan registrados en logs (`INFO`/`WARN`/`ERROR`).

### Administración de RabbitMQ (ms-digitalfix-rabbitadmin)

Microservicio con la lógica encapsulada en `RabbitAdminService` (los controladores no
conocen detalles del broker) y endpoints REST con validación de parámetros
(nombres obligatorios y con formato válido, tipos de exchange y bindings restringidos,
nombres reservados `amq.*`/`default` rechazados). Protegido con JWT y rol `Admin`:

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/rabbit/queues` | Crea una cola (opcional `deadLetterExchange`/`deadLetterRoutingKey`); 409 si ya existe |
| GET | `/api/rabbit/queues/{name}` | Información de la cola (mensajes/consumidores); 404 si no existe |
| DELETE | `/api/rabbit/queues/{name}` | Elimina la cola; 404 si no existe |
| POST | `/api/rabbit/exchanges` | Crea un exchange (`direct`, `topic`, `fanout`, `headers`) |
| DELETE | `/api/rabbit/exchanges/{name}` | Elimina el exchange |
| POST | `/api/rabbit/bindings` | Crea un binding (exchange → cola o exchange, con routing key) |
| DELETE | `/api/rabbit/bindings?exchange=&destination=&destinationType=&routingKey=` | Elimina el binding |

Errores claros con códigos HTTP: `400` (validación), `404` (recurso inexistente),
`409` (conflicto).

## Mensajería asíncrona (Kafka)

Kafka es un **log**, no una cola: los mensajes persisten hasta su retención, los
lee quien los lea, y cada *consumer group* lleva su propio *offset*. Por eso se usa
para **auditoría (event sourcing)** y **analítica (KPIs)**, donde importa poder
**reprocesar el historial** (replay) y que varios sistemas lean el mismo flujo sin
afectarse. (Detalle conceptual en la guía del curso: Kafka 4.x con KRaft.)

| Topic | Particiones | Productor | Consumidor (group-id) |
|---|---|---|---|
| `workorder.audit` | 3 | `ms-digitalfix-workorders` | `ms-digitalfix-audit` |
| `workorder.kpi` | 3 | `ms-digitalfix-workorders` | `ms-digitalfix-report` |

- **Productor** (`WorkOrderEventPublisher`): publica cada `WorkOrderEvent` a Kafka
  (además de RabbitMQ). La *key* es el `eventId`, de modo que los eventos de una
  misma orden conservan orden dentro de una partición.
- **Consumidores**: `@KafkaListener` en `ms-digitalfix-audit` (topic `workorder.audit`)
  y `ms-digitalfix-report` (topic `workorder.kpi`).
- **DLQ**: Kafka no tiene DLQ nativa; Spring la simula. Con `DefaultErrorHandler` +
  `DeadLetterPublishingRecoverer`, los mensajes fallidos (tras 3 reintentos) se
  publican a `workorder.audit.DLT` / `workorder.kpi.DLT`.
- **Broker**: Kafka 4.x con **KRaft** (sin ZooKeeper), imagen `apache/kafka:4.0.0`
  (`infrastructure/compose.kafka.yml`). Kafka UI (Kafbat) en http://localhost:8090.

### Demo de Kafka

1. Levantar el broker: `docker compose -f infrastructure\compose.kafka.yml up -d`
   y la UI: `docker compose -f infrastructure\compose.kafka-ui.yml up -d`.
2. Crear una orden → ver en logs de audit/report el consumo (`Evento de auditoría/KPI recibido`).
3. Replay: con la CLI del contenedor, `kafka-console-consumer --from-beginning` muestra
   el historial completo sin borrarlo (la diferencia clave frente a RabbitMQ).
4. En Kafka UI (http://localhost:8090) ver los topics, particiones y el *lag* de cada grupo.

### Configuración avanzada de topics

| Topic | Particiones | Réplicas | `retention.ms` | `cleanup.policy` | Justificación |
|---|---|---|---|---|---|
| `workorder.audit` | 3 | 1 | 7 días (604800000) | `delete` | Auditoría/event sourcing: el historial debe poder reprocesarse (replay) |
| `workorder.kpi` | 3 | 1 | 1 día (86400000) | `delete` | Analítica: solo interesa el flujo reciente |

Declaradas programáticamente en `KafkaConfig` (workorders) con `TopicBuilder.config(...)`.

### Consumer groups estratégicos

| Group | Topic | Propósito | Resiliencia |
|---|---|---|---|
| `ms-digitalfix-audit` | `workorder.audit` | Timeline de auditoría (event sourcing) | Reintentos (3, backoff) → `workorder.audit.DLT` |
| `ms-digitalfix-report` | `workorder.kpi` | KPIs y analítica | Reintentos (3, backoff) → `workorder.kpi.DLT` |

Grupos independientes = ambos leen el mismo flujo sin afectarse (offset propio). Las
3 particiones permiten escalar hasta 3 consumidores por grupo (una partición por consumidor).

### Manejo de errores (mensajes defectuosos)

Los listeners **validan el payload** (`eventId`, `workOrderId`) antes de procesar y
registran **logging contextual** (partición y offset). Un evento defectuoso lanza
`InvalidEventException`; el `DefaultErrorHandler` lo reintenta 3 veces y, si persiste,
lo publica al topic `.DLT` — **sin interrumpir el consumo** del grupo.

### Administración de Kafka (ms-digitalfix-kafkaadmin)

Microservicio (puerto 8087) con la lógica encapsulada en `KafkaAdminService` sobre
`AdminClient`. Protegido con JWT y rol `Admin`; validación de parámetros (nombres con
formato válido, particiones/réplicas ≥ 1, `cleanup.policy` ∈ {delete, compact},
nombres reservados rechazados) y errores HTTP claros (400/404/409/503):

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/kafka/topics` | Lista los topics |
| POST | `/api/kafka/topics` | Crea un topic (particiones, réplicas, `retention.ms`, `cleanup.policy`); 409 si existe |
| GET | `/api/kafka/topics/{name}` | Inspecciona un topic (particiones, réplicas, configs); 404 si no existe |
| PATCH | `/api/kafka/topics/{name}` | Actualiza configs y/o aumenta particiones |
| DELETE | `/api/kafka/topics/{name}` | Elimina el topic; 404 si no existe |
| GET | `/api/kafka/consumer-groups` | Lista los consumer groups |
| GET | `/api/kafka/consumer-groups/{groupId}` | Inspecciona un grupo (estado y miembros) |
| DELETE | `/api/kafka/consumer-groups/{groupId}` | Elimina el grupo; 404 si no existe |

### Nota de diseño: KRaft (sin ZooKeeper)

La imagen oficial `apache/kafka:4.0.0` usa **KRaft**: Kafka 4.x **eliminó ZooKeeper**
(ya deprecado desde 3.5) y el consenso de metadatos lo gestionan los controllers Raft
internamente. Los `docker-compose` con `zookeeper` + `kafka` son material obsoleto;
en Kafka 4.x **no es posible** usar ZooKeeper.

## Ejecución local

Requisitos: Docker, Docker Compose, Java 21, Node.js 20 y credenciales de Azure AD.

```powershell
Copy-Item infrastructure\.env.example infrastructure\.env
docker network create digitalfix-net
docker compose -f infrastructure\compose.postgres.yml --env-file infrastructure\.env up -d
docker compose -f infrastructure\compose.rabbitmq.yml --env-file infrastructure\.env up -d
docker compose -f infrastructure\compose.kafka.yml --env-file infrastructure\.env up -d
docker compose -f infrastructure\compose.apps.yml --env-file infrastructure\.env up -d --build
```

Para levantar solo un servicio backend:

```powershell
Set-Location backend\ms-digitalfix-workorders
.\mvnw.cmd -q -B verify
```

El frontend se ejecuta con:

```powershell
Set-Location frontend\digitalfix-react
npm ci
npm start
```

## Variables de entorno

Las variables mínimas están en [`infrastructure/.env.example`](infrastructure/.env.example):

- `AZURE_TENANT_ID`
- `AZURE_API_CLIENT_ID`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `RABBITMQ_USER`
- `RABBITMQ_PASSWORD`
- `RABBITMQ_ERLANG_COOKIE`

La primera inicialización de PostgreSQL crea `workorders_db`, `catalog_db`,
`audit_db` y `report_db` mediante `infrastructure/init-dbs.sh`. Si ya existe un
volumen PostgreSQL creado con una configuración anterior, elimínalo solo en
entornos locales para ejecutar nuevamente la inicialización:

```powershell
docker compose -f infrastructure\compose.postgres.yml down -v
```

Las variables de RabbitMQ se listan más arriba. Kafka no se utiliza en esta etapa.

## Estructura

```text
frontend/digitalfix-react/
backend/ms-digitalfix-bff/
backend/ms-digitalfix-workorders/
backend/ms-digitalfix-catalog/
backend/ms-digitalfix-notify/
backend/ms-digitalfix-audit/
backend/ms-digitalfix-report/
infrastructure/
```

## Despliegue AWS

```powershell
.\infrastructure\aws\deploy-aws.ps1 `
  -TenantId "<TENANT_ID>" `
  -ApiClientId "<API_CLIENT_ID>"
```

El script usa `t3.medium` por defecto y despliega PostgreSQL, el clúster de
RabbitMQ (2 nodos) y las aplicaciones del proyecto. Los servicios tienen límites
de memoria Docker y las JVM usan heaps acotados para evitar que una instancia
pequeña se quede sin RAM. La suma de los límites de los contenedores de las
aplicaciones es aproximadamente 2.1 GB, más 512 MB para los dos nodos RabbitMQ;
se configura además un swap de 2 GB como respaldo para la compilación inicial.
La UI de RabbitMQ queda expuesta en el puerto 15672.

La construcción de imágenes se ejecuta de forma secuencial en EC2 para evitar que
varios procesos Maven consuman la memoria simultáneamente. Para revisar el uso
después del despliegue:

```bash
free -h
docker stats --no-stream
```
