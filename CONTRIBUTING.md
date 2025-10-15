# Contributing to Car E-Commerce Platform

Thank you for considering contributing to our project! 🎉

## 📋 Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing Guidelines](#testing-guidelines)

## 🤝 Code of Conduct

This project adheres to a code of conduct. By participating, you are expected to uphold this code. Please be respectful and professional in all interactions.

## 🚀 Getting Started

### Prerequisites
Ensure you have the following installed:
- Java 21+
- Node.js 18+
- Python 3.10+
- PostgreSQL 15+
- Git
- Docker (optional)

### Fork and Clone
1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/Car_e-commerce.git
   cd Car_e-commerce
   ```
3. Add upstream remote:
   ```bash
   git remote add upstream https://github.com/Gharsallah-Islem/Car_e-commerce.git
   ```

### Set Up Development Environment

#### Backend
```bash
cd backend
mvn clean install
```

#### Frontend
```bash
cd frontend-web
npm install
```

#### Mobile
Open `mobile-app` in Android Studio and sync Gradle.

#### AI Module
```bash
cd ai-module
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

## 🔄 Development Workflow

### Branch Strategy
- `main` - Production code (protected)
- `develop` - Integration branch
- `feature/[feature-name]` - New features
- `bugfix/[bug-name]` - Bug fixes
- `hotfix/[issue-name]` - Urgent fixes

### Creating a Feature Branch
```bash
git checkout develop
git pull upstream develop
git checkout -b feature/your-feature-name
```

## 💻 Coding Standards

### Backend (Java/Spring Boot)
- Follow [Java Code Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html)
- Use meaningful variable and method names
- Write JavaDoc for public methods
- Maximum line length: 120 characters
- Use Lombok to reduce boilerplate

### Frontend (Angular/TypeScript)
- Follow [Angular Style Guide](https://angular.io/guide/styleguide)
- Use TypeScript strict mode
- Component-based architecture
- Use RxJS for async operations
- Maximum line length: 100 characters

### Mobile (Kotlin)
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use MVVM architecture
- Meaningful naming conventions
- Maximum line length: 120 characters

### AI Module (Python)
- Follow [PEP 8](https://www.python.org/dev/peps/pep-0008/)
- Use type hints
- Write docstrings for functions
- Maximum line length: 88 characters (Black formatter)

## 📝 Commit Guidelines

We follow [Conventional Commits](https://www.conventionalcommits.org/).

### Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes
- `perf`: Performance improvements

### Examples
```bash
feat(backend): add JWT authentication
fix(mobile): resolve cart calculation bug
docs(readme): update installation instructions
test(ai): add unit tests for recommendation engine
```

### Scope Examples
- `backend`, `frontend`, `mobile`, `ai`
- `auth`, `cart`, `products`, `orders`
- `api`, `ui`, `database`

## 🔀 Pull Request Process

1. **Update your branch**
   ```bash
   git checkout develop
   git pull upstream develop
   git checkout your-feature-branch
   git rebase develop
   ```

2. **Run tests**
   ```bash
   # Backend
   cd backend && mvn test
   
   # Frontend
   cd frontend-web && npm test
   
   # AI Module
   cd ai-module && pytest
   ```

3. **Push your branch**
   ```bash
   git push origin your-feature-branch
   ```

4. **Create Pull Request**
   - Go to GitHub and create a PR
   - Fill out the PR template
   - Link related issues
   - Request reviews from team members

5. **PR Requirements**
   - [ ] All tests pass
   - [ ] Code follows style guidelines
   - [ ] Documentation updated
   - [ ] No merge conflicts
   - [ ] Approved by at least one reviewer

## 🧪 Testing Guidelines

### Backend Tests
```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=UserServiceTest

# Generate coverage report
mvn test jacoco:report
```

### Frontend Tests
```bash
# Unit tests
npm test

# E2E tests
npm run e2e

# Coverage
npm run test:coverage
```

### Mobile Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

### AI Module Tests
```bash
# Run all tests
pytest

# Run with coverage
pytest --cov=src

# Run specific test
pytest tests/test_recommendations.py
```

### Test Coverage Requirements
- Backend: Minimum 80% coverage
- Frontend: Minimum 70% coverage
- AI Module: Minimum 75% coverage

## 📚 Documentation

- Update README.md if you change functionality
- Add JSDoc/JavaDoc/docstrings for new functions
- Update API documentation for new endpoints
- Add examples for complex features

## 🐛 Bug Reports

Use the [Bug Report template](.github/ISSUE_TEMPLATE/bug_report.md)

## ✨ Feature Requests

Use the [Feature Request template](.github/ISSUE_TEMPLATE/feature_request.md)

## 📞 Questions?

- Open a [Discussion](https://github.com/Gharsallah-Islem/Car_e-commerce/discussions)
- Contact project maintainers

## 🙏 Thank You!

Your contributions make this project better for everyone. We appreciate your time and effort!

---

**Happy Coding!** 🚀
