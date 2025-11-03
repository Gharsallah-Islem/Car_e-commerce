import { Injectable, signal } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface Notification {
    message: string;
    type: NotificationType;
    duration?: number;
}

/**
 * Service for displaying toast notifications
 */
@Injectable({
    providedIn: 'root'
})
export class NotificationService {
    // Signal for programmatic notification access
    currentNotification = signal<Notification | null>(null);

    constructor(private snackBar: MatSnackBar) { }

    /**
     * Show success notification
     */
    success(message: string, duration: number = 3000): void {
        this.show(message, 'success', duration);
    }

    /**
     * Show error notification
     */
    error(message: string, duration: number = 5000): void {
        this.show(message, 'error', duration);
    }

    /**
     * Show warning notification
     */
    warning(message: string, duration: number = 4000): void {
        this.show(message, 'warning', duration);
    }

    /**
     * Show info notification
     */
    info(message: string, duration: number = 3000): void {
        this.show(message, 'info', duration);
    }

    /**
     * Show notification with custom configuration
     */
    private show(message: string, type: NotificationType, duration: number): void {
        const notification: Notification = { message, type, duration };
        this.currentNotification.set(notification);

        this.snackBar.open(message, 'Close', {
            duration,
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: [`notification-${type}`]
        });
    }

    /**
     * Dismiss current notification
     */
    dismiss(): void {
        this.snackBar.dismiss();
        this.currentNotification.set(null);
    }
}
