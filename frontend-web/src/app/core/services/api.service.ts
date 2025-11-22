import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Base API service for HTTP operations
 */
@Injectable({
    providedIn: 'root'
})
export class ApiService {
    private readonly API_URL = environment.apiUrl;

    constructor(private http: HttpClient) { }

    /**
     * GET request
     */
    get<T>(endpoint: string, params?: any): Observable<T> {
        const httpParams = this.buildParams(params);
        return this.http.get<T>(`${this.API_URL}/${endpoint}`, { params: httpParams });
    }

    /**
     * POST request
     */
    post<T>(endpoint: string, body: any): Observable<T> {
        return this.http.post<T>(`${this.API_URL}/${endpoint}`, body);
    }

    /**
     * PUT request
     */
    put<T>(endpoint: string, body: any): Observable<T> {
        return this.http.put<T>(`${this.API_URL}/${endpoint}`, body);
    }

    /**
     * PATCH request
     */
    patch<T>(endpoint: string, body: any, params?: any): Observable<T> {
        const httpParams = this.buildParams(params);
        return this.http.patch<T>(`${this.API_URL}/${endpoint}`, body, { params: httpParams });
    }

    /**
     * DELETE request
     */
    delete<T>(endpoint: string): Observable<T> {
        return this.http.delete<T>(`${this.API_URL}/${endpoint}`);
    }

    /**
     * Upload file with progress tracking
     */
    upload<T>(endpoint: string, file: File, additionalData?: any): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);

        if (additionalData) {
            Object.keys(additionalData).forEach(key => {
                formData.append(key, additionalData[key]);
            });
        }

        return this.http.post<T>(`${this.API_URL}/${endpoint}`, formData, {
            reportProgress: true,
            observe: 'events'
        });
    }

    /**
     * Upload multiple files
     */
    uploadMultiple<T>(endpoint: string, files: File[], additionalData?: any): Observable<any> {
        const formData = new FormData();

        files.forEach((file, index) => {
            formData.append(`files`, file);
        });

        if (additionalData) {
            Object.keys(additionalData).forEach(key => {
                formData.append(key, additionalData[key]);
            });
        }

        return this.http.post<T>(`${this.API_URL}/${endpoint}`, formData, {
            reportProgress: true,
            observe: 'events'
        });
    }

    /**
     * Download file
     */
    download(endpoint: string, filename: string): Observable<Blob> {
        return this.http.get(`${this.API_URL}/${endpoint}`, {
            responseType: 'blob'
        });
    }

    /**
     * Build HTTP params from object
     */
    private buildParams(params?: any): HttpParams {
        let httpParams = new HttpParams();

        if (params) {
            Object.keys(params).forEach(key => {
                if (params[key] !== null && params[key] !== undefined) {
                    httpParams = httpParams.set(key, params[key].toString());
                }
            });
        }

        return httpParams;
    }

    /**
     * Get full API URL
     */
    getApiUrl(): string {
        return this.API_URL;
    }
}
