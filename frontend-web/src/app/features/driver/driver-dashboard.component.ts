import { Component, OnInit, OnDestroy, signal, computed, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { interval, Subscription } from 'rxjs';

import { DriverService, Driver } from '../../core/services/driver.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-driver-dashboard',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatSlideToggleModule,
        MatChipsModule,
        MatDividerModule,
        MatSnackBarModule
    ],
    templateUrl: './driver-dashboard.component.html',
    styleUrls: ['./driver-dashboard.component.scss']
})
export class DriverDashboardComponent implements OnInit, OnDestroy {
    // State
    driver = signal<Driver | null>(null);
    loading = signal<boolean>(true);
    error = signal<string | null>(null);
    isOnline = signal<boolean>(false);
    isSendingLocation = signal<boolean>(false);
    lastLocationUpdate = signal<Date | null>(null);

    // GPS tracking
    private locationWatchId: number | null = null;
    private locationInterval: Subscription | null = null;
    private currentPosition: GeolocationPosition | null = null;

    // Computed
    hasCurrentDelivery = computed(() => !!this.driver()?.currentDelivery);

    currentDelivery = computed(() => this.driver()?.currentDelivery);

    statusColor = computed(() => {
        return this.isOnline() ? 'primary' : 'warn';
    });

    constructor(
        private driverService: DriverService,
        private wsService: WebSocketService,
        private authService: AuthService,
        private snackBar: MatSnackBar,
        private router: Router,
        private ngZone: NgZone
    ) { }

    ngOnInit(): void {
        this.loadDriver();
    }

    ngOnDestroy(): void {
        this.stopLocationTracking();
        this.wsService.disconnect();
    }

    private loadDriver(): void {
        this.loading.set(true);
        this.driverService.getCurrentDriver().subscribe({
            next: (driver) => {
                this.driver.set(driver);
                this.isOnline.set(driver.isAvailable);
                this.loading.set(false);

                // If online, start location tracking
                if (driver.isAvailable) {
                    this.startLocationTracking();
                }

                // Connect WebSocket for notifications
                this.wsService.connect();
            },
            error: (err) => {
                console.error('Error loading driver:', err);
                if (err.status === 404) {
                    // User is not registered as a driver
                    this.error.set('NOT_DRIVER');
                } else {
                    this.error.set('Erreur de chargement');
                }
                this.loading.set(false);
            }
        });
    }

    toggleOnlineStatus(): void {
        const newStatus = !this.isOnline();
        this.isOnline.set(newStatus);

        if (newStatus) {
            this.driverService.goOnline().subscribe({
                next: (driver) => {
                    this.driver.set(driver);
                    this.startLocationTracking();
                    this.snackBar.open('Vous êtes maintenant en ligne', 'OK', { duration: 3000 });
                },
                error: (err) => {
                    this.isOnline.set(false);
                    this.snackBar.open(err.error?.message || 'Erreur', 'OK', { duration: 3000 });
                }
            });
        } else {
            this.driverService.goOffline().subscribe({
                next: (driver) => {
                    this.driver.set(driver);
                    this.stopLocationTracking();
                    this.snackBar.open('Vous êtes maintenant hors ligne', 'OK', { duration: 3000 });
                },
                error: () => {
                    this.isOnline.set(true);
                }
            });
        }
    }

    // ==================== GPS TRACKING ====================

    private startLocationTracking(): void {
        if (!navigator.geolocation) {
            this.snackBar.open('Géolocalisation non supportée', 'OK', { duration: 3000 });
            return;
        }

        // Watch position changes
        this.locationWatchId = navigator.geolocation.watchPosition(
            (position) => {
                this.ngZone.run(() => {
                    this.currentPosition = position;
                });
            },
            (error) => {
                console.error('Geolocation error:', error);
                this.snackBar.open('Erreur GPS: ' + error.message, 'OK', { duration: 5000 });
            },
            {
                enableHighAccuracy: true,
                maximumAge: 5000,
                timeout: 10000
            }
        );

        // Send location every 10 seconds
        this.locationInterval = interval(10000).subscribe(() => {
            this.sendCurrentLocation();
        });

        // Send initial location
        navigator.geolocation.getCurrentPosition(
            (position) => {
                this.currentPosition = position;
                this.sendCurrentLocation();
            },
            (error) => console.error('Initial position error:', error),
            { enableHighAccuracy: true }
        );
    }

    private stopLocationTracking(): void {
        if (this.locationWatchId !== null) {
            navigator.geolocation.clearWatch(this.locationWatchId);
            this.locationWatchId = null;
        }

        if (this.locationInterval) {
            this.locationInterval.unsubscribe();
            this.locationInterval = null;
        }
    }

    private sendCurrentLocation(): void {
        if (!this.currentPosition || !this.isOnline()) return;

        const driver = this.driver();
        if (!driver) return;

        this.isSendingLocation.set(true);

        const locationData = {
            latitude: this.currentPosition.coords.latitude,
            longitude: this.currentPosition.coords.longitude,
            speed: this.currentPosition.coords.speed || undefined,
            heading: this.currentPosition.coords.heading || undefined,
            accuracy: this.currentPosition.coords.accuracy,
            deliveryId: driver.currentDelivery?.id
        };

        // Send via HTTP (more reliable)
        this.driverService.updateLocation(locationData).subscribe({
            next: (updatedDriver) => {
                this.driver.set(updatedDriver);
                this.lastLocationUpdate.set(new Date());
                this.isSendingLocation.set(false);
            },
            error: (err) => {
                console.error('Location update error:', err);
                this.isSendingLocation.set(false);
            }
        });

        // Also send via WebSocket for real-time updates to customers
        if (this.wsService.isConnected) {
            this.wsService.sendLocationUpdate(driver.id, locationData);
        }
    }

    // ==================== DELIVERY ACTIONS ====================

    completeDelivery(): void {
        if (!confirm('Confirmer la livraison terminée ?')) return;

        this.loading.set(true);
        this.driverService.completeDelivery().subscribe({
            next: (driver) => {
                this.driver.set(driver);
                this.loading.set(false);
                this.snackBar.open('Livraison terminée avec succès!', 'OK', { duration: 3000 });
            },
            error: (err) => {
                this.loading.set(false);
                this.snackBar.open('Erreur: ' + (err.error?.message || 'Échec'), 'OK', { duration: 3000 });
            }
        });
    }

    viewDeliveryDetails(): void {
        const delivery = this.currentDelivery();
        if (delivery) {
            // Navigate to detailed view or show modal
            this.snackBar.open('Détails de la livraison', 'OK', { duration: 2000 });
        }
    }

    openNavigationApp(): void {
        const delivery = this.currentDelivery();
        if (delivery?.address) {
            const encoded = encodeURIComponent(delivery.address);
            window.open(`https://www.google.com/maps/search/?api=1&query=${encoded}`, '_blank');
        }
    }

    callCustomer(): void {
        const delivery = this.currentDelivery();
        if (delivery?.order?.user?.phone) {
            window.open(`tel:${delivery.order.user.phone}`);
        }
    }

    refresh(): void {
        this.loadDriver();
    }

    logout(): void {
        this.stopLocationTracking();
        this.authService.logout();
        this.router.navigate(['/']);
    }
}
