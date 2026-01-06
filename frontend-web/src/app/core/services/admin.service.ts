import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';
import { User, UserRole } from '../models';

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    private readonly endpoint = 'admin';

    constructor(private apiService: ApiService) { }

    /**
     * Get all users (Admins only)
     */
    /**
     * Get all users (Admins only)
     */
    getAllUsers(): Observable<User[]> {
        return this.apiService.get<User[]>('users');
    }

    /**
     * Get all admins (Super Admin only)
     */
    getAllAdmins(): Observable<User[]> {
        return this.apiService.get<User[]>(this.endpoint);
    }

    /**
     * Create a new admin (Super Admin only)
     */
    createAdmin(adminData: any): Observable<User> {
        return this.apiService.post<User>(this.endpoint, adminData);
    }

    /**
     * Update admin (Super Admin only)
     */
    updateAdmin(id: string, adminData: any): Observable<User> {
        return this.apiService.put<User>(`${this.endpoint}/${id}`, adminData);
    }

    /**
     * Delete admin (Super Admin only)
     */
    deleteAdmin(id: string): Observable<void> {
        return this.apiService.delete<void>(`${this.endpoint}/${id}`);
    }

    /**
     * Activate user (Generic)
     */
    activateUser(id: string): Observable<User> {
        return this.apiService.patch<User>(`${this.endpoint}/users/${id}/activate`, {});
    }

    /**
     * Deactivate user (Generic)
     */
    deactivateUser(id: string): Observable<User> {
        return this.apiService.patch<User>(`${this.endpoint}/users/${id}/deactivate`, {});
    }

    /**
     * Get dashboard statistics
     */
    getDashboardStats(): Observable<any> {
        return this.apiService.get<any>(`${this.endpoint}/stats`);
    }

    /**
     * Update user role (Super Admin only)
     */
    updateUserRole(id: string, role: UserRole): Observable<User> {
        return this.apiService.patch<User>(`users/${id}/role`, {}, { role });
    }

    /**
     * Get user orders by user ID
     */
    getUserOrders(userId: string): Observable<any[]> {
        return this.apiService.get<any>(`orders/user/${userId}`).pipe(
            map((response: any) => response.content || response || [])
        );
    }
}
