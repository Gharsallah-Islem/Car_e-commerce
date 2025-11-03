import { Injectable, signal } from '@angular/core';

/**
 * Loading Service
 * Manages global loading state
 */
@Injectable({
    providedIn: 'root'
})
export class LoadingService {
    private loadingCount = 0;
    public isLoading = signal<boolean>(false);

    /**
     * Show loading indicator
     */
    show(): void {
        this.loadingCount++;
        this.isLoading.set(true);
    }

    /**
     * Hide loading indicator
     */
    hide(): void {
        this.loadingCount--;

        if (this.loadingCount <= 0) {
            this.loadingCount = 0;
            this.isLoading.set(false);
        }
    }

    /**
     * Force hide loading indicator
     */
    forceHide(): void {
        this.loadingCount = 0;
        this.isLoading.set(false);
    }
}
