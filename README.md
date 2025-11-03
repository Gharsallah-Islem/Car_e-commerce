# 🚗 AutoParts Store - Car E-Commerce Platform

A comprehensive e-commerce platform for auto parts with AI-powered mechanic assistance, OAuth2 authentication, and Stripe payment integration.

## 📋 Project Overview

This platform provides a complete solution for car spare parts e-commerce with:
- 🛍️ Full-featured product catalog and shopping cart
- 🤖 AI-powered mechanic assistant with image recognition
- � Stripe payment integration
- � OAuth2 authentication (Google)
- 👨‍💼 Admin dashboard for product/order management
- 📱 Responsive web design

## 🏗️ Project Structure

```
Car_e-commerce/
├── Backend/                # Spring Boot 3.5.6 REST API
├── frontend-web/           # Angular 18 Web Application
├── docs/                   # Project Documentation
├── .github/                # GitHub Actions & Templates
└── docker/                 # Docker Compose & Configurations
```

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: PostgreSQL (JPA/Hibernate)
- **Security**: JWT, Spring Security, OAuth2
- **Payment**: Stripe API
- **API Documentation**: Swagger/OpenAPI

### Frontend (Web)
- **Framework**: Angular 18.2.14
- **Language**: TypeScript (Strict Mode)
- **UI Library**: Angular Material 18.2.14
- **State Management**: Angular Signals
- **Styling**: SCSS
  - Recommendation system
  - Chatbot integration

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Node.js 18+
- Python 3.10+
- PostgreSQL 15+
- Android Studio (for mobile development)
- Docker & Docker Compose

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/Gharsallah-Islem/Car_e-commerce.git
   cd Car_e-commerce
   ```

2. **Backend Setup**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

3. **Frontend Setup**
   ```bash
   cd frontend-web
   npm install
   ng serve
   ```

4. **Mobile Setup**
   - Open `mobile-app` in Android Studio
   - Sync Gradle
   - Run on emulator or device

5. **AI Module Setup**
   ```bash
   cd ai-module
   pip install -r requirements.txt
   python app.py
   ```

## 📚 Documentation

- [Backend API Documentation](./docs/backend/API.md)
- [Frontend Guide](./docs/frontend/README.md)
- [Mobile App Guide](./docs/mobile/README.md)
- [AI Module Documentation](./docs/ai/README.md)
- [Database Schema](./docs/database/SCHEMA.md)
- [Deployment Guide](./docs/deployment/README.md)

## 👥 Team Collaboration

### Branch Strategy
- `main` - Production-ready code
- `develop` - Development branch
- `feature/*` - Feature branches
- `bugfix/*` - Bug fix branches
- `hotfix/*` - Urgent fixes

### Commit Convention
Follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `style:` - Code style changes
- `refactor:` - Code refactoring
- `test:` - Test additions/changes
- `chore:` - Build/config changes

### Pull Request Template
Use the PR template in `.github/PULL_REQUEST_TEMPLATE.md`

## 🧪 Testing

```bash
# Backend tests
cd backend && mvn test

# Frontend tests
cd frontend-web && npm test

# Mobile tests
cd mobile-app && ./gradlew test

# AI Module tests
cd ai-module && pytest
```

## 🐳 Docker Deployment

```bash
# Build and run all services
docker-compose up --build

# Run specific service
docker-compose up backend
```

## 📊 Project Status

- [ ] Backend API Development
- [ ] Frontend Web Application
- [ ] Mobile Application
- [ ] AI Module Integration
- [ ] Testing & QA
- [ ] Deployment & DevOps

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Contributors

- **Islem Gharsallah** - [GitHub](https://github.com/Gharsallah-Islem)

## 📞 Contact

Project Link: [https://github.com/Gharsallah-Islem/Car_e-commerce](https://github.com/Gharsallah-Islem/Car_e-commerce)

---

**Last Updated**: October 15, 2025
