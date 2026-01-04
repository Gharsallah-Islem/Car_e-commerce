import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

/**
 * Response from AI part analysis
 */
export interface PartAnalysisResponse {
    success: boolean;
    partName: string;
    confidence: number;
    confidencePercent: string;
    recommendationId: string;
    products: AnalyzedProduct[];
    productsFound: boolean;
}

/**
 * Product matched from AI analysis
 */
export interface AnalyzedProduct {
    id: string;
    name: string;
    price: number;
    stock: number;
}

/**
 * AI Service for part recognition and virtual mechanic features
 */
@Injectable({
    providedIn: 'root'
})
export class AiService {
    private readonly API_URL = `${environment.apiUrl}/ia`;

    constructor(private http: HttpClient) { }

    /**
     * Analyze a car part image using AI
     * @param imageBase64 Base64 encoded image data
     * @returns Observable with analysis results
     */
    analyzePartImage(imageBase64: string): Observable<PartAnalysisResponse> {
        // Remove data URL prefix if present
        let cleanBase64 = imageBase64;
        if (imageBase64.includes(',')) {
            cleanBase64 = imageBase64.split(',')[1];
        }

        return this.http.post<{ analysis: string }>(`${this.API_URL}/analyze-image`, {
            imageData: cleanBase64
        }).pipe(
            map(response => {
                try {
                    // The backend returns a JSON string in the 'analysis' field
                    const parsed = JSON.parse(response.analysis);
                    return parsed as PartAnalysisResponse;
                } catch (e) {
                    // If parsing fails, return a basic response
                    console.error('Failed to parse AI response:', e);
                    return {
                        success: true,
                        partName: 'Unknown Part',
                        confidence: 0,
                        confidencePercent: '0%',
                        recommendationId: '',
                        products: [],
                        productsFound: false
                    };
                }
            }),
            catchError(error => {
                console.error('AI analysis error:', error);
                return of({
                    success: false,
                    partName: 'Error',
                    confidence: 0,
                    confidencePercent: '0%',
                    recommendationId: '',
                    products: [],
                    productsFound: false
                });
            })
        );
    }

    /**
     * Send a question to the virtual mechanic chatbot
     * @param question User's question
     * @returns Observable with AI response
     */
    askVirtualMechanic(question: string): Observable<{ response: string }> {
        return this.http.post<{ response: string }>(`${this.API_URL}/virtual-mechanic`, {
            question
        }).pipe(
            catchError(error => {
                console.error('Virtual mechanic error:', error);
                return of({
                    response: 'Sorry, I am temporarily unavailable. Please try again later.'
                });
            })
        );
    }
}
