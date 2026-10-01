#!/usr/bin/env bash
set -euo pipefail

for database in workorders_db catalog_db audit_db report_db; do
  exists="$(psql --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" --tuples-only --no-align \
    --command "SELECT 1 FROM pg_database WHERE datname = '$database'")"

  if [[ "$exists" != "1" ]]; then
    createdb --username "$POSTGRES_USER" --owner "$POSTGRES_USER" "$database"
  fi
done
