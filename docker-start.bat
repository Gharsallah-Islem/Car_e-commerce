@echo off
REM ==============================================================================
REM AutoParts Store - Docker Quick Start (Windows)
REM ==============================================================================
REM Run this script to start the entire application stack

echo.
echo ============================================
echo AutoParts Store - Docker Quick Start
echo ============================================
echo.

REM Check if Docker is running
docker info > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

echo [1/4] Checking Docker...
docker --version

echo.
echo [2/4] Building images...
docker-compose build

echo.
echo [3/4] Starting services...
docker-compose up -d

echo.
echo [4/4] Waiting for services to be healthy...
timeout /t 30 /nobreak > nul

echo.
echo ============================================
echo Services Started!
echo ============================================
echo.
echo Frontend:   http://localhost:4200
echo Backend:    http://localhost:8080
echo AI Module:  http://localhost:5000
echo Database:   localhost:5432
echo.
echo View logs:  docker-compose logs -f
echo Stop:       docker-compose down
echo.
echo ============================================

pause
