#!/bin/bash
# Reset Database and Storage for ATS Project
echo "Stopping and removing containers with volumes..."
docker compose down -v

echo "Starting system from scratch..."
docker compose up -d --build

echo "System is starting. Follow logs with: docker compose logs -f backend"
