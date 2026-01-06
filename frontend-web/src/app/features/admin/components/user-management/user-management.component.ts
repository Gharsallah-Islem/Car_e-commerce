import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AdminService } from '../../../../core/services/admin.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { User, UserRole } from '../../../../core/models';
import { UserDetailsDialogComponent } from './user-details-dialog.component';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    MatTooltipModule,
    MatSlideToggleModule,
    MatCardModule,
    MatDividerModule,
    MatDialogModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {
  // Users
  users = signal<User[]>([]);
  filteredUsers = signal<User[]>([]);
  loading = signal<boolean>(false);

  // Filters
  selectedRole = 'all';
  selectedStatus = 'all';
  searchQuery = '';

  // Pagination & Sorting
  pageSize = signal<number>(10);
  pageIndex = signal<number>(0);
  sortField = signal<string>('');
  sortDirection = signal<'asc' | 'desc'>('asc');

  constructor(
    private adminService: AdminService,
    private notificationService: NotificationService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        // Normalize role from backend's Role object {id, name} to string
        const normalizedUsers = users.map(user => ({
          ...user,
          role: typeof user.role === 'string' ? user.role : (user.role as any).name
        }));
        this.users.set(normalizedUsers);
        this.applyFilters();
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.notificationService.error('Erreur lors du chargement des utilisateurs');
        this.loading.set(false);
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.users()];

    // Role filter
    if (this.selectedRole !== 'all') {
      filtered = filtered.filter(user => user.role === this.selectedRole);
    }

    // Status filter
    if (this.selectedStatus !== 'all') {
      filtered = filtered.filter(user =>
        this.selectedStatus === 'active' ? user.isActive : !user.isActive
      );
    }

    // Search filter
    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(user =>
        user.firstName?.toLowerCase().includes(query) ||
        user.lastName?.toLowerCase().includes(query) ||
        user.email?.toLowerCase().includes(query)
      );
    }

    this.filteredUsers.set(filtered);
  }

  onSearch(event: Event): void {
    this.searchQuery = (event.target as HTMLInputElement).value;
    this.applyFilters();
  }

  // ========== ANALYTICS METHODS ==========

  getActiveCount(): number {
    return this.users().filter(u => u.isActive).length;
  }

  getInactiveCount(): number {
    return this.users().filter(u => !u.isActive).length;
  }

  getClientCount(): number {
    return this.users().filter(u => u.role === 'CLIENT').length;
  }

  getAdminCount(): number {
    return this.users().filter(u => u.role === 'ADMIN').length;
  }

  getSuperAdminCount(): number {
    return this.users().filter(u => u.role === 'SUPER_ADMIN').length;
  }

  getLocalCount(): number {
    return this.users().filter(u => !u.provider || u.provider === 'LOCAL').length;
  }

  getGoogleCount(): number {
    return this.users().filter(u => u.provider === 'GOOGLE').length;
  }

  getRolePercentage(role: string): number {
    const total = this.users().length;
    if (total === 0) return 0;
    const count = this.users().filter(u => u.role === role).length;
    return (count / total) * 100;
  }

  getUserInitials(user: User): string {
    const first = user.firstName?.charAt(0) || '';
    const last = user.lastName?.charAt(0) || '';
    return (first + last).toUpperCase() || 'U';
  }

  getRoleLabel(role: string | UserRole): string {
    const labels: { [key: string]: string } = {
      CLIENT: 'Client',
      ADMIN: 'Admin',
      SUPER_ADMIN: 'Super Admin'
    };
    return labels[role as string] || role as string;
  }

  // ========== USER ACTIONS ==========

  toggleUserStatus(userId: string): void {
    const user = this.users().find(u => u.id === userId);
    if (!user) return;

    if (user.isActive) {
      this.adminService.deactivateUser(userId).subscribe({
        next: (updatedUser) => {
          this.users.update(users => users.map(u => u.id === userId ? updatedUser : u));
          this.applyFilters();
          this.notificationService.success('Utilisateur désactivé');
        },
        error: () => {
          this.notificationService.error('Erreur lors de la désactivation');
        }
      });
    } else {
      this.adminService.activateUser(userId).subscribe({
        next: (updatedUser) => {
          this.users.update(users => users.map(u => u.id === userId ? updatedUser : u));
          this.applyFilters();
          this.notificationService.success('Utilisateur activé');
        },
        error: () => {
          this.notificationService.error("Erreur lors de l'activation");
        }
      });
    }
  }

  changeUserRole(userId: string, newRole: UserRole): void {
    this.adminService.updateUserRole(userId, newRole).subscribe({
      next: (updatedUser) => {
        this.users.update(users => users.map(u => u.id === userId ? updatedUser : u));
        this.applyFilters();
        this.notificationService.success('Rôle utilisateur mis à jour');
      },
      error: (error) => {
        console.error('Error updating user role:', error);
        this.notificationService.error('Erreur lors de la mise à jour du rôle');
      }
    });
  }

  handlePageEvent(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
    this.loadUsers();
  }

  handleSort(sort: Sort): void {
    this.sortField.set(sort.active);
    this.sortDirection.set(sort.direction as 'asc' | 'desc');
    this.loadUsers();
  }

  viewUserProfile(user: User): void {
    this.adminService.getUserOrders(user.id).subscribe({
      next: (orders: any[]) => {
        this.dialog.open(UserDetailsDialogComponent, {
          data: {
            user: user,
            orders: orders
          },
          width: '700px',
          maxHeight: '90vh',
          panelClass: 'user-details-dialog-panel'
        });
      },
      error: () => {
        this.dialog.open(UserDetailsDialogComponent, {
          data: {
            user: user,
            orders: []
          },
          width: '700px',
          maxHeight: '90vh',
          panelClass: 'user-details-dialog-panel'
        });
      }
    });
  }
}
