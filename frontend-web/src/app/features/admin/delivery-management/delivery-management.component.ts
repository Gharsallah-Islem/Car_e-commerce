import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';

import { NotificationService } from '../../../core/services/notification.service';
import { ExportService } from '../../../core/services/export.service';
import { DeliveryService, Delivery as DeliveryModel, DeliveryStats as DeliveryStatsModel } from '../../../core/services/delivery.service';
import { DeliveryDetailDialogComponent } from './delivery-detail-dialog/delivery-detail-dialog.component';

// Using types from DeliveryService
type Delivery = DeliveryModel;
type DeliveryStats = DeliveryStatsModel;

@Component({
    selector: 'app-delivery-management',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatTabsModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatInputModule,
        MatFormFieldModule,
        MatTableModule,
        MatChipsModule,
        MatDividerModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        MatTooltipModule,
        MatPaginatorModule,
        MatSortModule,
        MatMenuModule,
        MatBadgeModule,
        MatProgressBarModule,
        MatDialogModule
    ],
    templateUrl: './delivery-management.component.html',
    styleUrls: ['./delivery-management.component.scss']
})
export class DeliveryManagementComponent implements OnInit {
    // Forms
    trackingForm!: FormGroup;
    updateStatusForm!: FormGroup;

    // State
    loading = signal<boolean>(false);
    selectedTabIndex = signal<number>(0);

    // Stats
    stats = signal<DeliveryStats>({
        totalDeliveries: 0,
        processing: 0,
        inTransit: 0,
        outForDelivery: 0,
        delivered: 0,
        failed: 0,
        averageDeliveryTime: 0,
        onTimeRate: 0
    });

    // Deliveries - raw data
    deliveries = signal<Delivery[]>([]);

    // Tab 1: En Attente (PROCESSING - waiting for driver assignment) - FIFO oldest first
    get pendingDeliveries(): Delivery[] {
        return this.deliveries()
            .filter(d => d.status === 'PROCESSING')
            .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    }

    // Tab 2: En Cours (IN_TRANSIT, OUT_FOR_DELIVERY - actively being delivered) - FIFO oldest first
    get activeDeliveries(): Delivery[] {
        return this.deliveries()
            .filter(d => d.status === 'IN_TRANSIT' || d.status === 'OUT_FOR_DELIVERY')
            .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    }

    // Tab 3: Historique (DELIVERED - completed) - newest first for display
    get completedDeliveries(): Delivery[] {
        return this.deliveries()
            .filter(d => d.status === 'DELIVERED')
            .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    }

    // Tab 4: Échecs (FAILED, CANCELLED) - newest first
    get failedDeliveries(): Delivery[] {
        return this.deliveries()
            .filter(d => d.status === 'FAILED' || d.status === 'CANCELLED')
            .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    }

    deliveryColumns: string[] = ['trackingNumber', 'order', 'customer', 'address', 'status', 'courier', 'estimated', 'actions'];

    // Pagination
    pageSize = signal<number>(100);
    pageIndex = signal<number>(0);

    // Couriers
    couriers = ['Mohammed Ali', 'Ahmed Benali', 'Sara El Amrani', 'Youssef Alaoui'];

    // Sync loading state
    syncing = signal<boolean>(false);

    constructor(
        private fb: FormBuilder,
        private notificationService: NotificationService,
        private deliveryService: DeliveryService,
        private dialog: MatDialog,
        private router: Router,
        private exportService: ExportService
    ) {
        this.initForms();
    }

    ngOnInit(): void {
        this.loadDeliveryStats();
        this.loadDeliveries();
        this.syncConfirmedOrders(); // Auto-sync on load
    }


    initForms(): void {
        this.trackingForm = this.fb.group({
            trackingNumber: ['', [Validators.required]]
        });

        this.updateStatusForm = this.fb.group({
            deliveryId: ['', [Validators.required]],
            status: ['', [Validators.required]],
            courierName: [''],
            notes: ['']
        });
    }

    loadDeliveryStats(): void {
        this.loading.set(true);
        this.deliveryService.getStatistics().subscribe({
            next: (stats) => {
                this.stats.set(stats);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading delivery stats:', error);
                this.notificationService.error('Erreur lors du chargement des statistiques');
                this.loading.set(false);
            }
        });
    }

    loadDeliveries(): void {
        this.loading.set(true);
        const page = this.pageIndex();
        const size = this.pageSize();

        this.deliveryService.getAllDeliveries(page, size).subscribe({
            next: (response) => {
                this.deliveries.set(response.content);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading deliveries:', error);
                // Set empty array for graceful degradation
                this.deliveries.set([]);
                this.loading.set(false);
                // Only show error for non-404/500 errors
                if (error.status !== 404 && error.status !== 500) {
                    this.notificationService.error('Erreur lors du chargement des livraisons');
                }
            }
        });
    }

    /**
     * Sync confirmed orders - creates deliveries for any confirmed orders missing one
     */
    syncConfirmedOrders(): void {
        this.syncing.set(true);
        this.deliveryService.syncConfirmedOrders().subscribe({
            next: (response: any) => {
                this.syncing.set(false);
                if (response.deliveriesCreated > 0) {
                    this.notificationService.success(`${response.deliveriesCreated} nouvelle(s) livraison(s) créée(s)`);
                    this.loadDeliveries(); // Reload to show new deliveries
                }
            },
            error: (error) => {
                this.syncing.set(false);
                console.error('Error syncing confirmed orders:', error);
            }
        });
    }

    trackDelivery(): void {
        if (this.trackingForm.invalid) {
            this.notificationService.warning('Veuillez entrer un numéro de suivi');
            return;
        }

        const trackingNumber = this.trackingForm.value.trackingNumber;

        this.deliveryService.trackDelivery(trackingNumber).subscribe({
            next: (delivery) => {
                this.notificationService.success(`Livraison trouvée: ${this.getStatusLabel(delivery.status)}`);
                // Could open a dialog with full details
            },
            error: (error) => {
                console.error('Error tracking delivery:', error);
                this.notificationService.error('Numéro de suivi introuvable');
            }
        });
    }

    updateDeliveryStatus(deliveryId: string, newStatus: string, courierName?: string): void {
        this.deliveryService.updateStatus(deliveryId, newStatus).subscribe({
            next: (updated) => {
                this.deliveries.update(deliveries =>
                    deliveries.map(d => d.id === deliveryId ? updated : d)
                );
                // No need to reload - computed getters will update automatically
                this.notificationService.success('Statut mis à jour');
            },
            error: (error) => {
                console.error('Error updating delivery status:', error);
                this.notificationService.error('Erreur lors de la mise à jour du statut');
            }
        });
    }

    assignCourier(deliveryId: string, courierName: string): void {
        // Guard: skip if empty value selected (placeholder)
        if (!courierName || courierName === '') {
            return;
        }

        // Guard: check if delivery already has this driver assigned
        const delivery = this.deliveries().find(d => d.id === deliveryId);
        if (delivery && delivery.driverName === courierName) {
            console.log('Courier already assigned to this delivery');
            return;
        }

        // Guard: skip if already IN_TRANSIT (already assigned)
        if (delivery && delivery.status !== 'PROCESSING') {
            console.log('Delivery already in transit, skipping assignment');
            return;
        }

        this.deliveryService.markAsPickedUp(deliveryId, courierName).subscribe({
            next: (updated) => {
                this.deliveries.update(deliveries =>
                    deliveries.map(d => d.id === deliveryId ? updated : d)
                );
                this.notificationService.success(`Livraison assignée à ${courierName}`);
            },
            error: (error) => {
                console.error('Error assigning courier:', error);
                this.notificationService.error('Erreur lors de l\'assignation du coursier');
            }
        });
    }

    getStatusColor(status: string): string {
        const colors: { [key: string]: string } = {
            'PROCESSING': 'accent',
            'IN_TRANSIT': 'primary',
            'OUT_FOR_DELIVERY': 'primary',
            'DELIVERED': 'primary',
            'FAILED': 'warn'
        };
        return colors[status] || 'primary';
    }

    getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            'PROCESSING': 'En préparation',
            'PICKED_UP': 'Récupéré',
            'IN_TRANSIT': 'En transit',
            'OUT_FOR_DELIVERY': 'En cours de livraison',
            'DELIVERED': 'Livrée',
            'FAILED': 'Échec',
            'CANCELLED': 'Annulée'
        };
        return labels[status] || status;
    }

    getStatusIcon(status: string): string {
        const icons: { [key: string]: string } = {
            'PROCESSING': 'inventory',
            'PICKED_UP': 'local_shipping',
            'IN_TRANSIT': 'local_shipping',
            'OUT_FOR_DELIVERY': 'delivery_dining',
            'DELIVERED': 'check_circle',
            'FAILED': 'error',
            'CANCELLED': 'cancel'
        };
        return icons[status] || 'local_shipping';
    }

    getDeliveryProgress(status: string): number {
        const progress: { [key: string]: number } = {
            'PROCESSING': 20,
            'PICKED_UP': 40,
            'IN_TRANSIT': 60,
            'OUT_FOR_DELIVERY': 80,
            'DELIVERED': 100,
            'FAILED': 0,
            'CANCELLED': 0
        };
        return progress[status] || 0;
    }

    // Analytics helper methods
    getDeliverySuccessRate(): number {
        const total = this.stats().totalDeliveries || 0;
        const delivered = this.stats().delivered || 0;
        if (total === 0) return 0;
        return Math.round((delivered / total) * 100);
    }

    getStatusPercentage(type: string): number {
        const total = this.stats().totalDeliveries || 0;
        if (total === 0) return 0;

        switch (type) {
            case 'processing':
                return ((this.stats().processing || 0) / total) * 100;
            case 'transit':
                return (((this.stats().inTransit || 0) + (this.stats().outForDelivery || 0)) / total) * 100;
            case 'delivered':
                return ((this.stats().delivered || 0) / total) * 100;
            case 'failed':
                return ((this.stats().failed || 0) / total) * 100;
            default:
                return 0;
        }
    }

    handlePageEvent(event: PageEvent): void {
        this.pageSize.set(event.pageSize);
        this.pageIndex.set(event.pageIndex);
    }

    exportToCSV(): void {
        this.exportService.exportDeliveries(this.deliveries());
        this.notificationService.success('Export livraisons téléchargé');
    }

    printDeliveryLabel(deliveryId: string): void {
        this.notificationService.info('Impression de l\'étiquette...');
        // Implement print logic
    }

    viewDeliveryDetails(delivery: Delivery): void {
        const dialogRef = this.dialog.open(DeliveryDetailDialogComponent, {
            data: { delivery },
            width: '900px',
            maxWidth: '95vw',
            maxHeight: '90vh',
            panelClass: 'delivery-detail-dialog-panel'
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result?.action === 'track' && result?.trackingNumber) {
                this.router.navigate(['/track', result.trackingNumber]);
            }
        });
    }

    trackOnMap(delivery: Delivery): void {
        if (delivery.trackingNumber) {
            this.router.navigate(['/track', delivery.trackingNumber]);
        } else {
            this.notificationService.warning('Numéro de suivi non disponible');
        }
    }
}
