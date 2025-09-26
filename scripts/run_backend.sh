#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

IMAGE="gradle:8.7-jdk17"
CONTAINER_WORKDIR="/workspace"
HOST_PORT=${HOST_PORT:-8080}
CONTAINER_PORT=${CONTAINER_PORT:-8080}

if ! docker info >/dev/null 2>&1; then
  echo "[run_backend] Docker daemon is not running. Please start Docker Desktop or your daemon." >&2
  exit 1
fi

CMD=(
  docker run --rm \
    --name ai-car-sales-backend \
    -p "${HOST_PORT}:${CONTAINER_PORT}" \
    -v "${PROJECT_ROOT}:${CONTAINER_WORKDIR}" \
    -w "${CONTAINER_WORKDIR}" \
    --env SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-local} \
    --env DB_HOST=${DB_HOST:-host.docker.internal} \
    --env DB_PORT=${DB_PORT:-5432} \
    --env DB_NAME=${DB_NAME:-app_db} \
    --env DB_USER=${DB_USER:-app} \
    --env DB_PASSWORD=${DB_PASSWORD:-app_password} \
    ${IMAGE} \
    gradle bootRun
)

printf '[run_backend] Executing: %s\n' "${CMD[*]}"
exec "${CMD[@]}"
