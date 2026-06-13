# Reset Database and Storage for ATS Project
Write-Host "Stopping and removing containers with volumes..." -ForegroundColor Yellow
docker compose down -v

Write-Host "Starting system from scratch..." -ForegroundColor Green
docker compose up -d --build

Write-Host "System is starting. It may take a minute for the database to be ready and admin to be seeded." -ForegroundColor Cyan
Write-Host "You can follow logs with: docker compose logs -f backend" -ForegroundColor Gray
