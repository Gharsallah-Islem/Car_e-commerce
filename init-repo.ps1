# Git Repository Initialization Script
# Run this script to initialize your Git repository with proper structure

Write-Host "Initializing Car E-Commerce Git Repository..." -ForegroundColor Green
Write-Host ""

# Step 1: Initialize Git repository if not already initialized
if (-not (Test-Path -Path ".git")) {
    Write-Host "📦 Initializing Git repository..." -ForegroundColor Yellow
    git init
    Write-Host "✅ Git repository initialized" -ForegroundColor Green
} else {
    Write-Host "ℹ️  Git repository already initialized" -ForegroundColor Cyan
}

# Step 2: Create folder structure
Write-Host ""
Write-Host "📁 Creating project structure..." -ForegroundColor Yellow

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
        # Create .gitkeep file
        New-Item -ItemType File -Path "$folder/.gitkeep" -Force | Out-Null
        Write-Host "  ✓ Created: $folder" -ForegroundColor Gray
    }
}

Write-Host "✅ Folder structure created" -ForegroundColor Green

# Step 3: Rename Backend folder if needed
if (Test-Path -Path "Backend") {
    Write-Host ""
    Write-Host "📦 Renaming 'Backend' to 'backend'..." -ForegroundColor Yellow
    if (Test-Path -Path "backend") {
        Write-Host "⚠️  'backend' folder already exists. Skipping rename." -ForegroundColor Yellow
    } else {
        Rename-Item -Path "Backend" -NewName "backend"
        Write-Host "✅ Renamed to 'backend'" -ForegroundColor Green
    }
}

# Step 4: Add all files to Git
Write-Host ""
Write-Host "📝 Adding files to Git..." -ForegroundColor Yellow
git add .

# Step 5: Create initial commit
Write-Host ""
Write-Host "💾 Creating initial commit..." -ForegroundColor Yellow
$commitMessage = "chore: initial project structure setup

- Added comprehensive project documentation
- Created folder structure for all modules (backend, frontend-web, mobile-app, ai-module)
- Configured Spring Boot backend with PostgreSQL
- Added GitHub templates (PR, Issues)
- Configured Docker Compose
- Added .gitignore for all modules
- Created CONTRIBUTING.md guidelines"

git commit -m $commitMessage

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Initial commit created" -ForegroundColor Green
} else {
    Write-Host "⚠️  Commit failed or no changes to commit" -ForegroundColor Yellow
}

# Step 6: Rename branch to main
Write-Host ""
Write-Host "🌿 Setting up main branch..." -ForegroundColor Yellow
git branch -M main
Write-Host "✅ Branch renamed to 'main'" -ForegroundColor Green

# Step 7: Add remote origin
Write-Host ""
Write-Host "🔗 Adding remote origin..." -ForegroundColor Yellow
$remoteUrl = "https://github.com/Gharsallah-Islem/Car_e-commerce.git"

# Check if remote already exists
$remoteExists = git remote | Select-String -Pattern "origin"

if ($remoteExists) {
    Write-Host "ℹ️  Remote 'origin' already exists" -ForegroundColor Cyan
    # Update the remote URL
    git remote set-url origin $remoteUrl
    Write-Host "✅ Remote URL updated" -ForegroundColor Green
} else {
    git remote add origin $remoteUrl
    Write-Host "✅ Remote 'origin' added" -ForegroundColor Green
}

# Step 8: Push to GitHub
Write-Host ""
Write-Host "📤 Ready to push to GitHub..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Run the following command to push:" -ForegroundColor Cyan
Write-Host "  git push -u origin main" -ForegroundColor White
Write-Host ""

# Optional: Ask if user wants to push now
$pushNow = Read-Host "Would you like to push now? (y/n)"
if ($pushNow -eq 'y' -or $pushNow -eq 'Y') {
    Write-Host ""
    Write-Host "🚀 Pushing to GitHub..." -ForegroundColor Yellow
    git push -u origin main
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "🎉 Successfully pushed to GitHub!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Repository URL: $remoteUrl" -ForegroundColor Cyan
    } else {
        Write-Host ""
        Write-Host "❌ Push failed. Please check your credentials and try again." -ForegroundColor Red
        Write-Host "You can push manually with: git push -u origin main" -ForegroundColor Yellow
    }
} else {
    Write-Host ""
    Write-Host "ℹ️  Push skipped. You can push later with:" -ForegroundColor Cyan
    Write-Host "  git push -u origin main" -ForegroundColor White
}

# Step 9: Create develop branch
Write-Host ""
Write-Host "🌿 Creating develop branch..." -ForegroundColor Yellow
git checkout -b develop
git push -u origin develop
git checkout main

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "✅ Git Repository Setup Complete!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Next Steps:" -ForegroundColor Cyan
Write-Host "1. Create placeholder README files for each module" -ForegroundColor White
Write-Host "2. Set up branch protection rules on GitHub" -ForegroundColor White
Write-Host "3. Invite team members to the repository" -ForegroundColor White
Write-Host "4. Start development on feature branches" -ForegroundColor White
Write-Host ""
Write-Host "📚 Documentation:" -ForegroundColor Cyan
Write-Host "  - README.md - Main project overview" -ForegroundColor White
Write-Host "  - CONTRIBUTING.md - Contribution guidelines" -ForegroundColor White
Write-Host "  - PROJECT_STRUCTURE.md - Folder structure" -ForegroundColor White
Write-Host "  - docs/ - Detailed documentation" -ForegroundColor White
Write-Host ""
Write-Host "🔗 Repository: $remoteUrl" -ForegroundColor Cyan
Write-Host ""
