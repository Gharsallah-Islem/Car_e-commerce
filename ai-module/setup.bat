@echo off
echo ============================================================
echo AI Visual Search Module - Setup Script
echo ============================================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    echo Please install Python 3.12+ from https://www.python.org/
    pause
    exit /b 1
)

echo [1/6] Python detected
python --version
echo.

REM Check if CUDA is installed
nvcc --version >nul 2>&1
if errorlevel 1 (
    echo WARNING: CUDA not detected
    echo GPU training will not be available
    echo Install CUDA 12.2 from: https://developer.nvidia.com/cuda-downloads
    echo.
) else (
    echo [2/6] CUDA detected
    nvcc --version
    echo.
)

REM Create virtual environment
echo [3/6] Creating virtual environment...
if exist venv (
    echo Virtual environment already exists, skipping...
) else (
    python -m venv venv
    if errorlevel 1 (
        echo ERROR: Failed to create virtual environment
        pause
        exit /b 1
    )
    echo Virtual environment created successfully
)
echo.

REM Activate virtual environment
echo [4/6] Activating virtual environment...
call venv\Scripts\activate.bat
if errorlevel 1 (
    echo ERROR: Failed to activate virtual environment
    pause
    exit /b 1
)
echo.

REM Upgrade pip
echo [5/6] Upgrading pip...
python -m pip install --upgrade pip
echo.

REM Install dependencies
echo [6/6] Installing dependencies...
echo This may take 5-10 minutes...
pip install -r requirements.txt
if errorlevel 1 (
    echo ERROR: Failed to install dependencies
    pause
    exit /b 1
)
echo.

echo ============================================================
echo Setup completed successfully!
echo ============================================================
echo.
echo Next steps:
echo 1. Copy .env.example to .env and configure settings
echo 2. Run: python gpu_test.py (to verify GPU setup)
echo 3. Run: python src/api/main.py (to start API server)
echo 4. Access data collection at: http://localhost:5000/admin/data-collection
echo.
echo ============================================================
pause
