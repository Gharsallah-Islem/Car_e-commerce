import { Component, OnInit, signal } from '@angular/core';
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

import { NotificationService } from '../../../core/services/notification.service';
import { DeliveryService, Delivery as DeliveryModel, DeliveryStats as DeliveryStatsModel } from '../../../core/services/delivery.service';

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
        MatProgressBarModule
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

    // Deliveries
    deliveries = signal<Delivery[]>([]);
    activeDeliveries = signal<Delivery[]>([]);
    deliveryColumns: string[] = ['trackingNumber', 'order', 'customer', 'address', 'status', 'courier', 'estimated', 'actions'];

    // Pagination
    pageSize = signal<number>(10);
    pageIndex = signal<number>(0);

    // Couriers
    couriers = ['Mohammed Ali', 'Ahmed Benali', 'Sara El Amrani', 'Youssef Alaoui'];

    constructor(
        private fb: FormBuilder,
        private notificationService: NotificationService,
        private deliveryService: DeliveryService
    ) {
        this.initForms();
    }

    ngOnInit(): void {
        this.loadDeliveryStats();
        this.loadDeliveries();
        this.loadActiveDeliveries();
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

    loadActiveDeliveries(): void {
        this.deliveryService.getActiveDeliveries(0, 100).subscribe({
            next: (response) => {
                this.activeDeliveries.set(response.content);
            },
            error: (error) => {
                console.error('Error loading active deliveries:', error);
                // Set empty array for graceful degradation
                this.activeDeliveries.set([]);
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
                this.loadActiveDeliveries();
                this.notificationService.success('Statut mis à jour');
            },
            error: (error) => {
                console.error('Error updating delivery status:', error);
                this.notificationService.error('Erreur lors de la mise à jour du statut');
            }
        });
    }

    assignCourier(deliveryId: string, courierName: string): void {
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

    handlePageEvent(event: PageEvent): void {
        this.pageSize.set(event.pageSize);
        this.pageIndex.set(event.pageIndex);
    }

    exportToCSV(): void {
        this.notificationService.info('Export en cours...');
        // Implement CSV export logic
    }

    printDeliveryLabel(deliveryId: string): void {
        this.notificationService.info('Impression de l\'étiquette...');
        // Implement print logic
    }
}
