#!/bin/sh
cat > /usr/share/nginx/html/env.js <<EOF
window.__env = {
  clientId: "${AZURE_CLIENT_ID:-}",
  tenantId: "${AZURE_TENANT_ID:-}",
  redirectUri: "${AZURE_REDIRECT_URI:-http://localhost:3000}",
  apiScope: "${API_SCOPE:-}",
  apiBaseUrl: "${API_BASE_URL:-}"
};
EOF
