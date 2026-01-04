import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
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

import { AdminService } from '../../../../core/services/admin.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { User, UserRole } from '../../../../core/models';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    CommonModule,
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
    MatDividerModule
  ],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {
  // Users
  users = signal<User[]>([]);
  userColumns: string[] = ['id', 'name', 'email', 'role', 'provider', 'isActive', 'actions'];
  loading = signal<boolean>(false);

  // Pagination & Sorting
  pageSize = signal<number>(10);
  pageIndex = signal<number>(0);
  sortField = signal<string>('');
  sortDirection = signal<'asc' | 'desc'>('asc');

  constructor(
    private adminService: AdminService,
    private notificationService: NotificationService
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
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.notificationService.error('Erreur lors du chargement des utilisateurs');
        this.loading.set(false);
      }
    });
  }

  toggleUserStatus(userId: string): void {
    const user = this.users().find(u => u.id === userId);
    if (!user) return;

    if (user.isActive) {
      this.adminService.deactivateUser(userId).subscribe({
        next: (updatedUser) => {
          this.users.update(users => users.map(u => u.id === userId ? updatedUser : u));
          this.notificationService.success('Utilisateur désactivé');
        },
        error: (error) => {
          this.notificationService.error('Erreur lors de la désactivation');
        }
      });
    } else {
      this.adminService.activateUser(userId).subscribe({
        next: (updatedUser) => {
          this.users.update(users => users.map(u => u.id === userId ? updatedUser : u));
          this.notificationService.success('Utilisateur activé');
        },
        error: (error) => {
          this.notificationService.error('Erreur lors de l\'activation');
        }
      });
    }
  }

  changeUserRole(userId: string, newRole: UserRole): void {
    this.adminService.updateUserRole(userId, newRole).subscribe({
      next: (updatedUser) => {
        this.users.update(users => users.map(u => u.id === userId ? updatedUser : u));
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

  /**
   * Get role name as string (already normalized in loadUsers)
   */
  getRoleName(user: User): string {
    return user.role as string;
  }

  /**
   * View user profile
   */
  viewUserProfile(user: User): void {
    console.log('User Profile:', user);
    this.notificationService.success(`Affichage du profil de ${user.firstName} ${user.lastName}`);
  }
}
