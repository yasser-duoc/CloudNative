#!/bin/bash
# Cloud-init para la instancia EC2 de DigitalFix (Ubuntu 22.04).
# Instala Docker, clona el repo, crea el .env y levanta las pilas de compose.
# Los valores AZURE_TENANT_ID / AZURE_API_CLIENT_ID se inyectan al lanzar la instancia.

set -euo pipefail

export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y ca-certificates curl git
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" \
  > /etc/apt/sources.list.d/docker.list
apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Swap de respaldo para absorber picos de memoria durante la compilación.
# Los límites de Docker y de la JVM controlan el consumo normal de RAM.
if [ ! -f /swapfile ]; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

cd /opt
rm -rf CloudNative
git clone https://github.com/yasser-duoc/CloudNative.git
cd CloudNative

cat > infrastructure/.env <<EOF
AZURE_TENANT_ID=${AZURE_TENANT_ID}
AZURE_API_CLIENT_ID=${AZURE_API_CLIENT_ID}
POSTGRES_USER=digitalfix
POSTGRES_PASSWORD=DigitalFix2026!
EOF

docker network create digitalfix-net || true
docker compose -f infrastructure/compose.postgres.yml --env-file infrastructure/.env up -d
until docker inspect --format='{{.State.Health.Status}}' digitalfix-postgres 2>/dev/null | grep -q healthy; do
  sleep 5
done

# Construir un servicio a la vez evita que Maven consuma toda la RAM disponible.
export COMPOSE_PARALLEL_LIMIT=1
docker compose -f infrastructure/compose.apps.yml --env-file infrastructure/.env build
docker compose -f infrastructure/compose.apps.yml --env-file infrastructure/.env up -d

echo "DigitalFix stack desplegado" > /var/log/digitalfix-deploy.done
