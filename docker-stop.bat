@echo off
REM ==============================================================================
REM AutoParts Store - Docker Stop (Windows)
REM ==============================================================================

echo.
echo Stopping all containers...
docker-compose down

echo.
echo All services stopped.
pause
