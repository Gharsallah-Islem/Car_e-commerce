import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';

import { NotificationService } from '../../../../core/services/notification.service';
import { OrderService } from '../../../../core/services/order.service';
import { Order, OrderStatus } from '../../../../core/models';

@Component({
  selector: 'app-order-management',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatTooltipModule,
    MatCardModule,
    MatDividerModule
  ],
  templateUrl: './order-management.component.html',
  styleUrl: './order-management.component.scss'
})
export class OrderManagementComponent implements OnInit {
  // Orders
  orders = signal<Order[]>([]);
  orderColumns: string[] = ['orderNumber', 'customer', 'date', 'status', 'items', 'total', 'actions'];
  loading = signal<boolean>(false);
  totalOrders = signal<number>(0);

  // Pagination & Sorting
  pageSize = signal<number>(10);
  pageIndex = signal<number>(0);
  sortField = signal<string>('createdAt');
  sortDirection = signal<'asc' | 'desc'>('desc');

  // Status options for dropdown
  statusOptions = Object.values(OrderStatus);

  constructor(
    private router: Router,
    private notificationService: NotificationService,
    private orderService: OrderService
  ) { }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);
    this.orderService.getAllOrders(
      this.pageIndex(),
      this.pageSize(),
      this.sortField(),
      this.sortDirection()
    ).subscribe({
      next: (response) => {
        this.orders.set(response.content);
        this.totalOrders.set(response.totalElements);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.notificationService.error('Erreur lors du chargement des commandes');
        this.loading.set(false);
      }
    });
  }

  updateOrderStatus(orderId: string, newStatus: string): void {
    this.orderService.updateOrderStatus(orderId, newStatus).subscribe({
      next: (updatedOrder) => {
        this.orders.update(orders =>
          orders.map(o => o.id === updatedOrder.id ? updatedOrder : o)
        );
        this.notificationService.success('Statut de commande mis à jour');
      },
      error: (error) => {
        console.error('Error updating order status:', error);
        this.notificationService.error('Erreur lors de la mise à jour du statut');
      }
    });
  }

  viewOrderDetails(orderId: string): void {
    this.router.navigate(['/admin/orders', orderId]);
  }

  handlePageEvent(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
    this.loadOrders();
  }

  handleSort(sort: Sort): void {
    this.sortField.set(sort.active);
    this.sortDirection.set(sort.direction as 'asc' | 'desc');
    this.loadOrders();
  }

  getOrderStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      PENDING: 'accent',
      CONFIRMED: 'primary',
      PROCESSING: 'primary',
      SHIPPED: 'primary',
      DELIVERED: 'primary',
      CANCELLED: 'warn',
      REFUNDED: 'warn',
      PAID: 'accent',
      DELIVERY_FAILED: 'warn'
    };
    return colors[status] || 'primary';
  }

  getOrderStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      PENDING: 'En attente',
      CONFIRMED: 'Confirmée',
      PROCESSING: 'En préparation',
      SHIPPED: 'Expédiée',
      DELIVERED: 'Livrée',
      CANCELLED: 'Annulée',
      REFUNDED: 'Remboursée',
      PAID: 'Payée',
      DELIVERY_FAILED: 'Échec livraison'
    };
    return labels[status] || status;
  }
}
