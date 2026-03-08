#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Waking Docker (if needed)..."
if ! docker info >/dev/null 2>&1; then
  echo "Docker doesn't appear to be running. Please start Docker Desktop and re-run."
  exit 1
fi

echo "Starting database dependencies..."
docker compose up -d postgres mongodb valkey

echo "Building frontend..."
pushd "${ROOT_DIR}/frontend" >/dev/null
npm install
npm run build
popd >/dev/null

echo "Syncing frontend build into backend static assets..."
STATIC_DIR="${ROOT_DIR}/backend/src/main/resources/static"
rm -rf "${STATIC_DIR}"
mkdir -p "${STATIC_DIR}"
cp -R "${ROOT_DIR}/frontend/dist/." "${STATIC_DIR}/"

echo "Starting backend development server..."
pushd "${ROOT_DIR}/backend" >/dev/null
mvn spring-boot:run
