#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

IMAGE="gradle:8.7-jdk17"
CONTAINER_WORKDIR="/workspace"
HOST_PORT=${HOST_PORT:-8080}
CONTAINER_PORT=${CONTAINER_PORT:-8080}

PROJECT_NAME=${COMPOSE_PROJECT_NAME:-$(basename "$PROJECT_ROOT")}
NETWORK_NAME=$(printf '%s' "${PROJECT_NAME}_default" | tr '[:upper:]' '[:lower:]')
USE_COMPOSE_NETWORK=0
if docker network inspect "$NETWORK_NAME" >/dev/null 2>&1; then
  USE_COMPOSE_NETWORK=1
fi

if [ "$USE_COMPOSE_NETWORK" -eq 1 ]; then
  DEFAULT_DB_HOST=${COMPOSE_DB_HOST:-postgres}
else
  DEFAULT_DB_HOST=${COMPOSE_DB_HOST:-host.docker.internal}
fi

ENV_FILE="$PROJECT_ROOT/.env"
if [ -f "$ENV_FILE" ]; then
  echo "[run_backend] Loading environment variables from $ENV_FILE"
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

if ! docker info >/dev/null 2>&1; then
  echo "[run_backend] Docker daemon is not running. Please start Docker Desktop or your daemon." >&2
  exit 1
fi

CMD=(
  docker run --rm
  --name ai-car-sales-backend
  -p "${HOST_PORT}:${CONTAINER_PORT}"
  -v "${PROJECT_ROOT}:${CONTAINER_WORKDIR}"
  -w "${CONTAINER_WORKDIR}"
)

if [ "$USE_COMPOSE_NETWORK" -eq 1 ]; then
  CMD+=( --network "$NETWORK_NAME" )
fi

CMD+=(
  --env SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-local}
  --env DB_HOST=${DB_HOST:-$DEFAULT_DB_HOST}
  --env DB_PORT=${DB_PORT:-5432}
  --env DB_NAME=${DB_NAME:-app_db}
  --env DB_USER=${DB_USER:-app}
  --env DB_PASSWORD=${DB_PASSWORD:-app_password}
  --env GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID:-}
  --env GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET:-}
  ${IMAGE}
  gradle
  bootRun
)

printf '[run_backend] Executing: %s\n' "${CMD[*]}"
exec "${CMD[@]}"
