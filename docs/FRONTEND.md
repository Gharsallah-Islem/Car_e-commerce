# Frontend Documentation

> Angular 18 Web Application for the AutoParts Store e-commerce platform

## Table of Contents
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Feature Modules](#feature-modules)
- [Core Services](#core-services)
- [State Management](#state-management)
- [Routing & Guards](#routing--guards)
- [Real-Time Features](#real-time-features)
- [UI Components](#ui-components)

---

## Project Structure

```
frontend-web/
├── src/
│   ├── app/
│   │   ├── core/                      # Core module (singleton services)
│   │   │   ├── guards/                # Route guards
│   │   │   │   ├── auth.guard.ts      # Authentication guard
│   │   │   │   └── role.guard.ts      # Role-based access guard
│   │   │   ├── interceptors/          # HTTP interceptors
│   │   │   │   ├── auth.interceptor.ts
│   │   │   │   └── error.interceptor.ts
│   │   │   ├── models/                # TypeScript interfaces (10)
│   │   │   └── services/              # Application services (25)
│   │   ├── features/                  # Feature modules (14)
│   │   │   ├── home/                  # Landing page
│   │   │   ├── products/              # Product catalog
│   │   │   ├── cart/                  # Shopping cart
│   │   │   ├── checkout/              # Checkout flow
│   │   │   ├── orders/                # Order history
│   │   │   ├── profile/               # User profile
│   │   │   ├── auth/                  # Authentication
│   │   │   ├── admin/                 # Admin dashboard
│   │   │   ├── support/               # Support portal
│   │   │   ├── chat/                  # AI chatbot
│   │   │   ├── driver/                # Driver dashboard
│   │   │   ├── tracking/              # Delivery tracking
│   │   │   ├── ai-mechanic/           # AI part recognition
│   │   │   └── client-reclamation/    # Customer tickets
│   │   ├── app.component.ts           # Root component
│   │   ├── app.config.ts              # Application configuration
│   │   └── app.routes.ts              # Route definitions
│   ├── assets/                        # Static assets
│   ├── styles.scss                    # Global styles
│   └── index.html                     # Main HTML
├── angular.json                       # Angular CLI configuration
├── package.json                       # NPM dependencies
└── tsconfig.json                      # TypeScript configuration
```

---

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Angular** | 18 | Frontend framework |
| **TypeScript** | 5.x | Programming language |
| **Angular Material** | 18 | UI component library |
| **SCSS** | - | CSS preprocessor |
| **RxJS** | 7.x | Reactive programming |
| **Angular Signals** | - | State management |
| **ECharts** | - | Data visualization |
| **Leaflet** | - | Interactive maps |
| **SockJS/STOMP** | - | WebSocket client |
| **Stripe.js** | - | Payment integration |

---

## Feature Modules

### Public Features

#### Home (`/`)
Landing page with:
- Hero section with search
- Featured products carousel
- Category showcase
- Brand highlights
- AI Mechanic promotional section

#### Products (`/products`, `/products/:id`)
Product catalog with:
- Category/brand filtering
- Price range filter
- Search functionality
- Pagination
- Product detail view
- Image gallery
- Add to cart

#### Cart (`/cart`)
Shopping cart with:
- Item list with quantities
- Quantity adjustment
- Item removal
- Subtotal calculation
- Checkout button

#### Checkout (`/checkout`)
Checkout flow with:
- Address selection/input
- Payment method selection (Stripe/COD)
- Stripe payment integration
- Order confirmation

#### AI Mechanic (`/ai-mechanic`)
AI-powered part recognition:
- Image upload interface
- Camera capture option
- AI prediction display
- Matched products list
- Direct add to cart

#### Tracking (`/track/:trackingNumber`)
Public delivery tracking:
- Real-time map display (Leaflet)
- Delivery status timeline
- Driver information
- ETA display

---

### Authenticated Features

#### Profile (`/profile`) 🔐
User profile management:
- Personal information edit
- Profile picture upload
- Password change
- Address management

#### Orders (`/orders`) 🔐
Order history:
- Order list with status
- Order details view
- Order items
- Tracking link

#### Reclamations (`/reclamations`) 🔐
Support tickets:
- Ticket list
- Create new ticket
- Ticket details
- Status tracking

#### Chat (`/chat`) 🔐
AI-powered support chat:
- Conversation list
- Real-time messaging
- AI responses
- Product recommendations in chat

---

### Admin Features (`/admin/*`) 🔐 ADMIN

#### Dashboard (`/admin/dashboard`)
Real-time analytics:
- Sales charts (ECharts)
- Order statistics
- Revenue metrics
- Top products
- Recent orders

#### Product Management (`/admin/products`)
Product CRUD:
- Product list with search
- Add/Edit product modal
- Image upload
- Stock management
- Bulk actions

#### Category/Brand Management (`/admin/categories`, `/admin/brands`)
Catalog organization:
- Category CRUD
- Brand CRUD

#### Order Management (`/admin/orders`, `/admin/orders/:id`)
Order processing:
- Order list with filters
- Status updates
- Order details
- Delivery assignment

#### User Management (`/admin/users`)
User administration:
- User list
- Role changes (Super Admin only)
- Account activation/deactivation

#### Inventory Management (`/admin/inventory`)
Stock control:
- Stock levels dashboard
- Low stock alerts
- Supplier management
- Stock movements log
- Purchase orders
- Reorder settings

#### Delivery Management (`/admin/delivery`)
Delivery tracking:
- Active deliveries map
- Delivery list
- Status updates
- History

#### Driver Management (`/admin/drivers`)
Driver administration:
- Driver list
- Add/Edit drivers
- Availability status
- Performance metrics

---

### Support Features (`/support/*`) 🔐 SUPPORT

#### Support Dashboard (`/support/dashboard`)
Support metrics:
- Open tickets count
- Resolution time
- Performance stats

#### Tickets (`/support/tickets`, `/support/tickets/:id`)
Ticket management:
- Ticket queue
- Priority filtering
- Ticket details
- Response submission
- Status updates

#### Chat Review (`/support/chat`)
AI conversation monitoring:
- Conversation list
- Message history
- Intervention capability

#### Performance (`/support/performance`)
Support analytics:
- Response time metrics
- Resolution rates
- Agent performance

---

### Driver Features (`/driver`) 🔐 DRIVER

#### Driver Dashboard
Delivery management:
- Assigned deliveries
- Route navigation
- Status updates
- Location sharing
- Delivery completion

---

## Core Services

### Authentication Services

| Service | Purpose |
|---------|---------|
| `AuthService` | Login, logout, registration, token management |
| `StorageService` | Local/session storage with encryption |

**Key AuthService Methods:**
```typescript
login(email: string, password: string): Observable<AuthResponse>
register(userData: RegisterRequest): Observable<void>
logout(): void
getCurrentUser(): User | null
isAuthenticated(): boolean
hasRole(role: string): boolean
refreshToken(): Observable<string>
```

### API Services

| Service | Purpose |
|---------|---------|
| `ApiService` | Base HTTP client with interceptors |
| `ProductService` | Product CRUD and search |
| `OrderService` | Order management |
| `CartService` | Shopping cart operations |
| `CategoryService` | Category operations |
| `BrandService` | Brand operations |
| `InventoryService` | Stock management |
| `DeliveryService` | Delivery tracking |
| `DriverService` | Driver management |
| `ReclamationService` | Support tickets |
| `ChatService` | AI chat operations |
| `AnalyticsService` | Dashboard data |

### AI Services

| Service | Purpose |
|---------|---------|
| `AiService` | Visual search API integration |
| `RecommendationService` | Product recommendations |

### Real-Time Services

| Service | Purpose |
|---------|---------|
| `WebSocketService` | STOMP over WebSocket |
| `AdminNotificationService` | Admin alerts |
| `NotificationService` | User notifications |

### Utility Services

| Service | Purpose |
|---------|---------|
| `LoadingService` | Global loading state |
| `ExportService` | PDF/Excel export |
| `ProductImageService` | Image handling |
| `AddressService` | Address management |
| `AdminNavigationService` | Admin sidebar state |

---

## State Management

### Angular Signals
The application uses Angular's built-in Signals for reactive state:

```typescript
// Example: CartService
export class CartService {
  private cartItems = signal<CartItem[]>([]);
  private totalPrice = computed(() => 
    this.cartItems().reduce((sum, item) => 
      sum + item.price * item.quantity, 0)
  );

  // Read-only signals for components
  readonly items = this.cartItems.asReadonly();
  readonly total = this.totalPrice;

  addToCart(product: Product, quantity: number) {
    this.cartItems.update(items => [...items, { product, quantity }]);
  }
}
```

### RxJS for Async Operations
API calls and WebSocket streams use RxJS:

```typescript
// Example: ProductService
getProducts(filters: ProductFilters): Observable<Page<Product>> {
  return this.http.get<Page<Product>>('/api/products', {
    params: this.buildParams(filters)
  });
}
```

---

## Routing & Guards

### Route Structure

```typescript
// Main routes (app.routes.ts)
export const routes: Routes = [
  // Public
  { path: '', component: HomeComponent },
  { path: 'products', component: ProductListComponent },
  { path: 'products/:id', component: ProductDetailComponent },
  { path: 'cart', component: CartComponent },
  { path: 'checkout', component: CheckoutComponent },
  { path: 'ai-mechanic', component: AiMechanicComponent },
  { path: 'track/:trackingNumber', component: TrackingComponent },

  // Protected
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'orders', component: MyOrdersComponent, canActivate: [authGuard] },
  { path: 'reclamations', component: ClientReclamationComponent, canActivate: [authGuard] },
  { path: 'chat', component: ChatPageComponent, canActivate: [authGuard] },

  // Admin (with child routes)
  { 
    path: 'admin', 
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'products', component: ProductManagementComponent },
      { path: 'orders', component: OrderManagementComponent },
      // ... more admin routes
    ]
  },

  // Support (with child routes)
  {
    path: 'support',
    component: SupportLayoutComponent,
    canActivate: [supportGuard],
    children: [...]
  },

  // Driver
  { path: 'driver', component: DriverDashboardComponent, canActivate: [authGuard] },

  // Auth
  { path: 'auth/login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'auth/register', component: RegisterComponent, canActivate: [guestGuard] },
  // ... more auth routes

  // Fallback
  { path: '**', redirectTo: '' }
];
```

### Guards

#### AuthGuard
Protects routes requiring authentication:
```typescript
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.isAuthenticated()) {
    return true;
  }
  return router.createUrlTree(['/auth/login']);
};
```

#### RoleGuard
Restricts access by user role:
```typescript
export const supportGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  return authService.hasRole('SUPPORT') || 
         authService.hasRole('ADMIN') || 
         authService.hasRole('SUPER_ADMIN');
};
```

#### GuestGuard
Redirects authenticated users from auth pages:
```typescript
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  if (!authService.isAuthenticated()) {
    return true;
  }
  return inject(Router).createUrlTree(['/']);
};
```

---

## Real-Time Features

### WebSocket Configuration

```typescript
// WebSocketService
export class WebSocketService {
  private stompClient: Client;

  connect() {
    this.stompClient = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      connectHeaders: {
        Authorization: `Bearer ${this.authService.getToken()}`
      },
      onConnect: () => this.onConnected(),
      onDisconnect: () => this.onDisconnected()
    });
    this.stompClient.activate();
  }

  subscribe(topic: string, callback: (message: any) => void) {
    return this.stompClient.subscribe(topic, message => {
      callback(JSON.parse(message.body));
    });
  }
}
```

### Delivery Tracking Map

```typescript
// TrackingComponent
export class TrackingComponent implements OnInit, OnDestroy {
  private map: L.Map;
  private driverMarker: L.Marker;

  ngOnInit() {
    this.initMap();
    this.subscribeToLocationUpdates();
  }

  private subscribeToLocationUpdates() {
    this.websocket.subscribe(
      `/topic/delivery/${this.trackingNumber}`,
      (location) => this.updateDriverPosition(location)
    );
  }

  private updateDriverPosition(location: { lat: number, lng: number }) {
    this.driverMarker.setLatLng([location.lat, location.lng]);
    this.map.panTo([location.lat, location.lng]);
  }
}
```

### Admin Notifications

Real-time notifications appear in the admin navbar:
- New orders
- Low stock alerts
- New reclamations
- Driver status changes

---

## UI Components

### Angular Material Components Used

- `MatToolbar`, `MatSidenav` - Layouts
- `MatCard` - Content containers
- `MatTable`, `MatPaginator` - Data tables
- `MatDialog` - Modals
- `MatSnackBar` - Toast notifications
- `MatChip` - Status badges
- `MatAutocomplete` - Search inputs
- `MatStepper` - Multi-step forms
- `MatMenu` - Dropdown menus
- `MatIcon` - Icons
- `MatButton` - Buttons
- `MatFormField` - Form inputs

### Custom Components

| Component | Location | Purpose |
|-----------|----------|---------|
| `AdminLayoutComponent` | admin/components | Admin shell with sidebar |
| `SupportLayoutComponent` | support | Support shell with sidebar |
| `ProductCardComponent` | products | Product display card |
| `CartItemComponent` | cart | Cart line item |
| `OrderTimelineComponent` | orders | Order status timeline |
| `ChatMessageComponent` | chat | Chat message bubble |
| `DeliveryMapComponent` | tracking | Leaflet map wrapper |

---

## Development

### Running Locally

```bash
cd frontend-web

# Install dependencies
npm install

# Development server
ng serve
# or
npm run start

# Production build
ng build --configuration production
```

Application runs on: `http://localhost:4200`

### Environment Configuration

```typescript
// environment.ts (development)
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  wsUrl: 'ws://localhost:8080/ws',
  stripePublicKey: 'pk_test_...'
};

// environment.prod.ts (production)
export const environment = {
  production: true,
  apiUrl: 'https://api.yoursite.com/api',
  wsUrl: 'wss://api.yoursite.com/ws',
  stripePublicKey: 'pk_live_...'
};
```

### Testing

```bash
# Unit tests
ng test

# E2E tests
ng e2e
```
