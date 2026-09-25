# DigitalFix

Plataforma cloud-native para gestionar órdenes de trabajo de mantención eléctrica.
La solución usa React, Azure AD/MSAL, microservicios Java con Spring Boot, Oracle,
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
      ├── ms-digitalfix-workorders
      ├── ms-digitalfix-catalog
      ├── ms-digitalfix-audit
      └── ms-digitalfix-report
               │
               ▼
             Oracle
```

RabbitMQ y Kafka no forman parte de esta etapa. Las notificaciones, auditoría
basada en eventos y reportería por streaming quedan preparadas como módulos
independientes para una incorporación posterior.

## Servicios

| Servicio | Puerto | Base de datos | Responsabilidad |
|---|---:|---|---|
| `digitalfix-react` | 3000 | — | SPA React servida por nginx |
| `ms-digitalfix-bff` | 8080 | — | BFF, seguridad JWT y proxy |
| `ms-digitalfix-workorders` | 8081 | Oracle | CRUD de órdenes |
| `ms-digitalfix-catalog` | 8082 | Oracle | Servicios, repuestos y stock |
| `ms-digitalfix-notify` | 8083 | — | Módulo de notificaciones, sin broker por ahora |
| `ms-digitalfix-audit` | 8084 | Oracle | Consulta de auditoría |
| `ms-digitalfix-report` | 8085 | Oracle | Consulta de reportes y KPIs |

## Tecnologías

- Frontend: React 18, MSAL React, MSAL Browser y axios.
- Backend: Java 21, Spring Boot 3.3, Spring Security y Spring Data JPA.
- Seguridad: OAuth 2.0 / OpenID Connect, Azure AD y JWT.
- API Gateway: AWS API Gateway con JWT Authorizer.
- Base de datos: Oracle Free 23c mediante `ojdbc11`.
- Despliegue: AWS EC2, Docker y Docker Compose.

## Ejecución local

Requisitos: Docker, Docker Compose, Java 21, Node.js 20 y credenciales de Azure AD.

```powershell
Copy-Item infrastructure\.env.example infrastructure\.env
docker network create digitalfix-net
docker compose -f infrastructure\compose.oracle.yml --env-file infrastructure\.env up -d
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
- `ORACLE_USERNAME`
- `ORACLE_PASSWORD`

No se requieren variables de RabbitMQ ni Kafka.

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

El script despliega Oracle y las aplicaciones del proyecto. No crea instancias
ni abre puertos para RabbitMQ, Kafka o Zookeeper.
