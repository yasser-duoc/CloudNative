# Configuración de Azure AD (Entra ID) — Pedidos360

Guía paso a paso para dejar funcionando el flujo de autenticación/autorización
(login MSAL → JWT → BFF). El código ya está listo; **solo falta esta configuración manual
en el portal de Azure**, que cada miembro debe hacer con una cuenta con permisos de
*Application Administrator* (o dueño del tenant).

> Índice de valores que se obtienen aquí y dónde van:
> | Valor | Variable (frontend) | Variable (backend/infra) |
> |---|---|---|
> | Tenant ID | `REACT_APP_AZURE_TENANT_ID` | `AZURE_TENANT_ID` |
> | Client ID de la SPA | `REACT_APP_AZURE_CLIENT_ID` | — |
> | Client ID de la API | `REACT_APP_API_SCOPE` (dentro de `api://...`) | `AZURE_API_CLIENT_ID` |

---

## 1. Obtener el Tenant ID

1. Entra a [Azure Portal](https://portal.azure.com) → **Microsoft Entra ID** (Azure Active Directory).
2. En **Overview** copia el **Tenant ID** (GUID).
   - Va en `REACT_APP_AZURE_TENANT_ID` y `AZURE_TENANT_ID`.

---

## 2. App Registration de la SPA (frontend React)

1. En **Microsoft Entra ID → App registrations → New registration**.
2. Nombre: `pedidos360-spa`.
3. *Supported account types*: **Accounts in this organizational directory only (Single tenant)**.
4. *Redirect URI* → platform **Single-page application (SPA)** → URI `http://localhost:3000`.
5. **Register**.
6. Copia el **Application (client) ID**.
   - Va en `REACT_APP_AZURE_CLIENT_ID`.

> Si levantas el frontend en otro puerto, agrega ese redirect URI (ej. `http://localhost:3000`).

---

## 3. App Registration de la API (backend)

1. **App registrations → New registration**.
2. Nombre: `pedidos360-api`.
3. *Supported account types*: **Single tenant**.
4. **Register**.
5. Copia el **Application (client) ID**.
   - Va en `AZURE_API_CLIENT_ID` (infra) y dentro del scope del frontend.

---

## 4. Exponer el scope (Expose an API)

Este scope es el `aud` que valida el BFF (`api://<API_CLIENT_ID>`) y el scope que pide el frontend.

1. Abre `pedidos360-api` → **Expose an API**.
2. **Add a scope**:
   - *Scope name*: `access_as_user`
   - *Who can consent*: **Admins and users**
   - *Admin consent display name / description*: `Access Pedidos360 as the user` / descripción libre.
3. El valor final queda: **`api://<API_CLIENT_ID>/access_as_user`**.
   - Va en `REACT_APP_API_SCOPE`.

---

## 5. Definir los App Roles

Estos roles son el claim `roles` del JWT, que mapea el BFF a `ROLE_*` (Admin, Supervisor, Cliente, Auditor).

1. Abre `pedidos360-api` → **App roles → Create app role**.
2. Crea los **4 roles** con `Allowed member types = Users/Groups`:

   | Display name | Value | Description |
   |---|---|---|
   | Admin | `Admin` | Administrador del sistema |
   | Supervisor | `Supervisor` | Supervisor de operaciones |
   | Cliente | `Cliente` | Cliente final |
   | Auditor | `Auditor` | Auditor de seguridad |

3. Guarda cada uno (**Apply**).

---

## 6. Asignar roles a usuarios/grupos

1. **Microsoft Entra ID → Enterprise applications** → busca `pedidos360-api`.
2. **Users and groups → Add user/group**.
3. Selecciona el usuario o grupo y **elige el rol** correspondiente.
4. **Assign**.

> Sin esta asignación el token **no incluirá el claim `roles`** y el BFF responderá `403`.

---

## 7. Rellenar las variables de entorno

### Frontend (`frontend/pedidos360-react/.env`)

```bash
cp frontend/pedidos360-react/.env.example frontend/pedidos360-react/.env
```

```bash
REACT_APP_AZURE_CLIENT_ID=<Client ID de pedidos360-spa>
REACT_APP_AZURE_TENANT_ID=<Tenant ID>
REACT_APP_AZURE_REDIRECT_URI=http://localhost:3000
REACT_APP_API_SCOPE=api://<Client ID de pedidos360-api>/access_as_user
REACT_APP_API_BASE_URL=http://localhost:8080
```

### Infraestructura (`infrastructure/.env`)

```bash
cp infrastructure/.env.example infrastructure/.env
```

```bash
AZURE_TENANT_ID=<Tenant ID>
AZURE_API_CLIENT_ID=<Client ID de pedidos360-api>
```

---

## 8. Verificación del flujo

1. Levanta BFF + frontend (`npm start`).
2. Clic en **"Iniciar sesión"** → debe aparecer el pop-up de Microsoft.
3. Tras loguear, en DevTools → **Network** copia el `Authorization: Bearer <access_token>`.
4. Pégalo en [jwt.io](https://jwt.io) y verifica los claims:
   - `iss` = `https://login.microsoftonline.com/<TENANT_ID>/v2.0`
   - `aud` = `api://<API_CLIENT_ID>`
   - `roles` = `["Admin"]` (o el rol asignado)
   - `scp` = `access_as_user` (o el scope correspondiente)

> Captura de pantalla de estos claims en jwt.io = evidencia para la defensa (rúbrica de seguridad).
