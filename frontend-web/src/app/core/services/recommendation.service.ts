import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Product } from '../models';

/**
 * Response structure for recommendation endpoints
 */
export interface RecommendationResponse {
    success: boolean;
    products: Product[];
    count: number;
    type: string;
    message: string;
}

/**
 * For-you section response with multiple recommendation types
 */
export interface ForYouResponse {
    success: boolean;
    personalized: Product[];
    trending: Product[];
    message: string;
}

/**
 * Service for AI-powered product recommendations and user activity tracking.
 * Provides personalized, similar, also-bought, and trending recommendations.
 */
@Injectable({
    providedIn: 'root'
})
export class RecommendationService {
    private readonly API_URL = `${environment.apiUrl}/recommendations`;
    private readonly ACTIVITIES_URL = `${environment.apiUrl}/activities`;

    constructor(private http: HttpClient) { }

    // ===========================================
    // RECOMMENDATION METHODS
    // ===========================================

    /**
     * Get personalized recommendations for the current user
     */
    getPersonalizedRecommendations(limit: number = 10): Observable<Product[]> {
        return this.http.get<RecommendationResponse>(
            `${this.API_URL}/personalized`,
            { params: { limit: limit.toString() } }
        ).pipe(
            map(response => response.products || []),
            catchError(error => {
                console.error('Error fetching personalized recommendations:', error);
                return of([]);
            })
        );
    }

    /**
     * Get products similar to a given product
     */
    getSimilarProducts(productId: string, limit: number = 6): Observable<Product[]> {
        return this.http.get<RecommendationResponse>(
            `${this.API_URL}/similar/${productId}`,
            { params: { limit: limit.toString() } }
        ).pipe(
            map(response => response.products || []),
            catchError(error => {
                console.error('Error fetching similar products:', error);
                return of([]);
            })
        );
    }

    /**
     * Get products frequently bought together
     */
    getAlsoBoughtProducts(productId: string, limit: number = 6): Observable<Product[]> {
        return this.http.get<RecommendationResponse>(
            `${this.API_URL}/also-bought/${productId}`,
            { params: { limit: limit.toString() } }
        ).pipe(
            map(response => response.products || []),
            catchError(error => {
                console.error('Error fetching also-bought products:', error);
                return of([]);
            })
        );
    }

    /**
     * Get currently trending products
     */
    getTrendingProducts(days: number = 7, limit: number = 10): Observable<Product[]> {
        return this.http.get<RecommendationResponse>(
            `${this.API_URL}/trending`,
            { params: { days: days.toString(), limit: limit.toString() } }
        ).pipe(
            map(response => response.products || []),
            catchError(error => {
                console.error('Error fetching trending products:', error);
                return of([]);
            })
        );
    }

    /**
     * Get combined recommendations for home page
     */
    getForYouSection(): Observable<ForYouResponse> {
        return this.http.get<ForYouResponse>(`${this.API_URL}/for-you`).pipe(
            catchError(error => {
                console.error('Error fetching for-you section:', error);
                return of({
                    success: false,
                    personalized: [],
                    trending: [],
                    message: 'Recommendations unavailable'
                });
            })
        );
    }

    // ===========================================
    // ACTIVITY TRACKING METHODS
    // ===========================================

    /**
     * Track when a user views a product
     */
    trackProductView(productId: string, sessionId?: string): void {
        this.http.post(`${this.ACTIVITIES_URL}/view`, {
            productId,
            sessionId: sessionId || this.getSessionId()
        }).pipe(
            catchError(error => {
                console.warn('Failed to track product view:', error);
                return of(null);
            })
        ).subscribe();
    }

    /**
     * Track when a user adds a product to cart
     */
    trackAddToCart(productId: string): void {
        this.http.post(`${this.ACTIVITIES_URL}/cart`, {
            productId
        }).pipe(
            catchError(error => {
                console.warn('Failed to track add to cart:', error);
                return of(null);
            })
        ).subscribe();
    }

    /**
     * Track when a user searches for products
     */
    trackSearch(query: string): void {
        if (!query || query.trim().length < 2) return;

        this.http.post(`${this.ACTIVITIES_URL}/search`, {
            query: query.trim()
        }).pipe(
            catchError(error => {
                console.warn('Failed to track search:', error);
                return of(null);
            })
        ).subscribe();
    }

    /**
     * Get or generate a session ID for tracking
     */
    private getSessionId(): string {
        let sessionId = sessionStorage.getItem('recommendation_session_id');
        if (!sessionId) {
            sessionId = 'sess_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
            sessionStorage.setItem('recommendation_session_id', sessionId);
        }
        return sessionId;
    }
}
