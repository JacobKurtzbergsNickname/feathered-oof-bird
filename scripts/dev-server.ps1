Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$rootDir = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

Write-Host "Waking Docker (if needed)..."
try {
  docker info | Out-Null
} catch {
  Write-Host "Docker doesn't appear to be running. Please start Docker Desktop and re-run."
  exit 1
}

Write-Host "Starting database dependencies..."
docker compose up -d postgres mongodb valkey

Write-Host "Building frontend..."
Push-Location (Join-Path $rootDir "frontend")
npm install
npm run build
Pop-Location

Write-Host "Syncing frontend build into backend static assets..."
$staticDir = Join-Path $rootDir "backend/src/main/resources/static"
if (Test-Path $staticDir) {
  Remove-Item -Recurse -Force $staticDir
}
New-Item -ItemType Directory -Force -Path $staticDir | Out-Null
Copy-Item -Recurse -Force (Join-Path $rootDir "frontend/dist/*") $staticDir

Write-Host "Starting backend development server..."
Push-Location (Join-Path $rootDir "backend")
mvn spring-boot:run
