import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models';

/**
 * Role Guard
 * Protects routes based on user role
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
        router.navigate(['/auth/login']);
        return false;
    }

    const allowedRoles = route.data['roles'] as UserRole[];

    if (!allowedRoles || allowedRoles.length === 0) {
        return true;
    }

    const hasRole = authService.hasAnyRole(allowedRoles);

    if (hasRole) {
        return true;
    }

    // User doesn't have required role
    router.navigate(['/unauthorized']);
    return false;
};

/**
 * Admin Guard
 * Shorthand for requiring ADMIN or SUPER_ADMIN role
 */
export const adminGuard: CanActivateFn = (route) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
        router.navigate(['/auth/login']);
        return false;
    }

    if (authService.isAdmin()) {
        return true;
    }

    router.navigate(['/unauthorized']);
    return false;
};

/**
 * Super Admin Guard
 * Requires SUPER_ADMIN role
 */
export const superAdminGuard: CanActivateFn = (route) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
        router.navigate(['/auth/login']);
        return false;
    }

    if (authService.isSuperAdmin()) {
        return true;
    }

    router.navigate(['/unauthorized']);
    return false;
};

/**
 * Support Guard
 * Requires SUPPORT, ADMIN, or SUPER_ADMIN role
 */
export const supportGuard: CanActivateFn = (route) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
        router.navigate(['/auth/login']);
        return false;
    }

    // Allow SUPPORT, ADMIN, and SUPER_ADMIN
    if (authService.hasAnyRole([UserRole.SUPPORT, UserRole.ADMIN, UserRole.SUPER_ADMIN])) {
        return true;
    }

    router.navigate(['/unauthorized']);
    return false;
};
