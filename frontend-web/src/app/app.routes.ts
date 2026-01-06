import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { supportGuard } from './core/guards/role.guard';

export const routes: Routes = [
    // Home page (public)
    {
        path: '',
        loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent)
    },

    // Products routes
    {
        path: 'products',
        loadComponent: () => import('./features/products/product-list/product-list.component').then(m => m.ProductListComponent)
    },
    {
        path: 'products/:id',
        loadComponent: () => import('./features/products/product-detail/product-detail.component').then(m => m.ProductDetailComponent)
    },

    // Cart route
    {
        path: 'cart',
        loadComponent: () => import('./features/cart/cart.component').then(m => m.CartComponent)
    },

    // Checkout route
    {
        path: 'checkout',
        loadComponent: () => import('./features/checkout/checkout.component').then(m => m.CheckoutComponent)
    },

    // Profile route (protected)
    {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent),
        canActivate: [authGuard]
    },

    // My Orders route (protected)
    {
        path: 'orders',
        loadComponent: () => import('./features/orders/my-orders.component').then(m => m.MyOrdersComponent),
        canActivate: [authGuard]
    },

    // My Reclamations route (protected)
    {
        path: 'reclamations',
        loadComponent: () => import('./features/client-reclamation/client-reclamation.component').then(m => m.ClientReclamationComponent),
        canActivate: [authGuard]
    },

    // Admin routes (protected with child routes)
    {
        path: 'admin',
        loadComponent: () => import('./features/admin/components/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
        canActivate: [authGuard],
        children: [
            {
                path: '',
                redirectTo: 'dashboard',
                pathMatch: 'full'
            },
            {
                path: 'dashboard',
                loadComponent: () => import('./features/admin/components/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
            },
            {
                path: 'products',
                loadComponent: () => import('./features/admin/components/product-management/product-management.component').then(m => m.ProductManagementComponent)
            },
            {
                path: 'categories',
                loadComponent: () => import('./features/admin/components/category-management/category-management.component').then(m => m.CategoryManagementComponent)
            },
            {
                path: 'brands',
                loadComponent: () => import('./features/admin/components/brand-management/brand-management.component').then(m => m.BrandManagementComponent)
            },
            {
                path: 'orders',
                loadComponent: () => import('./features/admin/components/order-management/order-management.component').then(m => m.OrderManagementComponent)
            },
            {
                path: 'orders/:id',
                loadComponent: () => import('./features/admin/components/order-details/order-details.component').then(m => m.OrderDetailsComponent)
            },
            {
                path: 'users',
                loadComponent: () => import('./features/admin/components/user-management/user-management.component').then(m => m.UserManagementComponent)
            },
            {
                path: 'inventory',
                loadComponent: () => import('./features/admin/inventory-management/inventory-management.component').then(m => m.InventoryManagementComponent)
            },
            {
                path: 'delivery',
                loadComponent: () => import('./features/admin/delivery-management/delivery-management.component').then(m => m.DeliveryManagementComponent)
            },
            {
                path: 'drivers',
                loadComponent: () => import('./features/admin/driver-management/driver-management.component').then(m => m.DriverManagementComponent)
            },
            {
                path: 'settings',
                loadComponent: () => import('./features/admin/admin-settings/admin-settings.component').then(m => m.AdminSettingsComponent)
            }
        ]
    },

    // Support Team routes (protected with supportGuard)
    {
        path: 'support',
        loadComponent: () => import('./features/support/support-layout/support-layout.component').then(m => m.SupportLayoutComponent),
        canActivate: [supportGuard],
        children: [
            {
                path: '',
                redirectTo: 'dashboard',
                pathMatch: 'full'
            },
            {
                path: 'dashboard',
                loadComponent: () => import('./features/support/support-dashboard/support-dashboard.component').then(m => m.SupportDashboardComponent)
            },
            {
                path: 'tickets',
                loadComponent: () => import('./features/support/support-tickets/support-tickets.component').then(m => m.SupportTicketsComponent)
            },
            {
                path: 'tickets/:id',
                loadComponent: () => import('./features/support/support-ticket-detail/support-ticket-detail.component').then(m => m.SupportTicketDetailComponent)
            },
            {
                path: 'chat',
                loadComponent: () => import('./features/support/support-chat/support-chat.component').then(m => m.SupportChatComponent)
            },
            {
                path: 'performance',
                loadComponent: () => import('./features/support/support-performance/support-performance.component').then(m => m.SupportPerformanceComponent)
            }
        ]
    },

    // AI Mechanic route
    {
        path: 'ai-mechanic',
        loadComponent: () => import('./features/ai-mechanic/ai-mechanic.component').then(m => m.AiMechanicComponent)
    },

    // Delivery Tracking route (public)
    {
        path: 'track/:trackingNumber',
        loadComponent: () => import('./features/tracking/tracking.component').then(m => m.TrackingComponent)
    },

    // Driver Dashboard route (protected)
    {
        path: 'driver',
        loadComponent: () => import('./features/driver/driver-dashboard.component').then(m => m.DriverDashboardComponent),
        canActivate: [authGuard]
    },

    // Chat route (protected)
    {
        path: 'chat',
        loadComponent: () => import('./features/chat/chat-page/chat-page.component').then(m => m.ChatPageComponent),
        canActivate: [authGuard]
    },

    // Authentication routes (lazy loaded)
    {
        path: 'auth',
        children: [
            {
                path: 'login',
                loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
                canActivate: [guestGuard]
            },
            {
                path: 'register',
                loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent),
                canActivate: [guestGuard]
            },
            {
                path: 'verify-email',
                loadComponent: () => import('./features/auth/verify-email/verify-email.component').then(m => m.VerifyEmailComponent)
            },
            {
                path: 'forgot-password',
                loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent),
                canActivate: [guestGuard]
            },
            {
                path: 'reset-password',
                loadComponent: () => import('./features/auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent),
                canActivate: [guestGuard]
            },
            {
                path: 'oauth-callback',
                loadComponent: () => import('./features/auth/oauth-callback/oauth-callback.component').then(m => m.OauthCallbackComponent)
            }
        ]
    },

    // Wildcard route (404)
    {
        path: '**',
        redirectTo: ''
    }
];
