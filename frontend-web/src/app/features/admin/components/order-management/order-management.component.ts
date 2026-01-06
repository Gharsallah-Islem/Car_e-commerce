import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { NotificationService } from '../../../../core/services/notification.service';
import { ExportService } from '../../../../core/services/export.service';
import { OrderService } from '../../../../core/services/order.service';
import { Order, OrderStatus } from '../../../../core/models';

@Component({
  selector: 'app-order-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatTooltipModule,
    MatCardModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './order-management.component.html',
  styleUrl: './order-management.component.scss'
})
export class OrderManagementComponent implements OnInit {
  // Math for template
  Math = Math;

  // Orders
  orders = signal<Order[]>([]);
  filteredOrders = signal<Order[]>([]);
  loading = signal<boolean>(false);
  totalOrders = signal<number>(0);

  // Pagination & Sorting
  pageSize = signal<number>(10);
  pageIndex = signal<number>(0);
  sortField = signal<string>('createdAt');
  sortDirection = signal<'asc' | 'desc'>('desc');

  // Filters
  selectedStatus = 'all';
  selectedPeriod = 'all';
  searchQuery = '';

  // Status options for dropdown
  statusOptions = Object.values(OrderStatus);

  constructor(
    private router: Router,
    private notificationService: NotificationService,
    private orderService: OrderService,
    private exportService: ExportService
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
        this.applyFilters();
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.notificationService.error('Erreur lors du chargement des commandes');
        this.loading.set(false);
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.orders()];

    // Status filter
    if (this.selectedStatus !== 'all') {
      filtered = filtered.filter(order => order.status === this.selectedStatus);
    }

    // Period filter
    if (this.selectedPeriod !== 'all') {
      const now = new Date();
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

      filtered = filtered.filter(order => {
        const orderDate = new Date(order.createdAt);
        switch (this.selectedPeriod) {
          case 'today':
            return orderDate >= today;
          case 'week':
            const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
            return orderDate >= weekAgo;
          case 'month':
            const monthAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);
            return orderDate >= monthAgo;
          default:
            return true;
        }
      });
    }

    // Search filter
    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(order =>
        order.id?.toLowerCase().includes(query) ||
        order.user?.fullName?.toLowerCase().includes(query) ||
        order.user?.email?.toLowerCase().includes(query)
      );
    }

    this.filteredOrders.set(filtered);
  }

  onSearch(event: Event): void {
    this.searchQuery = (event.target as HTMLInputElement).value;
    this.applyFilters();
  }

  updateOrderStatus(orderId: string, newStatus: string): void {
    this.orderService.updateOrderStatus(orderId, newStatus).subscribe({
      next: (updatedOrder) => {
        this.orders.update(orders =>
          orders.map(o => o.id === updatedOrder.id ? updatedOrder : o)
        );
        this.applyFilters();
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

  // ========== ANALYTICS METHODS ==========

  getPendingCount(): number {
    return this.orders().filter(o => o.status === 'PENDING').length;
  }

  getConfirmedCount(): number {
    return this.orders().filter(o => o.status === 'CONFIRMED').length;
  }

  getShippedCount(): number {
    return this.orders().filter(o => o.status === 'SHIPPED').length;
  }

  getDeliveredCount(): number {
    return this.orders().filter(o => o.status === 'DELIVERED').length;
  }

  getCancelledCount(): number {
    return this.orders().filter(o => o.status === 'CANCELLED').length;
  }

  getStatusPercentage(status: string): number {
    const total = this.orders().length;
    if (total === 0) return 0;

    const count = this.orders().filter(o => o.status === status).length;
    return (count / total) * 100;
  }

  getTotalRevenue(): number {
    return this.orders()
      .filter(o => o.status !== 'CANCELLED' && o.status !== 'REFUNDED')
      .reduce((sum, order) => sum + (order.totalPrice || 0), 0);
  }

  getCompletionRate(): number {
    const total = this.orders().length;
    if (total === 0) return 0;

    const delivered = this.getDeliveredCount();
    return (delivered / total) * 100;
  }

  getAverageOrderValue(): number {
    const validOrders = this.orders().filter(o => o.status !== 'CANCELLED');
    if (validOrders.length === 0) return 0;

    const total = validOrders.reduce((sum, order) => sum + (order.totalPrice || 0), 0);
    return total / validOrders.length;
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
      PAID: 'payment',
      DELIVERY_FAILED: 'error'
    };
    return icons[status] || 'receipt';
  }

  getCustomerInitials(order: Order): string {
    const name = order.user?.fullName || order.user?.username || 'U';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    return name.charAt(0).toUpperCase();
  }

  // ========== PAGINATION ==========

  previousPage(): void {
    if (this.pageIndex() > 0) {
      this.pageIndex.update(p => p - 1);
      this.loadOrders();
    }
  }

  nextPage(): void {
    if ((this.pageIndex() + 1) * this.pageSize() < this.totalOrders()) {
      this.pageIndex.update(p => p + 1);
      this.loadOrders();
    }
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

  // ========== ACTIONS ==========

  printOrder(order: Order): void {
    this.notificationService.info('Impression en cours de développement');
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

  exportOrders(): void {
    this.exportService.exportOrders(this.filteredOrders());
    this.notificationService.success('Export commandes téléchargé');
  }
}
