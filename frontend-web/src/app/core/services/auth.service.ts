import { Injectable, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, tap, catchError, of, throwError } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { ApiService } from './api.service';
import { StorageService } from './storage.service';
import { NotificationService } from './notification.service';
import {
    User,
    LoginRequest,
    RegisterRequest,
    AuthResponse,
    TokenPayload,
    UserRole
} from '../models';
import { environment } from '../../../environments/environment';

/**
 * Authentication Service
 * Handles login, register, OAuth2, JWT management, and user state
 */
@Injectable({
    providedIn: 'root'
})
export class AuthService {
    // User state with signals (Angular 18)
    private currentUserSubject = new BehaviorSubject<User | null>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    // Signals for reactive state
    public currentUser = signal<User | null>(null);
    public isAuthenticated = computed(() => this.currentUser() !== null);
    public isAdmin = computed(() => {
        const user = this.currentUser();
        return user?.role === UserRole.ADMIN || user?.role === UserRole.SUPER_ADMIN;
    });
    public isSuperAdmin = computed(() => this.currentUser()?.role === UserRole.SUPER_ADMIN);

    constructor(
        private apiService: ApiService,
        private storageService: StorageService,
        private notificationService: NotificationService,
        private router: Router
    ) {
        this.loadUserFromStorage();
    }

    /**
     * Login with email and password
     */
    login(credentials: LoginRequest): Observable<AuthResponse> {
        return this.apiService.post<AuthResponse>('auth/login', credentials).pipe(
            tap(response => {
                this.handleAuthSuccess(response);
                this.notificationService.success('Login successful!');
            }),
            catchError(error => {
                this.notificationService.error(error.error?.message || 'Login failed');
                return throwError(() => error);
            })
        );
    }

    /**
     * Register new user
     */
    register(userData: RegisterRequest): Observable<AuthResponse> {
        return this.apiService.post<AuthResponse>('auth/register', userData).pipe(
            tap(response => {
                this.handleAuthSuccess(response);
                this.notificationService.success('Registration successful!');
            }),
            catchError(error => {
                this.notificationService.error(error.error?.message || 'Registration failed');
                return throwError(() => error);
            })
        );
    }

    /**
     * Initiate Google OAuth2 login
     */
    loginWithGoogle(): void {
        // Redirect to backend OAuth2 endpoint
        const googleAuthUrl = `${environment.apiUrl}/oauth2/authorization/google`;
        window.location.href = googleAuthUrl;
    }

    /**
     * Handle OAuth2 callback with token
     */
    handleOAuthCallback(token: string): void {
        // Store token immediately
        this.storageService.saveToken(token);

        // Fetch user data from backend
        this.getCurrentUser().subscribe({
            next: (user) => {
                this.notificationService.success(`Welcome back, ${user.firstName || user.email}!`);
                this.router.navigate(['/']);
            },
            error: (error) => {
                console.error('Failed to fetch user data after OAuth:', error);
                this.notificationService.error('Failed to complete login');
                this.logout();
            }
        });
    }

    /**
     * Logout user
     */
    logout(): void {
        this.storageService.clearAll();
        this.currentUser.set(null);
        this.currentUserSubject.next(null);
        this.notificationService.info('Logged out successfully');
        this.router.navigate(['/auth/login']);
    }

    /**
     * Get current user profile from backend
     */
    getCurrentUser(): Observable<User> {
        return this.apiService.get<User>('auth/me').pipe(
            tap(user => {
                this.currentUser.set(user);
                this.currentUserSubject.next(user);
                this.storageService.saveUser(user);
            })
        );
    }

    /**
     * Refresh JWT token
     */
    refreshToken(): Observable<AuthResponse> {
        const refreshToken = this.storageService.getRefreshToken();

        if (!refreshToken) {
            return throwError(() => new Error('No refresh token available'));
        }

        return this.apiService.post<AuthResponse>('auth/refresh', { refreshToken }).pipe(
            tap(response => {
                this.storageService.saveToken(response.token);
                if (response.refreshToken) {
                    this.storageService.saveRefreshToken(response.refreshToken);
                }
            }),
            catchError(error => {
                this.logout();
                return throwError(() => error);
            })
        );
    }

    /**
     * Check if token is expired
     */
    isTokenExpired(): boolean {
        const token = this.storageService.getToken();

        if (!token) {
            return true;
        }

        try {
            const decoded = jwtDecode<TokenPayload>(token);
            const expirationDate = new Date(decoded.exp * 1000);
            return expirationDate < new Date();
        } catch (error) {
            return true;
        }
    }

    /**
     * Get decoded token payload
     */
    getTokenPayload(): TokenPayload | null {
        const token = this.storageService.getToken();

        if (!token) {
            return null;
        }

        try {
            return jwtDecode<TokenPayload>(token);
        } catch (error) {
            return null;
        }
    }

    /**
     * Check if user has specific role
     */
    hasRole(role: UserRole): boolean {
        const user = this.currentUser();
        return user?.role === role;
    }

    /**
     * Check if user has any of the specified roles
     */
    hasAnyRole(roles: UserRole[]): boolean {
        const user = this.currentUser();
        return user ? roles.includes(user.role) : false;
    }

    /**
     * Update user profile
     */
    updateProfile(userData: Partial<User>): Observable<User> {
        return this.apiService.put<User>('auth/profile', userData).pipe(
            tap(user => {
                this.currentUser.set(user);
                this.currentUserSubject.next(user);
                this.storageService.saveUser(user);
                this.notificationService.success('Profile updated successfully');
            }),
            catchError(error => {
                this.notificationService.error('Failed to update profile');
                return throwError(() => error);
            })
        );
    }

    /**
     * Change password
     */
    changePassword(oldPassword: string, newPassword: string): Observable<void> {
        return this.apiService.post<void>('auth/change-password', {
            oldPassword,
            newPassword
        }).pipe(
            tap(() => {
                this.notificationService.success('Password changed successfully');
            }),
            catchError(error => {
                this.notificationService.error(error.error?.message || 'Failed to change password');
                return throwError(() => error);
            })
        );
    }

    /**
     * Request password reset
     */
    requestPasswordReset(email: string): Observable<void> {
        return this.apiService.post<void>('auth/forgot-password', { email }).pipe(
            tap(() => {
                this.notificationService.success('Password reset email sent');
            }),
            catchError(error => {
                this.notificationService.error('Failed to send password reset email');
                return throwError(() => error);
            })
        );
    }

    /**
     * Reset password with token
     */
    resetPassword(token: string, newPassword: string): Observable<void> {
        return this.apiService.post<void>('auth/reset-password', { token, newPassword }).pipe(
            tap(() => {
                this.notificationService.success('Password reset successfully');
            }),
            catchError(error => {
                this.notificationService.error('Failed to reset password');
                return throwError(() => error);
            })
        );
    }

    /**
     * Handle successful authentication
     */
    private handleAuthSuccess(response: AuthResponse): void {
        this.storageService.saveToken(response.token);

        if (response.refreshToken) {
            this.storageService.saveRefreshToken(response.refreshToken);
        }

        this.storageService.saveUser(response.user);
        this.currentUser.set(response.user);
        this.currentUserSubject.next(response.user);
    }

    /**
     * Load user from localStorage on app init
     */
    private loadUserFromStorage(): void {
        const token = this.storageService.getToken();
        const user = this.storageService.getUser();

        if (token && user && !this.isTokenExpired()) {
            this.currentUser.set(user);
            this.currentUserSubject.next(user);

            // Optionally refresh user data from backend
            this.getCurrentUser().subscribe({
                error: () => {
                    // If fetching user fails, logout
                    this.logout();
                }
            });
        } else if (token && this.isTokenExpired()) {
            // Token expired, try to refresh
            this.refreshToken().subscribe({
                error: () => {
                    this.logout();
                }
            });
        }
    }

    /**
     * Get authentication token
     */
    getToken(): string | null {
        return this.storageService.getToken();
    }
}
