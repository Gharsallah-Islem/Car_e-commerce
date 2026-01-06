import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export interface LocationUpdate {
    driverId: string;
    latitude: number;
    longitude: number;
    speed: number | null;
    heading: number | null;
    driverName: string;
}

export interface DeliveryStatusUpdate {
    deliveryId: string;
    status: string;
    message: string;
    timestamp: number;
}

export interface AdminNotification {
    id: string;
    type: string;
    title: string;
    message: string;
    data?: string;
    isRead: boolean;
    referenceId?: string;
    icon?: string;
    actionUrl?: string;
    createdAt: string;
}

@Injectable({
    providedIn: 'root'
})
export class WebSocketService implements OnDestroy {
    private client: Client | null = null;
    private subscriptions: Map<string, StompSubscription> = new Map();

    private connectionStatus$ = new BehaviorSubject<boolean>(false);
    private locationUpdates$ = new Subject<LocationUpdate>();
    private statusUpdates$ = new Subject<DeliveryStatusUpdate>();
    private notificationUpdates$ = new Subject<AdminNotification>();

    constructor(private authService: AuthService) { }

    /**
     * Connect to WebSocket server
     */
    connect(): Observable<boolean> {
        if (this.client?.connected) {
            return this.connectionStatus$.asObservable();
        }

        const wsUrl = environment.apiUrl.replace('/api', '') + '/ws';

        this.client = new Client({
            webSocketFactory: () => new SockJS(wsUrl),
            debug: (str) => {
                if (!environment.production) {
                    console.log('[WebSocket]', str);
                }
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        this.client.onConnect = () => {
            console.log('WebSocket connected');
            this.connectionStatus$.next(true);
        };

        this.client.onDisconnect = () => {
            console.log('WebSocket disconnected');
            this.connectionStatus$.next(false);
        };

        this.client.onStompError = (frame) => {
            console.error('WebSocket error:', frame);
            this.connectionStatus$.next(false);
        };

        this.client.activate();
        return this.connectionStatus$.asObservable();
    }

    /**
     * Disconnect from WebSocket
     */
    disconnect(): void {
        this.subscriptions.forEach(sub => sub.unsubscribe());
        this.subscriptions.clear();

        if (this.client) {
            this.client.deactivate();
            this.client = null;
        }
        this.connectionStatus$.next(false);
    }

    /**
     * Subscribe to delivery location updates
     */
    subscribeToDeliveryLocation(deliveryId: string): Observable<LocationUpdate> {
        const topic = `/topic/delivery/${deliveryId}/location`;

        if (!this.subscriptions.has(topic) && this.client?.connected) {
            const subscription = this.client.subscribe(topic, (message: IMessage) => {
                try {
                    const location: LocationUpdate = JSON.parse(message.body);
                    this.locationUpdates$.next(location);
                } catch (e) {
                    console.error('Error parsing location update:', e);
                }
            });
            this.subscriptions.set(topic, subscription);
        }

        return this.locationUpdates$.asObservable();
    }

    /**
     * Subscribe to delivery status updates
     */
    subscribeToDeliveryStatus(deliveryId: string): Observable<DeliveryStatusUpdate> {
        const topic = `/topic/delivery/${deliveryId}/status`;

        if (!this.subscriptions.has(topic) && this.client?.connected) {
            const subscription = this.client.subscribe(topic, (message: IMessage) => {
                try {
                    const status: DeliveryStatusUpdate = JSON.parse(message.body);
                    this.statusUpdates$.next(status);
                } catch (e) {
                    console.error('Error parsing status update:', e);
                }
            });
            this.subscriptions.set(topic, subscription);
        }

        return this.statusUpdates$.asObservable();
    }

    /**
     * Unsubscribe from a topic
     */
    unsubscribe(topic: string): void {
        const subscription = this.subscriptions.get(topic);
        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(topic);
        }
    }

    /**
     * Send location update (for drivers)
     */
    sendLocationUpdate(driverId: string, location: {
        latitude: number;
        longitude: number;
        speed?: number;
        heading?: number;
        deliveryId?: string;
    }): void {
        if (this.client?.connected) {
            this.client.publish({
                destination: `/app/driver/${driverId}/location`,
                body: JSON.stringify(location)
            });
        }
    }

    /**
     * Subscribe to admin notifications (real-time)
     */
    subscribeToAdminNotifications(): Observable<AdminNotification> {
        const topic = '/topic/admin/notifications';

        if (!this.subscriptions.has(topic) && this.client?.connected) {
            const subscription = this.client.subscribe(topic, (message: IMessage) => {
                try {
                    const notification: AdminNotification = JSON.parse(message.body);
                    this.notificationUpdates$.next(notification);
                } catch (e) {
                    console.error('Error parsing admin notification:', e);
                }
            });
            this.subscriptions.set(topic, subscription);
        }

        return this.notificationUpdates$.asObservable();
    }

    /**
     * Get notification updates observable
     */
    get notificationUpdates(): Observable<AdminNotification> {
        return this.notificationUpdates$.asObservable();
    }

    /**
     * Get connection status
     */
    get isConnected(): boolean {
        return this.client?.connected ?? false;
    }

    get connectionStatus(): Observable<boolean> {
        return this.connectionStatus$.asObservable();
    }

    ngOnDestroy(): void {
        this.disconnect();
    }
}
