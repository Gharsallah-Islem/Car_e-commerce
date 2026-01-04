import { Component, OnInit, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTabsModule } from '@angular/material/tabs';

import { DriverService, Driver, DriverStats } from '../../../core/services/driver.service';
import { DeliveryService, Delivery } from '../../../core/services/delivery.service';

@Component({
    selector: 'app-driver-management',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatTableModule,
        MatPaginatorModule,
        MatProgressSpinnerModule,
        MatChipsModule,
        MatInputModule,
        MatFormFieldModule,
        MatSelectModule,
        MatDialogModule,
        MatMenuModule,
        MatSnackBarModule,
        MatTooltipModule,
        MatTabsModule
    ],
    templateUrl: './driver-management.component.html',
    styleUrls: ['./driver-management.component.scss']
})
export class DriverManagementComponent implements OnInit {
    @ViewChild(MatPaginator) paginator!: MatPaginator;

    // State
    drivers = signal<Driver[]>([]);
    availableDrivers = signal<Driver[]>([]);
    pendingDeliveries = signal<Delivery[]>([]);
    stats = signal<DriverStats | null>(null);
    loading = signal<boolean>(true);
    searchQuery = signal<string>('');
    selectedTab = signal<number>(0);

    // Pagination
    totalDrivers = 0;
    pageSize = 10;
    currentPage = 0;

    // Table columns
    displayedColumns: string[] = ['driver', 'vehicle', 'status', 'rating', 'deliveries', 'actions'];

    constructor(
        private driverService: DriverService,
        private deliveryService: DeliveryService,
        private snackBar: MatSnackBar,
        private dialog: MatDialog
    ) { }

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        this.loading.set(true);

        // Load drivers
        this.driverService.getAllDrivers(this.currentPage, this.pageSize).subscribe({
            next: (page) => {
                this.drivers.set(page.content);
                this.totalDrivers = page.totalElements;
                this.loading.set(false);
            },
            error: (err) => {
                console.error('Error loading drivers:', err);
                this.loading.set(false);
                this.snackBar.open('Erreur de chargement', 'OK', { duration: 3000 });
            }
        });

        // Load stats
        this.driverService.getStatistics().subscribe({
            next: (stats) => this.stats.set(stats),
            error: (err) => console.error('Error loading stats:', err)
        });

        // Load available drivers
        this.driverService.getAvailableDrivers().subscribe({
            next: (drivers) => this.availableDrivers.set(drivers),
            error: (err) => console.error('Error loading available drivers:', err)
        });

        // Load pending deliveries
        this.deliveryService.getPendingDeliveries(0, 20).subscribe({
            next: (page) => this.pendingDeliveries.set(page.content),
            error: (err) => console.error('Error loading deliveries:', err)
        });
    }

    onPageChange(event: PageEvent): void {
        this.currentPage = event.pageIndex;
        this.pageSize = event.pageSize;
        this.loadData();
    }

    search(): void {
        if (!this.searchQuery()) {
            this.loadData();
            return;
        }

        this.loading.set(true);
        this.driverService.searchDrivers(this.searchQuery(), 0, this.pageSize).subscribe({
            next: (page) => {
                this.drivers.set(page.content);
                this.totalDrivers = page.totalElements;
                this.loading.set(false);
            },
            error: (err) => {
                this.loading.set(false);
                this.snackBar.open('Erreur de recherche', 'OK', { duration: 3000 });
            }
        });
    }

    // ==================== DRIVER ACTIONS ====================

    verifyDriver(driver: Driver): void {
        if (!confirm(`Vérifier le livreur ${driver.user.fullName} ?`)) return;

        this.driverService.verifyDriver(driver.id).subscribe({
            next: () => {
                this.snackBar.open('Livreur vérifié', 'OK', { duration: 3000 });
                this.loadData();
            },
            error: (err) => {
                this.snackBar.open('Erreur: ' + (err.error?.message || 'Échec'), 'OK', { duration: 3000 });
            }
        });
    }

    suspendDriver(driver: Driver): void {
        if (!confirm(`Suspendre le livreur ${driver.user.fullName} ?`)) return;

        this.driverService.suspendDriver(driver.id).subscribe({
            next: () => {
                this.snackBar.open('Livreur suspendu', 'OK', { duration: 3000 });
                this.loadData();
            },
            error: (err) => {
                this.snackBar.open('Erreur: ' + (err.error?.message || 'Échec'), 'OK', { duration: 3000 });
            }
        });
    }

    reactivateDriver(driver: Driver): void {
        if (!confirm(`Réactiver le livreur ${driver.user.fullName} ?`)) return;

        this.driverService.reactivateDriver(driver.id).subscribe({
            next: () => {
                this.snackBar.open('Livreur réactivé', 'OK', { duration: 3000 });
                this.loadData();
            },
            error: (err) => {
                this.snackBar.open('Erreur: ' + (err.error?.message || 'Échec'), 'OK', { duration: 3000 });
            }
        });
    }

    // ==================== DELIVERY ASSIGNMENT ====================

    assignDelivery(driverId: string, deliveryId: string): void {
        this.driverService.assignDelivery(driverId, deliveryId).subscribe({
            next: () => {
                this.snackBar.open('Livraison assignée', 'OK', { duration: 3000 });
                this.loadData();
            },
            error: (err) => {
                this.snackBar.open('Erreur: ' + (err.error?.message || 'Échec'), 'OK', { duration: 3000 });
            }
        });
    }

    findNearestDriver(delivery: Delivery): void {
        // Default to Tunisia coordinates if no address geocoding
        const lat = 36.8065;
        const lng = 10.1815;

        this.driverService.findNearestDriver(lat, lng).subscribe({
            next: (driver) => {
                if (confirm(`Assigner à ${driver.user.fullName} (le plus proche) ?`)) {
                    this.assignDelivery(driver.id, delivery.id);
                }
            },
            error: (err) => {
                this.snackBar.open('Aucun livreur disponible', 'OK', { duration: 3000 });
            }
        });
    }

    // ==================== HELPERS ====================

    getStatusColor(driver: Driver): string {
        if (!driver.isVerified) return 'warn';
        if (!driver.isActive) return 'accent';
        if (driver.isAvailable) return 'primary';
        return 'default';
    }

    getStatusLabel(driver: Driver): string {
        if (!driver.isVerified) return 'En attente';
        if (!driver.isActive) return 'Suspendu';
        if (driver.currentDelivery) return 'En livraison';
        if (driver.isAvailable) return 'En ligne';
        return 'Hors ligne';
    }

    getVehicleIcon(vehicleType: string): string {
        const icons: { [key: string]: string } = {
            'CAR': 'directions_car',
            'MOTORCYCLE': 'two_wheeler',
            'BICYCLE': 'directions_bike',
            'VAN': 'airport_shuttle'
        };
        return icons[vehicleType] || 'local_shipping';
    }

    refresh(): void {
        this.loadData();
    }
}
