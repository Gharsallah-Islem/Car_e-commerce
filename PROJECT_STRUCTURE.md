# 🏗️ Project Structure

This document outlines the complete folder structure for the Car E-Commerce platform.

## 📂 Root Structure

```
Car_e-commerce/
├── .github/                    # GitHub configuration
│   ├── workflows/             # CI/CD workflows
│   ├── ISSUE_TEMPLATE/        # Issue templates
│   └── PULL_REQUEST_TEMPLATE.md
├── backend/                   # Spring Boot Backend
├── frontend-web/              # Angular Web Application
├── mobile-app/                # Kotlin Android Application
├── ai-module/                 # Python AI/ML Services
├── docs/                      # Documentation
├── docker/                    # Docker configurations
├── scripts/                   # Utility scripts
├── .gitignore                # Git ignore rules
├── README.md                 # Main README
├── CONTRIBUTING.md           # Contribution guidelines
├── LICENSE                   # Project license
└── docker-compose.yml        # Docker compose configuration
```

## 🔧 Backend Structure (Spring Boot)

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── Backend/
│   │   │               ├── config/              # Configuration classes
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── WebConfig.java
│   │   │               │   └── SwaggerConfig.java
│   │   │               ├── controller/          # REST Controllers
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── ProductController.java
│   │   │               │   ├── OrderController.java
│   │   │               │   ├── CartController.java
│   │   │               │   ├── UserController.java
│   │   │               │   ├── AdminController.java
│   │   │               │   ├── SuperAdminController.java
│   │   │               │   ├── ReclamationController.java
│   │   │               │   ├── DeliveryController.java
│   │   │               │   ├── ChatController.java
│   │   │               │   └── IAController.java
│   │   │               ├── dto/                 # Data Transfer Objects
│   │   │               │   ├── UserDTO.java
│   │   │               │   ├── ProductDTO.java
│   │   │               │   ├── OrderDTO.java
│   │   │               │   └── ...
│   │   │               ├── entity/              # JPA Entities
│   │   │               │   ├── User.java
│   │   │               │   ├── Product.java
│   │   │               │   ├── Order.java
│   │   │               │   └── ...
│   │   │               ├── exception/           # Custom Exceptions
│   │   │               │   ├── CustomExceptionHandler.java
│   │   │               │   ├── ResourceNotFoundException.java
│   │   │               │   └── ValidationException.java
│   │   │               ├── repository/          # JPA Repositories
│   │   │               │   ├── UserRepository.java
│   │   │               │   ├── ProductRepository.java
│   │   │               │   └── ...
│   │   │               ├── security/            # Security components
│   │   │               │   ├── JwtTokenProvider.java
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   ├── CustomUserDetailsService.java
│   │   │               │   └── SecurityConstants.java
│   │   │               ├── service/             # Service interfaces
│   │   │               │   ├── impl/           # Service implementations
│   │   │               │   └── ...
│   │   │               ├── util/                # Utility classes
│   │   │               │   └── EmailUtil.java
│   │   │               └── EcommerceSparePartsApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── Backend/
├── target/                    # Build output
├── .mvn/                      # Maven wrapper
├── mvnw                       # Maven wrapper script (Unix)
├── mvnw.cmd                   # Maven wrapper script (Windows)
├── pom.xml                    # Maven configuration
├── Dockerfile                 # Docker image definition
└── DATABASE_SETUP.md          # Database setup guide
```

## 🌐 Frontend Web Structure (Angular)

```
frontend-web/
├── src/
│   ├── app/
│   │   ├── core/              # Core module (singleton services)
│   │   │   ├── guards/
│   │   │   ├── interceptors/
│   │   │   ├── services/
│   │   │   └── models/
│   │   ├── shared/            # Shared module (reusable components)
│   │   │   ├── components/
│   │   │   ├── directives/
│   │   │   ├── pipes/
│   │   │   └── shared.module.ts
│   │   ├── features/          # Feature modules
│   │   │   ├── auth/
│   │   │   │   ├── login/
│   │   │   │   ├── register/
│   │   │   │   └── auth.module.ts
│   │   │   ├── products/
│   │   │   │   ├── product-list/
│   │   │   │   ├── product-detail/
│   │   │   │   └── products.module.ts
│   │   │   ├── cart/
│   │   │   ├── orders/
│   │   │   ├── admin/
│   │   │   └── chat/
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   ├── app.component.scss
│   │   ├── app.routes.ts
│   │   └── app.config.ts
│   ├── assets/
│   │   ├── images/
│   │   ├── icons/
│   │   └── styles/
│   ├── environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   ├── index.html
│   ├── main.ts
│   └── styles.scss
├── node_modules/
├── dist/                      # Build output
├── .angular/                  # Angular cache
├── angular.json               # Angular configuration
├── package.json               # NPM dependencies
├── tsconfig.json              # TypeScript configuration
├── Dockerfile
└── README.md
```

## 📱 Mobile App Structure (Kotlin/Android)

```
mobile-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── carecommerce/
│   │   │   │           ├── ui/                # UI Layer
│   │   │   │           │   ├── auth/
│   │   │   │           │   ├── products/
│   │   │   │           │   ├── cart/
│   │   │   │           │   ├── orders/
│   │   │   │           │   └── profile/
│   │   │   │           ├── data/              # Data Layer
│   │   │   │           │   ├── models/
│   │   │   │           │   ├── repository/
│   │   │   │           │   └── remote/
│   │   │   │           ├── domain/            # Domain Layer
│   │   │   │           │   ├── usecase/
│   │   │   │           │   └── model/
│   │   │   │           ├── di/                # Dependency Injection
│   │   │   │           └── util/
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   ├── values/
│   │   │   │   └── menu/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── local.properties
└── README.md
```

## 🤖 AI Module Structure (Python)

```
ai-module/
├── src/
│   ├── api/                   # API endpoints
│   │   ├── __init__.py
│   │   ├── routes.py
│   │   └── schemas.py
│   ├── models/                # ML models
│   │   ├── __init__.py
│   │   ├── image_recognition.py
│   │   ├── recommendation.py
│   │   └── chatbot.py
│   ├── services/              # Business logic
│   │   ├── __init__.py
│   │   ├── image_service.py
│   │   ├── recommendation_service.py
│   │   └── chatbot_service.py
│   ├── utils/                 # Utilities
│   │   ├── __init__.py
│   │   ├── preprocessor.py
│   │   └── logger.py
│   └── config/                # Configuration
│       ├── __init__.py
│       └── settings.py
├── tests/
│   ├── test_api.py
│   ├── test_models.py
│   └── test_services.py
├── models/                    # Trained models (saved)
│   ├── image_model.h5
│   └── recommendation_model.pkl
├── data/                      # Training data
│   ├── raw/
│   └── processed/
├── notebooks/                 # Jupyter notebooks
│   └── exploratory.ipynb
├── requirements.txt
├── Dockerfile
├── app.py                     # Main application
└── README.md
```

## 📚 Documentation Structure

```
docs/
├── README.md                  # Documentation index
├── getting-started/
│   ├── README.md
│   └── INSTALLATION.md
├── backend/
│   ├── README.md
│   ├── API.md
│   ├── DATABASE.md
│   ├── SECURITY.md
│   └── ARCHITECTURE.md
├── frontend/
│   ├── README.md
│   └── DEVELOPMENT.md
├── mobile/
│   ├── README.md
│   └── DEVELOPMENT.md
├── ai/
│   ├── README.md
│   └── DEVELOPMENT.md
├── deployment/
│   ├── README.md
│   ├── DOCKER.md
│   └── CICD.md
├── api/
│   └── README.md
└── troubleshooting/
    └── README.md
```

## 🐳 Docker Structure

```
docker/
├── backend/
│   └── Dockerfile
├── frontend/
│   └── Dockerfile
├── ai-module/
│   └── Dockerfile
├── postgres/
│   └── init.sql
└── nginx/
    └── nginx.conf
```

## 📜 Scripts Structure

```
scripts/
├── setup/
│   ├── setup-dev.sh           # Development environment setup
│   └── setup-prod.sh          # Production environment setup
├── build/
│   ├── build-all.sh           # Build all modules
│   └── build-backend.sh
├── deploy/
│   ├── deploy-dev.sh
│   └── deploy-prod.sh
└── utils/
    ├── backup-db.sh
    └── restore-db.sh
```

## 🔄 GitHub Workflows

```
.github/
├── workflows/
│   ├── backend-ci.yml         # Backend CI/CD
│   ├── frontend-ci.yml        # Frontend CI/CD
│   ├── mobile-ci.yml          # Mobile CI/CD
│   ├── ai-module-ci.yml       # AI Module CI/CD
│   └── deploy.yml             # Deployment workflow
├── ISSUE_TEMPLATE/
│   ├── bug_report.md
│   └── feature_request.md
└── PULL_REQUEST_TEMPLATE.md
```

## 📝 Notes

- All folders should have appropriate `.gitkeep` files if initially empty
- Each module should have its own `README.md`
- Maintain consistent naming conventions across all modules
- Keep environment-specific files out of version control

---

**Last Updated**: October 15, 2025
