# 📚 Car E-Commerce Platform Documentation

Welcome to the comprehensive documentation for the Car E-Commerce Platform.

## 📑 Table of Contents

### 1. [Getting Started](./getting-started/README.md)
- Installation Guide
- Quick Start
- Environment Setup

### 2. [Backend Documentation](./backend/README.md)
- [API Documentation](./backend/API.md)
- [Database Schema](./backend/DATABASE.md)
- [Authentication & Security](./backend/SECURITY.md)
- [Service Architecture](./backend/ARCHITECTURE.md)

### 3. [Frontend Web Documentation](./frontend/README.md)
- Component Structure
- State Management
- Routing
- API Integration

### 4. [Mobile App Documentation](./mobile/README.md)
- App Architecture
- Features
- Build & Deployment

### 5. [AI Module Documentation](./ai/README.md)
- Image Recognition
- Recommendation System
- Chatbot Integration
- Model Training

### 6. [Deployment](./deployment/README.md)
- Docker Setup
- CI/CD Pipeline
- Production Deployment
- Monitoring & Logging

### 7. [Development Guidelines](./development/README.md)
- Coding Standards
- Git Workflow
- Testing Strategy
- Code Review Process

### 8. [Project Management](PRODUCT_BACKLOG_GUIDE.md)
- Product Backlog Guide
- User Story Templates
- Sprint Planning
- Agile Best Practices

### 9. [API Reference](./api/README.md)
- REST API Endpoints
- WebSocket Events
- Request/Response Formats
- Error Codes

## 🚀 Quick Links

- [Installation Guide](./getting-started/INSTALLATION.md)
- [API Documentation](./backend/API.md)
- [Contributing Guidelines](../CONTRIBUTING.md)
- [Troubleshooting](./troubleshooting/README.md)

## 📊 Architecture Overview

```
┌─────────────────┐         ┌─────────────────┐
│   Web Client    │         │  Mobile Client  │
│    (Angular)    │         │    (Kotlin)     │
└────────┬────────┘         └────────┬────────┘
         │                           │
         └───────────┬───────────────┘
                     │
              ┌──────▼──────┐
              │   Backend   │
              │ (Spring Boot)│
              └──────┬──────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
    ┌────▼────┐ ┌───▼────┐ ┌───▼────┐
    │PostgreSQL│ │AI Module│ │ Redis │
    │         │ │ (Python)│ │       │
    └─────────┘ └─────────┘ └───────┘
```

## 🛠️ Technologies Used

| Component | Technologies |
|-----------|-------------|
| Backend | Spring Boot, Java 21, PostgreSQL, JWT |
| Frontend | Angular, TypeScript, RxJS |
| Mobile | Kotlin, Android, MVVM |
| AI Module | Python, TensorFlow, Flask |
| DevOps | Docker, GitHub Actions |

## 📖 Documentation Sections

### For Developers
- [Backend Development Guide](./backend/DEVELOPMENT.md)
- [Frontend Development Guide](./frontend/DEVELOPMENT.md)
- [Mobile Development Guide](./mobile/DEVELOPMENT.md)
- [AI Module Development Guide](./ai/DEVELOPMENT.md)

### For DevOps
- [Deployment Guide](./deployment/README.md)
- [Docker Configuration](./deployment/DOCKER.md)
- [CI/CD Pipeline](./deployment/CICD.md)

### For Testers
- [Testing Strategy](./testing/STRATEGY.md)
- [Test Cases](./testing/TEST_CASES.md)
- [Bug Reporting](./testing/BUG_REPORTING.md)

### For Users
- [User Manual](./user-guide/README.md)
- [FAQ](./user-guide/FAQ.md)
- [Feature Overview](./user-guide/FEATURES.md)

## 🔄 Update History

| Date | Version | Description |
|------|---------|-------------|
| 2025-10-15 | 1.0.0 | Initial documentation |

## 📞 Support

For questions or issues:
- Open an [Issue](https://github.com/Gharsallah-Islem/Car_e-commerce/issues)
- Start a [Discussion](https://github.com/Gharsallah-Islem/Car_e-commerce/discussions)
- Contact: [Your Email]

---

**Last Updated**: October 15, 2025
