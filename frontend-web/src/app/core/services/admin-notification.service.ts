import { Injectable, signal, computed, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subscription, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebSocketService, AdminNotification } from './websocket.service';

/**
 * Service for managing admin notifications
 * Combines REST API calls with real-time WebSocket updates
 */
@Injectable({
    providedIn: 'root'
})
export class AdminNotificationService implements OnDestroy {
    private apiUrl = `${environment.apiUrl}/admin/notifications`;
    private wsSubscription: Subscription | null = null;

    // State signals
    notifications = signal<AdminNotification[]>([]);
    loading = signal<boolean>(false);

    // Computed values
    unreadCount = computed(() =>
        this.notifications().filter(n => !n.isRead).length
    );

    unreadNotifications = computed(() =>
        this.notifications().filter(n => !n.isRead)
    );

    recentNotifications = computed(() =>
        this.notifications().slice(0, 10)
    );

    constructor(
        private http: HttpClient,
        private webSocketService: WebSocketService
    ) { }

    /**
     * Initialize the notification service
     * Call this when admin logs in or admin page loads
     */
    initialize(): void {
        // Load existing notifications
        this.loadNotifications();

        // Connect to WebSocket and subscribe to real-time updates
        this.webSocketService.connect().subscribe(connected => {
            if (connected) {
                this.subscribeToRealTimeUpdates();
            }
        });
    }

    /**
     * Subscribe to real-time notification updates
     */
    private subscribeToRealTimeUpdates(): void {
        if (this.wsSubscription) {
            this.wsSubscription.unsubscribe();
        }

        this.wsSubscription = this.webSocketService
            .subscribeToAdminNotifications()
            .subscribe(notification => {
                // Add new notification to the top of the list
                this.notifications.update(notifications => [notification, ...notifications]);

                // Play notification sound (optional)
                this.playNotificationSound();
            });
    }

    /**
     * Load all notifications from API
     */
    loadNotifications(): void {
        this.loading.set(true);
        this.http.get<AdminNotification[]>(this.apiUrl).subscribe({
            next: (notifications) => {
                this.notifications.set(notifications);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading notifications:', error);
                this.loading.set(false);
            }
        });
    }

    /**
     * Get unread count from API
     */
    getUnreadCount(): Observable<{ count: number }> {
        return this.http.get<{ count: number }>(`${this.apiUrl}/unread/count`);
    }

    /**
     * Mark a notification as read
     */
    markAsRead(notificationId: string): void {
        this.http.put(`${this.apiUrl}/${notificationId}/read`, {}).subscribe({
            next: () => {
                this.notifications.update(notifications =>
                    notifications.map(n =>
                        n.id === notificationId ? { ...n, isRead: true } : n
                    )
                );
            },
            error: (error) => console.error('Error marking notification as read:', error)
        });
    }

    /**
     * Mark all notifications as read
     */
    markAllAsRead(): void {
        this.http.put(`${this.apiUrl}/read-all`, {}).subscribe({
            next: () => {
                this.notifications.update(notifications =>
                    notifications.map(n => ({ ...n, isRead: true }))
                );
            },
            error: (error) => console.error('Error marking all as read:', error)
        });
    }

    /**
     * Delete a notification
     */
    deleteNotification(notificationId: string): void {
        this.http.delete(`${this.apiUrl}/${notificationId}`).subscribe({
            next: () => {
                this.notifications.update(notifications =>
                    notifications.filter(n => n.id !== notificationId)
                );
            },
            error: (error) => console.error('Error deleting notification:', error)
        });
    }

    /**
     * Get icon for notification type
     */
    getNotificationIcon(type: string): string {
        const icons: { [key: string]: string } = {
            NEW_ORDER: 'shopping_cart',
            ORDER_CANCELLED: 'cancel',
            LOW_STOCK: 'inventory',
            OUT_OF_STOCK: 'remove_shopping_cart',
            NEW_USER: 'person_add',
            DELIVERY_COMPLETED: 'local_shipping',
            DELIVERY_FAILED: 'error',
            PAYMENT_RECEIVED: 'payments',
            SYSTEM_ALERT: 'warning'
        };
        return icons[type] || 'notifications';
    }

    /**
     * Get color for notification type
     */
    getNotificationColor(type: string): string {
        const colors: { [key: string]: string } = {
            NEW_ORDER: '#10b981',      // Green
            ORDER_CANCELLED: '#ef4444', // Red
            LOW_STOCK: '#f59e0b',       // Yellow
            OUT_OF_STOCK: '#ef4444',    // Red
            NEW_USER: '#3b82f6',        // Blue
            DELIVERY_COMPLETED: '#10b981', // Green
            DELIVERY_FAILED: '#ef4444', // Red
            PAYMENT_RECEIVED: '#10b981', // Green
            SYSTEM_ALERT: '#f59e0b'     // Yellow
        };
        return colors[type] || '#6b7280';
    }

    /**
     * Play notification sound
     */
    private playNotificationSound(): void {
        try {
            const audio = new Audio('/assets/sounds/notification.mp3');
            audio.volume = 0.3;
            audio.play().catch(() => {
                // Ignore error if sound can't play
            });
        } catch (e) {
            // Ignore
        }
    }

    /**
     * Cleanup on destroy
     */
    ngOnDestroy(): void {
        if (this.wsSubscription) {
            this.wsSubscription.unsubscribe();
        }
    }
}
