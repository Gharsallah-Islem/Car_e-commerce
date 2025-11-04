import { Injectable } from '@angular/core';

/**
 * Service for managing localStorage operations with type safety
 */
@Injectable({
    providedIn: 'root'
})
export class StorageService {
    private readonly TOKEN_KEY = 'auth_token';
    private readonly REFRESH_TOKEN_KEY = 'refresh_token';
    private readonly USER_KEY = 'current_user';
    private readonly CART_KEY = 'shopping_cart';
    private readonly LANGUAGE_KEY = 'selected_language';
    private readonly THEME_KEY = 'theme_preference';

    /**
     * Save JWT token
     */
    saveToken(token: string): void {
        localStorage.setItem(this.TOKEN_KEY, token);
    }

    /**
     * Get JWT token
     */
    getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    /**
     * Remove JWT token
     */
    removeToken(): void {
        localStorage.removeItem(this.TOKEN_KEY);
    }

    /**
     * Save refresh token
     */
    saveRefreshToken(token: string): void {
        localStorage.setItem(this.REFRESH_TOKEN_KEY, token);
    }

    /**
     * Get refresh token
     */
    getRefreshToken(): string | null {
        return localStorage.getItem(this.REFRESH_TOKEN_KEY);
    }

    /**
     * Remove refresh token
     */
    removeRefreshToken(): void {
        localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    }

    /**
     * Save user data
     */
    saveUser(user: any): void {
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }

    /**
     * Get user data
     */
    getUser(): any {
        const user = localStorage.getItem(this.USER_KEY);
        return user ? JSON.parse(user) : null;
    }

    /**
     * Remove user data
     */
    removeUser(): void {
        localStorage.removeItem(this.USER_KEY);
    }

    /**
     * Save cart data (for guest users)
     */
    saveCart(cart: any): void {
        localStorage.setItem(this.CART_KEY, JSON.stringify(cart));
    }

    /**
     * Get cart data
     */
    getCart(): any {
        const cart = localStorage.getItem(this.CART_KEY);
        return cart ? JSON.parse(cart) : null;
    }

    /**
     * Remove cart data
     */
    removeCart(): void {
        localStorage.removeItem(this.CART_KEY);
    }

    /**
     * Save language preference
     */
    saveLanguage(language: string): void {
        localStorage.setItem(this.LANGUAGE_KEY, language);
    }

    /**
     * Get language preference
     */
    getLanguage(): string | null {
        return localStorage.getItem(this.LANGUAGE_KEY);
    }

    /**
     * Save theme preference (light/dark)
     */
    saveTheme(theme: string): void {
        localStorage.setItem(this.THEME_KEY, theme);
    }

    /**
     * Get theme preference
     */
    getTheme(): string | null {
        return localStorage.getItem(this.THEME_KEY);
    }

    /**
     * Clear all stored data (logout)
     */
    clearAll(): void {
        this.removeToken();
        this.removeRefreshToken();
        this.removeUser();
        // Keep language and theme preferences
    }

    /**
     * Save custom data with key
     */
    save(key: string, value: any): void {
        localStorage.setItem(key, JSON.stringify(value));
    }

    /**
     * Get custom data by key
     */
    get<T>(key: string): T | null {
        const item = localStorage.getItem(key);
        return item ? JSON.parse(item) : null;
    }

    /**
     * Remove custom data by key
     */
    remove(key: string): void {
        localStorage.removeItem(key);
    }

    /**
     * Check if key exists
     */
    has(key: string): boolean {
        return localStorage.getItem(key) !== null;
    }
}
