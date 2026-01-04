@echo off
REM Quick setup script that works with PowerShell
echo ============================================================
echo AI Module - Quick Setup
echo ============================================================
echo.

REM Check Python
python --version
if errorlevel 1 (
    echo ERROR: Python not found
    pause
    exit /b 1
)

REM Create virtual environment if it doesn't exist
if not exist venv (
    echo Creating virtual environment...
    python -m venv venv
)

REM Activate and install
echo.
echo Activating virtual environment and installing dependencies...
echo This will take 5-10 minutes...
echo.

call venv\Scripts\activate.bat && pip install --upgrade pip && pip install -r requirements.txt

if errorlevel 1 (
    echo.
    echo ERROR: Installation failed
    echo.
    echo Try running these commands manually:
    echo   python -m venv venv
    echo   venv\Scripts\activate
    echo   pip install -r requirements.txt
    pause
    exit /b 1
)

echo.
echo ============================================================
echo Setup Complete!
echo ============================================================
echo.
echo Next steps:
echo 1. Activate virtual environment: venv\Scripts\activate
echo 2. Download dataset: python scripts\download_dataset.py
echo 3. Test GPU: python gpu_test.py
echo 4. Start server: python src\api\main.py
echo.
pause
