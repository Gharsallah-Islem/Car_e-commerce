# AutoParts Store - Frontend

Angular 18 frontend application for an auto parts e-commerce platform with AI-powered mechanic assistance.

## Features

- 🏠 **Home Page** - Hero section, featured products, categories
- 🛍️ **Product Catalog** - Browse, search, filter auto parts
- 🛒 **Shopping Cart** - Manage cart items, apply coupons
- 💳 **Checkout** - Multi-step checkout with Stripe integration
- 👤 **User Profile** - Manage profile, view orders, addresses
- 🔐 **Authentication** - Login, register, OAuth2 (Google)
- 🤖 **AI Mechanic** - Image-based part identification & chatbot
- 👨‍💼 **Admin Dashboard** - Manage products, orders, users

## Tech Stack

- **Framework**: Angular 18.2.14 (Standalone Components)
- **UI Library**: Angular Material 18.2.14
- **State Management**: Angular Signals
- **Forms**: Reactive Forms, FormsModule
- **HTTP Client**: HttpClient with Interceptors
- **Routing**: Angular Router with Guards
- **Styling**: SCSS

## Prerequisites

- Node.js 18+ and npm
- Angular CLI 18.2.5+

## Installation

```bash
npm install
```

## Development Server

```bash
ng serve
```

Navigate to `http://localhost:4200/`

## Build

```bash
ng build
```

Build artifacts will be stored in the `dist/` directory.

## Project Structure

```
src/
├── app/
│   ├── core/           # Services, guards, interceptors, models
│   ├── features/       # Feature modules (home, products, cart, etc.)
│   ├── app.component.* # Root component with navbar & footer
│   ├── app.config.ts   # App configuration
│   └── app.routes.ts   # Route definitions
├── assets/             # Static assets
└── styles.scss         # Global styles
```

## Key Services

- **AuthService** - Authentication & user management
- **ProductService** - Product CRUD operations
- **CartService** - Shopping cart management
- **OrderService** - Order processing
- **NotificationService** - Toast notifications
- **LoadingService** - Global loading state

## Environment Configuration

Configure API endpoints in `src/environments/`:
- `environment.ts` - Development
- `environment.prod.ts` - Production

## API Integration

Backend API base URL: `http://localhost:8080/api`

## License

MIT
