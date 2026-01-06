import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { User } from '../../../../core/models';

export interface UserDetailsDialogData {
  user: User;
  orders?: any[];
}

@Component({
  selector: 'app-user-details-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatChipsModule,
    MatCardModule,
    MatTabsModule,
    MatTableModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="user-details-dialog">
      <!-- Header -->
      <div class="dialog-header">
        <div class="user-avatar">
          @if (data.user.profilePicture) {
            <img [src]="data.user.profilePicture" alt="Profile" />
          } @else {
            <mat-icon>person</mat-icon>
          }
        </div>
        <div class="user-info">
          <h2>{{ data.user.firstName }} {{ data.user.lastName }}</h2>
          <p class="email">{{ data.user.email }}</p>
          <div class="badges">
            <mat-chip [class]="getRoleClass()">
              <mat-icon>{{ getRoleIcon() }}</mat-icon>
              {{ getRoleName() }}
            </mat-chip>
            <mat-chip [class]="data.user.isActive ? 'active' : 'inactive'">
              {{ data.user.isActive ? 'Actif' : 'Inactif' }}
            </mat-chip>
            @if (data.user.enabled) {
              <mat-chip class="verified">
                <mat-icon>verified</mat-icon>
                Email vérifié
              </mat-chip>
            }
          </div>
        </div>
        <button mat-icon-button (click)="close()" class="close-btn">
          <mat-icon>close</mat-icon>
        </button>
      </div>

      <mat-divider></mat-divider>

      <!-- Content Tabs -->
      <mat-tab-group class="user-tabs">
        <!-- Profile Tab -->
        <mat-tab>
          <ng-template mat-tab-label>
            <mat-icon>person</mat-icon>
            Profil
          </ng-template>
          
          <div class="tab-content">
            <div class="info-grid">
              <div class="info-card">
                <mat-icon>badge</mat-icon>
                <div class="info-content">
                  <span class="label">ID Utilisateur</span>
                  <span class="value">{{ data.user.id }}</span>
                </div>
              </div>

              <div class="info-card">
                <mat-icon>person</mat-icon>
                <div class="info-content">
                  <span class="label">Nom complet</span>
                  <span class="value">{{ data.user.firstName }} {{ data.user.lastName }}</span>
                </div>
              </div>

              <div class="info-card">
                <mat-icon>phone</mat-icon>
                <div class="info-content">
                  <span class="label">Téléphone</span>
                  <span class="value">{{ data.user.phoneNumber || 'Non défini' }}</span>
                </div>
              </div>

              <div class="info-card">
                <mat-icon>login</mat-icon>
                <div class="info-content">
                  <span class="label">Méthode de connexion</span>
                  <span class="value">{{ data.user.provider === 'GOOGLE' ? 'Google' : 'Email/Mot de passe' }}</span>
                </div>
              </div>

              <div class="info-card">
                <mat-icon>calendar_today</mat-icon>
                <div class="info-content">
                  <span class="label">Date d'inscription</span>
                  <span class="value">{{ formatDate(data.user.createdAt) }}</span>
                </div>
              </div>

              <div class="info-card">
                <mat-icon>update</mat-icon>
                <div class="info-content">
                  <span class="label">Dernière mise à jour</span>
                  <span class="value">{{ formatDate(data.user.updatedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </mat-tab>

        <!-- Orders Tab -->
        <mat-tab>
          <ng-template mat-tab-label>
            <mat-icon>shopping_bag</mat-icon>
            Commandes
          </ng-template>
          
          <div class="tab-content">
            @if (data.orders && data.orders.length > 0) {
              <table mat-table [dataSource]="data.orders" class="orders-table">
                <ng-container matColumnDef="id">
                  <th mat-header-cell *matHeaderCellDef>ID</th>
                  <td mat-cell *matCellDef="let order">#{{ order.id?.substring(0, 8) }}</td>
                </ng-container>

                <ng-container matColumnDef="date">
                  <th mat-header-cell *matHeaderCellDef>Date</th>
                  <td mat-cell *matCellDef="let order">{{ formatDate(order.createdAt) }}</td>
                </ng-container>

                <ng-container matColumnDef="total">
                  <th mat-header-cell *matHeaderCellDef>Total</th>
                  <td mat-cell *matCellDef="let order">{{ order.totalPrice | number:'1.2-2' }} TND</td>
                </ng-container>

                <ng-container matColumnDef="status">
                  <th mat-header-cell *matHeaderCellDef>Statut</th>
                  <td mat-cell *matCellDef="let order">
                    <mat-chip [class]="getOrderStatusClass(order.status)">
                      {{ order.status }}
                    </mat-chip>
                  </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="['id', 'date', 'total', 'status']"></tr>
                <tr mat-row *matRowDef="let row; columns: ['id', 'date', 'total', 'status'];"></tr>
              </table>
            } @else {
              <div class="empty-state">
                <mat-icon>shopping_cart</mat-icon>
                <p>Aucune commande pour cet utilisateur</p>
              </div>
            }
          </div>
        </mat-tab>

        <!-- Activity Tab -->
        <mat-tab>
          <ng-template mat-tab-label>
            <mat-icon>history</mat-icon>
            Activité
          </ng-template>
          
          <div class="tab-content">
            <div class="stats-grid">
              <div class="stat-card">
                <mat-icon>shopping_bag</mat-icon>
                <div class="stat-value">{{ data.orders?.length || 0 }}</div>
                <div class="stat-label">Commandes</div>
              </div>
              
              <div class="stat-card">
                <mat-icon>payments</mat-icon>
                <div class="stat-value">{{ getTotalSpent() | number:'1.2-2' }} TND</div>
                <div class="stat-label">Total dépensé</div>
              </div>

              <div class="stat-card">
                <mat-icon>star</mat-icon>
                <div class="stat-value">{{ getAverageOrderValue() | number:'1.2-2' }} TND</div>
                <div class="stat-label">Panier moyen</div>
              </div>
            </div>
          </div>
        </mat-tab>
      </mat-tab-group>

      <!-- Footer Actions -->
      <div class="dialog-actions">
        <button mat-stroked-button (click)="close()">
          Fermer
        </button>
      </div>
    </div>
  `,
  styles: [`
    .user-details-dialog {
      min-width: 600px;
      max-width: 800px;
    }

    .dialog-header {
      display: flex;
      align-items: flex-start;
      gap: 20px;
      padding: 24px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      position: relative;
    }

    .user-avatar {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      border: 3px solid rgba(255, 255, 255, 0.5);
    }

    .user-avatar img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .user-avatar mat-icon {
      font-size: 40px;
      width: 40px;
      height: 40px;
      color: white;
    }

    .user-info {
      flex: 1;
    }

    .user-info h2 {
      margin: 0 0 4px 0;
      font-size: 24px;
      font-weight: 600;
    }

    .user-info .email {
      margin: 0 0 12px 0;
      opacity: 0.9;
      font-size: 14px;
    }

    .badges {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .badges mat-chip {
      background: rgba(255, 255, 255, 0.2) !important;
      color: white !important;
      font-size: 12px;
    }

    .badges mat-chip mat-icon {
      font-size: 16px !important;
      width: 16px !important;
      height: 16px !important;
      margin-right: 4px;
    }

    .close-btn {
      position: absolute;
      top: 16px;
      right: 16px;
      color: white;
    }

    .user-tabs {
      min-height: 300px;
    }

    .tab-content {
      padding: 24px;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
    }

    .info-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
      background: #f8f9fa;
      border-radius: 12px;
      border: 1px solid #e9ecef;
    }

    .info-card mat-icon {
      color: #667eea;
      font-size: 24px;
      width: 24px;
      height: 24px;
    }

    .info-content {
      display: flex;
      flex-direction: column;
    }

    .info-content .label {
      font-size: 12px;
      color: #666;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .info-content .value {
      font-size: 14px;
      font-weight: 500;
      color: #333;
      word-break: break-all;
    }

    .orders-table {
      width: 100%;
      border-radius: 8px;
      overflow: hidden;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;
    }

    .stat-card {
      text-align: center;
      padding: 24px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      color: white;
    }

    .stat-card mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      margin-bottom: 8px;
      opacity: 0.9;
    }

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 1px;
      opacity: 0.9;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px;
      color: #999;
    }

    .empty-state mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
    }

    .dialog-actions {
      display: flex;
      justify-content: flex-end;
      padding: 16px 24px;
      border-top: 1px solid #e9ecef;
    }

    /* Status colors */
    .role-admin { background: #e91e63 !important; }
    .role-super_admin { background: #9c27b0 !important; }
    .role-client { background: #4caf50 !important; }
    .role-driver { background: #ff9800 !important; }
    
    .active { background: #4caf50 !important; }
    .inactive { background: #f44336 !important; }
    .verified { background: #2196f3 !important; }

    .order-pending { background: #ff9800 !important; }
    .order-confirmed { background: #2196f3 !important; }
    .order-shipped { background: #9c27b0 !important; }
    .order-delivered { background: #4caf50 !important; }
    .order-cancelled { background: #f44336 !important; }
  `]
})
export class UserDetailsDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<UserDetailsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserDetailsDialogData
  ) { }

  close(): void {
    this.dialogRef.close();
  }

  getRoleName(): string {
    const role = this.data.user.role as string;
    const roleMap: { [key: string]: string } = {
      'CLIENT': 'Client',
      'ADMIN': 'Administrateur',
      'SUPER_ADMIN': 'Super Admin',
      'DRIVER': 'Livreur'
    };
    return roleMap[role] || role;
  }

  getRoleClass(): string {
    const role = (this.data.user.role as string).toLowerCase();
    return `role-${role}`;
  }

  getRoleIcon(): string {
    const role = this.data.user.role as string;
    const iconMap: { [key: string]: string } = {
      'CLIENT': 'person',
      'ADMIN': 'admin_panel_settings',
      'SUPER_ADMIN': 'shield',
      'DRIVER': 'local_shipping'
    };
    return iconMap[role] || 'person';
  }

  formatDate(date: string | Date | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getOrderStatusClass(status: string): string {
    return `order-${status?.toLowerCase() || 'pending'}`;
  }

  getTotalSpent(): number {
    if (!this.data.orders || this.data.orders.length === 0) return 0;
    return this.data.orders.reduce((sum, order) => sum + (order.totalPrice || 0), 0);
  }

  getAverageOrderValue(): number {
    if (!this.data.orders || this.data.orders.length === 0) return 0;
    return this.getTotalSpent() / this.data.orders.length;
  }
}
