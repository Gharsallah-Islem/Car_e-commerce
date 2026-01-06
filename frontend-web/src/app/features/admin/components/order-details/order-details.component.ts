import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { OrderService } from '../../../../core/services/order.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Order, OrderStatus } from '../../../../core/models';

@Component({
    selector: 'app-order-details',
    standalone: true,
    imports: [
        CommonModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatDividerModule,
        MatChipsModule,
        MatTableModule,
        MatTooltipModule,
        MatSelectModule,
        MatFormFieldModule,
        MatDialogModule,
        MatProgressSpinnerModule
    ],
    templateUrl: './order-details.component.html',
    styleUrl: './order-details.component.scss'
})
export class OrderDetailsComponent implements OnInit {
    // State
    order = signal<Order | null>(null);
    loading = signal<boolean>(false);
    orderId = signal<string>('');

    // Status options
    statusOptions = Object.values(OrderStatus);

    // Order items columns
    itemColumns: string[] = ['product', 'quantity', 'price', 'total'];

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private orderService: OrderService,
        private notificationService: NotificationService,
        private dialog: MatDialog
    ) { }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            const id = params['id'];
            if (id) {
                this.orderId.set(id);
                this.loadOrder(id);
            }
        });
    }

    loadOrder(id: string): void {
        this.loading.set(true);
        this.orderService.getOrderById(id).subscribe({
            next: (order) => {
                this.order.set(order);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading order:', error);
                this.notificationService.error('Erreur lors du chargement de la commande');
                this.loading.set(false);
                this.router.navigate(['/admin']);
            }
        });
    }

    updateOrderStatus(newStatus: string): void {
        const orderId = this.orderId();
        if (!orderId) return;

        this.orderService.updateOrderStatus(orderId, newStatus).subscribe({
            next: (updatedOrder) => {
                this.order.set(updatedOrder);
                this.notificationService.success('Statut de commande mis à jour');
            },
            error: (error) => {
                console.error('Error updating order status:', error);
                this.notificationService.error('Erreur lors de la mise à jour du statut');
            }
        });
    }

    printInvoice(): void {
        window.print();
    }

    downloadInvoice(): void {
        // TODO: Implement PDF generation
        this.notificationService.info('Génération PDF en cours...');
    }

    processRefund(): void {
        // TODO: Implement refund dialog
        this.notificationService.info('Fonction de remboursement à venir');
    }

    goBack(): void {
        this.router.navigate(['/admin']);
    }

    getStatusColor(status: string): string {
        const colors: { [key: string]: string } = {
            PENDING: 'accent',
            PROCESSING: 'primary',
            SHIPPED: 'primary',
            DELIVERED: 'primary',
            CANCELLED: 'warn',
            REFUNDED: 'warn',
            PAID: 'accent'
        };
        return colors[status] || 'primary';
    }

    getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            PENDING: 'En attente',
            PROCESSING: 'En préparation',
            SHIPPED: 'Expédiée',
            DELIVERED: 'Livrée',
            CANCELLED: 'Annulée',
            REFUNDED: 'Remboursée',
            PAID: 'Payée'
        };
        return labels[status] || status;
    }

    getItemTotal(item: any): number {
        return item.price * item.quantity;
    }

    formatDate(date: Date | string | undefined): string {
        if (!date) return 'N/A';
        return new Date(date).toLocaleDateString('fr-FR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    getStatusIcon(status: string): string {
        const icons: { [key: string]: string } = {
            PENDING: 'schedule',
            CONFIRMED: 'check_circle',
            PROCESSING: 'settings',
            SHIPPED: 'local_shipping',
            DELIVERED: 'done_all',
            CANCELLED: 'cancel',
            REFUNDED: 'replay',
            PAID: 'payment'
        };
        return icons[status] || 'receipt';
    }

    getCustomerInitials(): string {
        const order = this.order();
        if (!order) return 'U';
        const name = order.user?.fullName || order.user?.username || 'User';
        const parts = name.split(' ');
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return name.charAt(0).toUpperCase();
    }
}
