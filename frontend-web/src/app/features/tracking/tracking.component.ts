import { Component, OnInit, OnDestroy, signal, computed, AfterViewInit, ElementRef, ViewChild, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { Subscription } from 'rxjs';

import { DeliveryService, Delivery } from '../../core/services/delivery.service';
import { WebSocketService, LocationUpdate } from '../../core/services/websocket.service';
import { NotificationService } from '../../core/services/notification.service';

import * as L from 'leaflet';

@Component({
    selector: 'app-tracking',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatChipsModule,
        MatDividerModule
    ],
    templateUrl: './tracking.component.html',
    styleUrls: ['./tracking.component.scss']
})
export class TrackingComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('mapContainer') mapContainer!: ElementRef;

    // State
    delivery = signal<Delivery | null>(null);
    loading = signal<boolean>(true);
    error = signal<string | null>(null);
    trackingNumber = signal<string>('');
    isSheetExpanded = false;
    isSheetMinimized = false;
    wsConnected = signal<boolean>(false);

    // Map state
    private map: L.Map | null = null;
    private driverMarker: L.Marker | null = null;
    private destinationMarker: L.Marker | null = null;
    private routeLine: L.Polyline | null = null;
    private fullRouteWaypoints: [number, number][] = []; // Store full route for drawing

    // Subscriptions
    private wsSubscription: Subscription | null = null;
    private routeSubscription: Subscription | null = null;

    // Computed
    statusSteps = computed(() => {
        const d = this.delivery();
        if (!d) return [];

        const allSteps = [
            { key: 'PROCESSING', label: 'Commande préparée', icon: 'inventory_2' },
            { key: 'IN_TRANSIT', label: 'En route', icon: 'local_shipping' },
            { key: 'OUT_FOR_DELIVERY', label: 'Livraison en cours', icon: 'delivery_dining' },
            { key: 'DELIVERED', label: 'Livrée', icon: 'check_circle' }
        ];

        const currentIndex = allSteps.findIndex(s => s.key === d.status);
        return allSteps.map((step, index) => ({
            ...step,
            completed: index < currentIndex,
            current: index === currentIndex,
            pending: index > currentIndex
        }));
    });

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private deliveryService: DeliveryService,
        private wsService: WebSocketService,
        private notificationService: NotificationService
    ) { }

    ngOnInit(): void {
        this.routeSubscription = this.route.paramMap.subscribe(params => {
            const trackingNum = params.get('trackingNumber');
            if (trackingNum) {
                this.trackingNumber.set(trackingNum);
                this.loadDelivery(trackingNum);
            } else {
                this.error.set('Numéro de suivi non fourni');
                this.loading.set(false);
            }
        });
    }

    ngAfterViewInit(): void {
        // Initialize map eagerly - container is always rendered now
        setTimeout(() => {
            this.initializeMap();
        }, 100);
    }

    ngOnDestroy(): void {
        this.wsSubscription?.unsubscribe();
        this.routeSubscription?.unsubscribe();
        this.wsService.disconnect();

        if (this.map) {
            this.map.remove();
        }
    }

    // Handle window resize to fix map tiles
    @HostListener('window:resize', ['$event'])
    onWindowResize(): void {
        if (this.map) {
            setTimeout(() => {
                this.map?.invalidateSize();
            }, 100);
        }
    }

    private loadDelivery(trackingNumber: string): void {
        this.loading.set(true);
        this.deliveryService.trackDelivery(trackingNumber).subscribe({
            next: (delivery) => {
                this.delivery.set(delivery);
                this.loading.set(false);

                // Connect WebSocket after delivery loads
                setTimeout(() => {
                    this.connectWebSocket();
                }, 300);
            },
            error: (err) => {
                console.error('Error loading delivery:', err);
                this.error.set('Livraison non trouvée');
                this.loading.set(false);
            }
        });
    }

    private initializeMap(): void {
        if (!this.mapContainer?.nativeElement) return;

        // Default center (Tunisia)
        const defaultLat = 36.8065;
        const defaultLng = 10.1815;

        // Fix Leaflet default icon issue
        delete (L.Icon.Default.prototype as any)._getIconUrl;
        L.Icon.Default.mergeOptions({
            iconRetinaUrl: 'assets/marker-icon-2x.png',
            iconUrl: 'assets/marker-icon.png',
            shadowUrl: 'assets/marker-shadow.png',
        });

        this.map = L.map(this.mapContainer.nativeElement, {
            center: [defaultLat, defaultLng],
            zoom: 13,
            zoomControl: true
        });

        // Add OpenStreetMap tiles
        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
            maxZoom: 19,
            tileSize: 256
        }).addTo(this.map);

        // Force map to recalculate size multiple times (fixes tile gaps)
        // This is needed because Angular may still be rendering content
        const refreshMap = () => {
            if (this.map) {
                this.map.invalidateSize({ animate: false });
            }
        };

        // Immediate calls
        setTimeout(refreshMap, 100);
        setTimeout(refreshMap, 300);
        setTimeout(refreshMap, 500);
        setTimeout(refreshMap, 1000);

        // Continuous refresh for first few seconds
        let refreshCount = 0;
        const refreshInterval = setInterval(() => {
            refreshCount++;
            refreshMap();
            if (refreshCount >= 5) {
                clearInterval(refreshInterval);
            }
        }, 2000);

        // Custom icons
        const driverIcon = L.divIcon({
            className: 'driver-marker',
            html: `<div class="marker-pin driver"><mat-icon>local_shipping</mat-icon></div>`,
            iconSize: [40, 40],
            iconAnchor: [20, 40]
        });

        const destinationIcon = L.divIcon({
            className: 'destination-marker',
            html: `<div class="marker-pin destination"><mat-icon>home</mat-icon></div>`,
            iconSize: [40, 40],
            iconAnchor: [20, 40]
        });

        // Add destination marker if we have address coordinates
        // For now, use a placeholder - in production, geocode the address
        this.destinationMarker = L.marker([defaultLat, defaultLng], {
            icon: destinationIcon
        }).addTo(this.map);
        this.destinationMarker.bindPopup('Destination de livraison');

        // Initialize driver marker (will be updated via WebSocket)
        const delivery = this.delivery();
        if (delivery?.currentLatitude && delivery?.currentLongitude) {
            this.updateDriverPosition(delivery.currentLatitude, delivery.currentLongitude);
        }
    }

    private connectWebSocket(): void {
        console.log('[Tracking] Connecting to WebSocket...');
        this.wsService.connect().subscribe(connected => {
            console.log('[Tracking] WebSocket connected:', connected);
            this.wsConnected.set(connected);

            if (connected) {
                const delivery = this.delivery();
                if (delivery) {
                    console.log('[Tracking] Subscribing to delivery location:', delivery.id);
                    this.wsSubscription = this.wsService
                        .subscribeToDeliveryLocation(delivery.id)
                        .subscribe({
                            next: (location: any) => {
                                console.log('[Tracking] Received location update:', location);

                                // Handle both direct lat/lng and nested structure
                                const lat = location.latitude ?? location.lat;
                                const lng = location.longitude ?? location.lng;

                                // Check if this message contains route waypoints (initial broadcast)
                                if (location.routeWaypoints && Array.isArray(location.routeWaypoints)) {
                                    console.log('[Tracking] Received route with', location.routeWaypoints.length, 'waypoints');
                                    this.fullRouteWaypoints = location.routeWaypoints.map(
                                        (wp: number[]) => [wp[0], wp[1]] as [number, number]
                                    );
                                    this.drawFullRoute();
                                }

                                if (lat && lng) {
                                    this.updateDriverPosition(lat, lng);
                                }

                                // Update destination if provided
                                if (location.destinationLatitude && location.destinationLongitude) {
                                    this.updateDestinationMarker(
                                        location.destinationLatitude,
                                        location.destinationLongitude
                                    );
                                }
                            },
                            error: (err) => console.error('[Tracking] WebSocket error:', err)
                        });
                }
            }
        });
    }

    /**
     * Draw the full route polyline using waypoints from backend
     */
    private drawFullRoute(): void {
        if (!this.map || this.fullRouteWaypoints.length < 2) return;

        console.log('[Tracking] Drawing full route with', this.fullRouteWaypoints.length, 'points');

        // Remove existing route line if any
        if (this.routeLine) {
            this.routeLine.remove();
        }

        // Draw the full route polyline
        this.routeLine = L.polyline(this.fullRouteWaypoints, {
            color: '#007AFF',
            weight: 5,
            opacity: 0.8,
            lineJoin: 'round',
            lineCap: 'round'
        }).addTo(this.map);

        // Fit map to show entire route
        this.map.fitBounds(this.routeLine.getBounds(), { padding: [50, 50] });
    }

    private updateDestinationMarker(lat: number, lng: number): void {
        if (!this.map) return;

        if (this.destinationMarker) {
            this.destinationMarker.setLatLng([lat, lng]);
        }
    }

    private updateDriverPosition(lat: number, lng: number): void {
        if (!this.map) return;

        const driverIcon = L.divIcon({
            className: 'driver-marker',
            html: `
                <div class="driver-marker-container">
                    <div class="driver-pulse"></div>
                    <div class="driver-icon">🚚</div>
                </div>
            `,
            iconSize: [50, 50],
            iconAnchor: [25, 25]
        });

        if (this.driverMarker) {
            this.driverMarker.setLatLng([lat, lng]);
        } else {
            this.driverMarker = L.marker([lat, lng], {
                icon: driverIcon
            }).addTo(this.map);
            this.driverMarker.bindPopup('Votre livreur');
        }

        // Pan map to follow driver (smooth)
        this.map.panTo([lat, lng], { animate: true, duration: 0.5 });

        // If we have full route waypoints, update remaining route
        if (this.fullRouteWaypoints.length > 0 && this.routeLine) {
            // Find nearest waypoint to current driver position
            let nearestIndex = 0;
            let minDist = Infinity;

            for (let i = 0; i < this.fullRouteWaypoints.length; i++) {
                const wp = this.fullRouteWaypoints[i];
                const dist = Math.pow(wp[0] - lat, 2) + Math.pow(wp[1] - lng, 2);
                if (dist < minDist) {
                    minDist = dist;
                    nearestIndex = i;
                }
            }

            // Update route to show remaining path from driver position
            const remainingRoute: [number, number][] = [[lat, lng], ...this.fullRouteWaypoints.slice(nearestIndex + 1)];
            this.routeLine.setLatLngs(remainingRoute);
        } else if (this.destinationMarker && !this.routeLine) {
            // Fallback: draw straight line if no waypoints
            const destLatLng = this.destinationMarker.getLatLng();
            this.routeLine = L.polyline([[lat, lng], destLatLng], {
                color: '#007AFF',
                weight: 4,
                opacity: 0.7,
                dashArray: '10, 10'
            }).addTo(this.map);
        }
    }

    getStatusColor(status: string): string {
        const colors: { [key: string]: string } = {
            'PROCESSING': 'accent',
            'IN_TRANSIT': 'primary',
            'OUT_FOR_DELIVERY': 'primary',
            'DELIVERED': 'success',
            'FAILED': 'warn'
        };
        return colors[status] || 'default';
    }

    getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            'PROCESSING': 'En préparation',
            'IN_TRANSIT': 'En transit',
            'OUT_FOR_DELIVERY': 'En livraison',
            'DELIVERED': 'Livrée',
            'FAILED': 'Échec'
        };
        return labels[status] || status;
    }

    refresh(): void {
        const trackingNum = this.trackingNumber();
        if (trackingNum) {
            this.loadDelivery(trackingNum);
        }
    }

    goBack(): void {
        this.router.navigate(['/']);
    }

    toggleSheet(): void {
        // 3-state toggle: minimized -> normal -> expanded -> minimized
        if (this.isSheetMinimized) {
            // minimized -> normal
            this.isSheetMinimized = false;
            this.isSheetExpanded = false;
        } else if (!this.isSheetExpanded) {
            // normal -> minimized to show full map
            this.isSheetMinimized = true;
            this.isSheetExpanded = false;
        } else {
            // expanded -> normal
            this.isSheetExpanded = false;
        }

        // Invalidate map size after animation
        setTimeout(() => {
            this.map?.invalidateSize();
        }, 600);
    }

    getProgressPercent(): number {
        const steps = this.statusSteps();
        if (!steps.length) return 0;

        const currentIndex = steps.findIndex(s => s.current);
        const completedIndex = steps.reduce((acc, step, index) =>
            step.completed ? index : acc, -1);

        const activeIndex = currentIndex >= 0 ? currentIndex : completedIndex + 1;
        return Math.min((activeIndex / (steps.length - 1)) * 100, 100);
    }
}
