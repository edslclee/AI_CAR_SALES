#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CSV_PATH="${CSV_PATH:-$PROJECT_ROOT/data/samples/cars_valid.csv}"

DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-app_db}
DB_USER=${DB_USER:-app}

if [[ ! -f "$CSV_PATH" ]]; then
  echo "[seed_sample_cars] CSV not found: $CSV_PATH" >&2
  exit 1
fi

if ! command -v psql >/dev/null 2>&1; then
  echo "[seed_sample_cars] psql command not found. Install PostgreSQL client." >&2
  exit 1
fi

read -r -s -p "DB password for user ${DB_USER}: " DB_PASSWORD
echo

PGPASSWORD="$DB_PASSWORD" psql \
  --host "$DB_HOST" \
  --port "$DB_PORT" \
  --username "$DB_USER" \
  --dbname "$DB_NAME" \
  --set=ON_ERROR_STOP=on <<PSQL
\set ON_ERROR_STOP on
\echo '[seed_sample_cars] Importing from $CSV_PATH'
CREATE TEMP TABLE tmp_cars (
  oem_code text,
  model_name text,
  trim text,
  price text,
  body_type text,
  fuel_type text,
  efficiency text,
  seats text,
  drivetrain text,
  release_year text,
  features text,
  media_assets text
);

\copy tmp_cars FROM '$CSV_PATH' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8');

INSERT INTO cars (oem_code, model_name, trim, price, body_type, fuel_type, efficiency, seats, drivetrain, release_year, features, media_assets)
SELECT oem_code,
       model_name,
       NULLIF(trim, '') AS trim,
       NULLIF(price, '')::integer,
       body_type,
       fuel_type,
       NULLIF(efficiency, '')::numeric,
       NULLIF(seats, '')::smallint,
       NULLIF(drivetrain, '') AS drivetrain,
       CASE WHEN release_year ~ '^[0-9]+$' THEN release_year::smallint ELSE NULL END,
       CASE WHEN features IS NULL OR features = '' THEN NULL ELSE features::jsonb END,
       CASE WHEN media_assets IS NULL OR media_assets = '' THEN NULL ELSE media_assets::jsonb END
FROM tmp_cars
ON CONFLICT (oem_code, model_name, trim)
DO UPDATE SET price = EXCLUDED.price,
              body_type = EXCLUDED.body_type,
              fuel_type = EXCLUDED.fuel_type,
              efficiency = EXCLUDED.efficiency,
              seats = EXCLUDED.seats,
              drivetrain = EXCLUDED.drivetrain,
              release_year = EXCLUDED.release_year,
              features = EXCLUDED.features,
              media_assets = EXCLUDED.media_assets;
PSQL

echo "[seed_sample_cars] Imported data from $CSV_PATH"
