import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OrderService } from '../../core/services/order.service';

@Component({
    selector: 'app-my-orders',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatChipsModule,
        MatProgressSpinnerModule
    ],
    template: `
    <div class="orders-container">
      <div class="orders-header">
        <h1>Mes Commandes</h1>
        <p class="subtitle">Retrouvez l'historique de toutes vos commandes</p>
      </div>

      @if (loading()) {
        <div class="loading-container">
          <mat-spinner diameter="50"></mat-spinner>
          <p>Chargement de vos commandes...</p>
        </div>
      } @else if (orders().length === 0) {
        <div class="empty-state">
          <mat-icon>shopping_bag</mat-icon>
          <h2>Aucune commande</h2>
          <p>Vous n'avez pas encore passé de commande.</p>
          <a routerLink="/products" mat-raised-button color="primary">
            Découvrir nos produits
          </a>
        </div>
      } @else {
        <div class="orders-list">
          @for (order of orders(); track order.id) {
            <mat-card class="order-card">
              <div class="order-header">
                <div class="order-info">
                  <span class="order-id">Commande #{{ order.id.slice(0, 8).toUpperCase() }}</span>
                  <span class="order-date">{{ formatDate(order.createdAt) }}</span>
                </div>
                <div class="order-status">
                  <span class="status-badge" [class]="getStatusClass(order.status)">
                    {{ getStatusLabel(order.status) }}
                  </span>
                </div>
              </div>

              <div class="order-items">
                @for (item of getOrderItems(order).slice(0, 3); track item.id || $index) {
                  <div class="order-item">
                    <div class="item-image">
                      @if (item.product.imageUrl) {
                        <img [src]="item.product.imageUrl" [alt]="item.product.name">
                      } @else {
                        <mat-icon>image</mat-icon>
                      }
                    </div>
                    <div class="item-details">
                      <span class="item-name">{{ item.product.name }}</span>
                      <span class="item-qty">Qté: {{ item.quantity }}</span>
                    </div>
                    <span class="item-price">{{ (item.price || item.priceAtPurchase) * item.quantity | number:'1.2-2' }} TND</span>
                  </div>
                }
                @if (getOrderItems(order).length > 3) {
                  <p class="more-items">+ {{ getOrderItems(order).length - 3 }} autres articles</p>
                }
              </div>

              <div class="order-footer">
                <div class="order-total">
                  <span>Total:</span>
                  <strong>{{ order.totalPrice | number:'1.2-2' }} TND</strong>
                </div>
                <div class="order-actions">
                  @if (order.trackingNumber) {
                    <a [routerLink]="'/track/' + order.trackingNumber" mat-stroked-button color="primary">
                      <mat-icon>local_shipping</mat-icon>
                      Suivre la livraison
                    </a>
                  }
                </div>
              </div>
            </mat-card>
          }
        </div>
      }
    </div>
  `,
    styles: [`
    .orders-container {
      max-width: 900px;
      margin: 0 auto;
      padding: 2rem 1rem;
    }

    .orders-header {
      text-align: center;
      margin-bottom: 2rem;

      h1 {
        font-size: 2rem;
        font-weight: 700;
        color: #1a1a2e;
        margin: 0;
      }

      .subtitle {
        color: #666;
        margin-top: 0.5rem;
      }
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem;
      gap: 1rem;
      color: #666;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem 2rem;
      text-align: center;
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      border-radius: 16px;

      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: #dee2e6;
        margin-bottom: 1rem;
      }

      h2 {
        color: #1a1a2e;
        margin: 0;
      }

      p {
        color: #666;
        margin: 0.5rem 0 1.5rem;
      }
    }

    .orders-list {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .order-card {
      border-radius: 16px;
      overflow: hidden;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      transition: transform 0.2s, box-shadow 0.2s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
      }
    }

    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.25rem 1.5rem;
      background: linear-gradient(135deg, #1a1a2e 0%, #2d2d44 100%);
      color: white;
    }

    .order-info {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;

      .order-id {
        font-weight: 600;
        font-size: 1rem;
      }

      .order-date {
        font-size: 0.85rem;
        opacity: 0.8;
      }
    }

    .status-badge {
      padding: 0.35rem 0.75rem;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 600;
      text-transform: uppercase;

      &.pending {
        background: #fff3cd;
        color: #856404;
      }

      &.confirmed {
        background: #cce5ff;
        color: #004085;
      }

      &.shipped {
        background: #d4edda;
        color: #155724;
      }

      &.delivered {
        background: #28a745;
        color: white;
      }

      &.cancelled {
        background: #f8d7da;
        color: #721c24;
      }
    }

    .order-items {
      padding: 1.25rem 1.5rem;
      border-bottom: 1px solid #eee;
    }

    .order-item {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 0.75rem 0;
      border-bottom: 1px solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }
    }

    .item-image {
      width: 50px;
      height: 50px;
      border-radius: 8px;
      overflow: hidden;
      background: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      mat-icon {
        color: #ccc;
      }
    }

    .item-details {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;

      .item-name {
        font-weight: 500;
        color: #1a1a2e;
      }

      .item-qty {
        font-size: 0.85rem;
        color: #666;
      }
    }

    .item-price {
      font-weight: 600;
      color: #1a1a2e;
    }

    .more-items {
      text-align: center;
      color: #666;
      font-size: 0.9rem;
      margin: 0.5rem 0 0;
    }

    .order-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.25rem 1.5rem;
      background: #f8f9fa;
    }

    .order-total {
      display: flex;
      gap: 0.5rem;
      align-items: center;

      span {
        color: #666;
      }

      strong {
        font-size: 1.2rem;
        color: #1a1a2e;
      }
    }

    .order-actions {
      display: flex;
      gap: 0.75rem;
    }

    @media (max-width: 600px) {
      .order-header {
        flex-direction: column;
        gap: 0.75rem;
        align-items: flex-start;
      }

      .order-footer {
        flex-direction: column;
        gap: 1rem;
        align-items: stretch;
      }

      .order-actions {
        justify-content: center;
      }
    }
  `]
})
export class MyOrdersComponent implements OnInit {
    private orderService = inject(OrderService);

    orders = signal<any[]>([]);
    loading = signal(true);

    ngOnInit(): void {
        this.loadOrders();
    }

    loadOrders(): void {
        this.orderService.getMyOrders(0, 50).subscribe({
            next: (response) => {
                this.orders.set(response.content || []);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    formatDate(dateString: string): string {
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR', {
            day: 'numeric',
            month: 'long',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    getStatusClass(status: string): string {
        return status.toLowerCase().replace('_', '-');
    }

    getStatusLabel(status: string): string {
        const labels: Record<string, string> = {
            'PENDING': 'En attente',
            'CONFIRMED': 'Confirmée',
            'SHIPPED': 'Expédiée',
            'DELIVERED': 'Livrée',
            'CANCELLED': 'Annulée'
        };
        return labels[status] || status;
    }

    getOrderItems(order: any): any[] {
        return order.orderItems || order.items || [];
    }
}
