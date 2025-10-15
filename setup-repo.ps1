# Git Repository Initialization Script for Car E-Commerce
# Simple version without special characters

Write-Host "============================================" -ForegroundColor Green
Write-Host "Car E-Commerce Repository Initialization" -ForegroundColor Green  
Write-Host "============================================" -ForegroundColor Green
Write-Host ""

# Initialize Git if not already done
if (-not (Test-Path -Path ".git")) {
    Write-Host "[1/8] Initializing Git repository..." -ForegroundColor Yellow
    git init
    Write-Host "      Done!" -ForegroundColor Green
} else {
    Write-Host "[1/8] Git repository already initialized" -ForegroundColor Cyan
}

# Create folders
Write-Host ""
Write-Host "[2/8] Creating project folders..." -ForegroundColor Yellow

$folders = @(
    "frontend-web",
    "mobile-app",
    "ai-module",
    "docs/getting-started",
    "docs/backend",
    "docs/frontend",
    "docs/mobile",
    "docs/ai",
    "docs/deployment",
    "docs/api",
    "docs/troubleshooting",
    "docker/postgres",
    "docker/nginx",
    "scripts/setup",
    "scripts/build",
    "scripts/deploy",
    "scripts/utils"
)

foreach ($folder in $folders) {
    if (-not (Test-Path -Path $folder)) {
        New-Item -ItemType Directory -Path $folder -Force | Out-Null
        New-Item -ItemType File -Path "$folder/.gitkeep" -Force | Out-Null
    }
}
Write-Host "      Done!" -ForegroundColor Green

# Rename Backend folder
if (Test-Path -Path "Backend") {
    Write-Host ""
    Write-Host "[3/8] Renaming 'Backend' to 'backend'..." -ForegroundColor Yellow
    if (-not (Test-Path -Path "backend")) {
        Rename-Item -Path "Backend" -NewName "backend"
        Write-Host "      Done!" -ForegroundColor Green
    } else {
        Write-Host "      'backend' already exists, skipping" -ForegroundColor Yellow
    }
} else {
    Write-Host ""
    Write-Host "[3/8] Backend folder check - OK" -ForegroundColor Cyan
}

# Add files to Git
Write-Host ""
Write-Host "[4/8] Adding files to Git..." -ForegroundColor Yellow
git add .
Write-Host "      Done!" -ForegroundColor Green

# Create commit
Write-Host ""
Write-Host "[5/8] Creating initial commit..." -ForegroundColor Yellow
$commitMsg = @"
chore: initial project structure setup

- Added comprehensive project documentation
- Created folder structure for all modules (backend, frontend-web, mobile-app, ai-module)
- Configured Spring Boot backend with PostgreSQL
- Added GitHub templates (PR, Issues)
- Configured Docker Compose
- Added .gitignore for all modules
- Created CONTRIBUTING.md guidelines
"@

git commit -m $commitMsg
if ($LASTEXITCODE -eq 0) {
    Write-Host "      Done!" -ForegroundColor Green
} else {
    Write-Host "      No changes to commit or commit failed" -ForegroundColor Yellow
}

# Rename branch
Write-Host ""
Write-Host "[6/8] Setting main branch..." -ForegroundColor Yellow
git branch -M main
Write-Host "      Done!" -ForegroundColor Green

# Add remote
Write-Host ""
Write-Host "[7/8] Configuring remote repository..." -ForegroundColor Yellow
$remoteUrl = "https://github.com/Gharsallah-Islem/Car_e-commerce.git"

$remoteExists = git remote | Select-String -Pattern "origin"
if ($remoteExists) {
    git remote set-url origin $remoteUrl
    Write-Host "      Remote URL updated" -ForegroundColor Green
} else {
    git remote add origin $remoteUrl
    Write-Host "      Remote added" -ForegroundColor Green
}

# Push to GitHub
Write-Host ""
Write-Host "[8/8] Ready to push to GitHub" -ForegroundColor Yellow
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next step: Push to GitHub with the following command:" -ForegroundColor Yellow
Write-Host "  git push -u origin main" -ForegroundColor White
Write-Host ""

$pushNow = Read-Host "Push to GitHub now? (y/n)"
if ($pushNow -eq 'y' -or $pushNow -eq 'Y') {
    Write-Host ""
    Write-Host "Pushing to GitHub..." -ForegroundColor Yellow
    git push -u origin main
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "============================================" -ForegroundColor Green
        Write-Host "Successfully pushed to GitHub!" -ForegroundColor Green
        Write-Host "============================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Repository: $remoteUrl" -ForegroundColor Cyan
        
        # Create develop branch
        Write-Host ""
        Write-Host "Creating develop branch..." -ForegroundColor Yellow
        git checkout -b develop
        git push -u origin develop
        git checkout main
        Write-Host "Develop branch created and pushed" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "Push failed. Check your credentials and try:" -ForegroundColor Red
        Write-Host "  git push -u origin main" -ForegroundColor Yellow
    }
} else {
    Write-Host ""
    Write-Host "You can push later with: git push -u origin main" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "Repository URL: $remoteUrl" -ForegroundColor Cyan
Write-Host "Documentation: See SETUP_GUIDE.md" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
