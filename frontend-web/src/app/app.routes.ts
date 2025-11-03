import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';

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

    // Admin route (protected)
    {
        path: 'admin',
        loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent),
        canActivate: [authGuard]
    },

    // AI Mechanic route
    {
        path: 'ai-mechanic',
        loadComponent: () => import('./features/ai-mechanic/ai-mechanic.component').then(m => m.AiMechanicComponent)
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
