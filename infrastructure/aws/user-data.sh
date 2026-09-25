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

# Swap de respaldo (4GB) para absorber picos de memoria del stack
fallocate -l 4G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab

cd /opt
rm -rf CloudNative
git clone https://github.com/yasser-duoc/CloudNative.git
cd CloudNative

cat > infrastructure/.env <<EOF
AZURE_TENANT_ID=${AZURE_TENANT_ID}
AZURE_API_CLIENT_ID=${AZURE_API_CLIENT_ID}
ORACLE_USERNAME=digitalfix
ORACLE_PASSWORD=DigitalFix2026!
EOF

docker network create digitalfix-net || true
docker compose -f infrastructure/compose.oracle.yml   --env-file infrastructure/.env up -d
sleep 30
docker compose -f infrastructure/compose.apps.yml      --env-file infrastructure/.env up -d --build

echo "DigitalFix stack desplegado" > /var/log/digitalfix-deploy.done
