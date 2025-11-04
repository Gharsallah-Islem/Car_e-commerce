# 🚀 Quick Start Guide - Repository Setup

This guide will help you set up your Git repository properly for the Car E-Commerce project.

## Option 1: Automated Setup (Recommended)

We've created a PowerShell script that automates the entire setup process.

### Steps:

1. **Run the initialization script:**
   ```powershell
   .\init-repo.ps1
   ```

2. **What the script does:**
   - ✅ Initializes Git repository
   - ✅ Creates all necessary folders
   - ✅ Renames 'Backend' to 'backend'
   - ✅ Adds all files to Git
   - ✅ Creates initial commit with proper message
   - ✅ Renames branch to 'main'
   - ✅ Adds remote origin
   - ✅ Optionally pushes to GitHub
   - ✅ Creates 'develop' branch

3. **Follow the prompts** and choose whether to push immediately or later.

## Option 2: Manual Setup

If you prefer to do it manually, follow these steps:

### 1. Initialize Git Repository

```powershell
git init
```

### 2. Add All Files

```powershell
git add .
```

### 3. Create Initial Commit

```powershell
git commit -m "chore: initial project structure setup

- Added comprehensive project documentation
- Created folder structure for all modules
- Configured Spring Boot backend with PostgreSQL
- Added GitHub templates and workflows
- Configured Docker Compose"
```

### 4. Rename Branch to Main

```powershell
git branch -M main
```

### 5. Add Remote Origin

```powershell
git remote add origin https://github.com/Gharsallah-Islem/Car_e-commerce.git
```

### 6. Push to GitHub

```powershell
git push -u origin main
```

### 7. Create Develop Branch

```powershell
git checkout -b develop
git push -u origin develop
git checkout main
```

## 📋 Post-Setup Tasks

After setting up the repository:

### 1. Configure GitHub Repository Settings

Go to your repository on GitHub and:

- ✅ Enable **Issues**
- ✅ Enable **Discussions**
- ✅ Set up **Branch Protection Rules** for `main` and `develop`:
  - Require pull request reviews
  - Require status checks to pass
  - Require conversation resolution
  - Include administrators

### 2. Set Up GitHub Actions Secrets

Add the following secrets in Settings → Secrets and variables → Actions:

- `JWT_SECRET` - Your JWT secret key
- `DATABASE_PASSWORD` - Production database password
- `DOCKER_USERNAME` - Docker Hub username
- `DOCKER_PASSWORD` - Docker Hub password

### 3. Invite Team Members

1. Go to Settings → Collaborators
2. Add team members
3. Assign appropriate roles

### 4. Create Project Board (Optional)

1. Go to Projects tab
2. Create a new project
3. Add columns: To Do, In Progress, Review, Done

### 5. Set Up Issue Labels

Create labels for better issue organization:
- `bug` - Bug reports
- `enhancement` - Feature requests
- `documentation` - Documentation updates
- `backend` - Backend related
- `frontend` - Frontend related
- `mobile` - Mobile app related
- `ai` - AI module related
- `high priority` - High priority items
- `good first issue` - Good for beginners

## 🌿 Branching Strategy

### Main Branches
- `main` - Production-ready code (protected)
- `develop` - Integration branch (protected)

### Supporting Branches
- `feature/feature-name` - New features
- `bugfix/bug-name` - Bug fixes
- `hotfix/issue-name` - Urgent production fixes
- `release/version` - Release preparation

### Workflow Example

```powershell
# Start a new feature
git checkout develop
git pull origin develop
git checkout -b feature/user-authentication

# Work on your feature...
git add .
git commit -m "feat(auth): implement JWT authentication"

# Push and create PR
git push origin feature/user-authentication
# Then create PR on GitHub: feature/user-authentication → develop
```

## 📝 Commit Message Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types:
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation
- `style` - Formatting
- `refactor` - Code restructuring
- `test` - Tests
- `chore` - Maintenance

### Examples:
```
feat(backend): add product search API
fix(mobile): resolve cart calculation bug
docs(readme): update installation steps
```

## 🔍 Verify Setup

Check your repository structure:

```powershell
git status
git branch -a
git remote -v
```

You should see:
- ✅ All files tracked
- ✅ On `main` branch
- ✅ Remote `origin` pointing to GitHub
- ✅ `develop` branch exists

## 📚 Next Steps

1. **Backend Development**
   - Implement entity classes
   - Create repositories
   - Build services and controllers

2. **Frontend Development**
   - Set up Angular project
   - Create components and services
   - Implement routing

3. **Mobile Development**
   - Set up Android project
   - Create MVVM architecture
   - Implement API integration

4. **AI Module Development**
   - Set up Flask/FastAPI
   - Implement image recognition
   - Build recommendation system

## 🆘 Troubleshooting

### Push Fails

If push fails due to authentication:
1. Generate a Personal Access Token on GitHub
2. Use it instead of password when prompted

### Remote Already Exists

```powershell
git remote remove origin
git remote add origin https://github.com/Gharsallah-Islem/Car_e-commerce.git
```

### Need to Undo Last Commit

```powershell
git reset --soft HEAD~1
```

## 📞 Need Help?

- Check [CONTRIBUTING.md](./CONTRIBUTING.md)
- Read [PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md)
- Open an issue on GitHub

---

**Happy Coding! 🚀**
