import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';
import { StorageService } from '../services/storage.service';

/**
 * Error Interceptor
 * Handles HTTP errors globally
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const notificationService = inject(NotificationService);
    const router = inject(Router);
    const storageService = inject(StorageService);

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            let errorMessage = 'An error occurred';

            if (error.error instanceof ErrorEvent) {
                // Client-side error
                errorMessage = `Error: ${error.error.message}`;
            } else {
                // Server-side error
                switch (error.status) {
                    case 0:
                        errorMessage = 'No connection. Please check your internet.';
                        break;
                    case 400:
                        errorMessage = error.error?.message || 'Bad request';
                        break;
                    case 401:
                        errorMessage = 'Unauthorized. Please login again.';
                        storageService.clearAll();
                        router.navigate(['/auth/login']);
                        break;
                    case 403:
                        errorMessage = 'Access denied. You don\'t have permission.';
                        break;
                    case 404:
                        errorMessage = 'Resource not found';
                        break;
                    case 500:
                        errorMessage = 'Server error. Please try again later.';
                        break;
                    case 503:
                        errorMessage = 'Service unavailable. Please try again later.';
                        break;
                    default:
                        errorMessage = error.error?.message || `Error: ${error.status}`;
                }
            }

            // Don't show notification for silent errors
            if (!req.headers.has('X-Silent-Error')) {
                notificationService.error(errorMessage);
            }

            return throwError(() => error);
        })
    );
};
