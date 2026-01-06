import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Reclamation {
    id: string;
    ticketNumber: string;
    user: any;
    order?: any;
    subject: string;
    description: string;
    category: string;
    priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
    status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
    assignedAgent?: any;
    response?: string;
    resolution?: string;
    attachmentUrl?: string;
    createdAt: Date;
    updatedAt: Date;
    resolvedAt?: Date;
}

export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface ReclamationStats {
    totalTickets: number;
    openTickets: number;
    inProgressTickets: number;
    inProgress: number;
    resolved: number;
    closed: number;
    resolvedToday: number;
    averageResolutionTime: number;
}

@Injectable({
    providedIn: 'root'
})
export class ReclamationService {
    private apiUrl = `${environment.apiUrl}/reclamations`;

    constructor(private http: HttpClient) { }

    // ==================== RECLAMATION CRUD ====================

    createReclamation(reclamation: Partial<Reclamation>): Observable<Reclamation> {
        return this.http.post<Reclamation>(this.apiUrl, reclamation);
    }

    getReclamationById(id: string): Observable<Reclamation> {
        return this.http.get<Reclamation>(`${this.apiUrl}/${id}`);
    }

    // Alias for getReclamationById
    getById(id: string): Observable<Reclamation> {
        return this.getReclamationById(id);
    }

    // ==================== ADMIN QUERIES ====================

    getAllReclamations(page: number = 0, size: number = 20, sort: string = 'createdAt'): Observable<Page<Reclamation>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sort', sort);
        return this.http.get<Page<Reclamation>>(this.apiUrl, { params });
    }

    getMyReclamations(page: number = 0, size: number = 20): Observable<Page<Reclamation>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Reclamation>>(`${this.apiUrl}/my-reclamations`, { params });
    }

    getReclamationsByStatus(status: string, page: number = 0, size: number = 20): Observable<Page<Reclamation>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Reclamation>>(`${this.apiUrl}/status/${status}`, { params });
    }

    getReclamationsByCategory(category: string, page: number = 0, size: number = 20): Observable<Page<Reclamation>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Reclamation>>(`${this.apiUrl}/category/${category}`, { params });
    }

    getPendingReclamations(page: number = 0, size: number = 20): Observable<Page<Reclamation>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Reclamation>>(`${this.apiUrl}/pending`, { params });
    }

    getMyAssignedReclamations(paramsOrPage: number | { page?: number; size?: number } = 0, size: number = 20): Observable<Page<Reclamation>> {
        let page: number;
        let pageSize: number;

        if (typeof paramsOrPage === 'object') {
            page = paramsOrPage.page || 0;
            pageSize = paramsOrPage.size || 20;
        } else {
            page = paramsOrPage;
            pageSize = size;
        }

        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', pageSize.toString());
        return this.http.get<Page<Reclamation>>(`${this.apiUrl}/assigned-to-me`, { params });
    }

    getReclamationsByAgent(agentId: string, page: number = 0, size: number = 20): Observable<Page<Reclamation>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Reclamation>>(`${this.apiUrl}/assigned/${agentId}`, { params });
    }

    // ==================== ASSIGNMENT ====================

    assignToAgent(id: string, agentId: string): Observable<Reclamation> {
        return this.http.patch<Reclamation>(`${this.apiUrl}/${id}/assign/${agentId}`, {});
    }

    assignToMe(id: string): Observable<Reclamation> {
        return this.http.patch<Reclamation>(`${this.apiUrl}/${id}/assign-to-me`, {});
    }

    // ==================== STATUS & RESPONSE ====================

    updateStatus(id: string, status: string): Observable<Reclamation> {
        return this.http.patch<Reclamation>(`${this.apiUrl}/${id}/status`, { status });
    }

    addResponse(id: string, response: string): Observable<Reclamation> {
        return this.http.post<Reclamation>(`${this.apiUrl}/${id}/response`, { response });
    }

    closeReclamation(id: string, resolution: string): Observable<Reclamation> {
        return this.http.patch<Reclamation>(`${this.apiUrl}/${id}/close`, { resolution });
    }

    // ==================== STATISTICS ====================

    getStatistics(): Observable<ReclamationStats> {
        return this.http.get<ReclamationStats>(`${this.apiUrl}/statistics`);
    }

    getAverageResolutionTime(): Observable<number> {
        return this.http.get<number>(`${this.apiUrl}/average-resolution-time`);
    }

    getPendingCount(): Observable<number> {
        return this.http.get<number>(`${this.apiUrl}/pending/count`);
    }

    // ==================== ALIASES FOR COMPATIBILITY ====================

    // Alias for getStatistics - used by support dashboard
    getStats(): Observable<ReclamationStats> {
        return this.getStatistics();
    }

    // Flexible query method - accepts an object with page, size, status, search
    getReclamations(params: { page?: number; size?: number; status?: string; search?: string }): Observable<Page<Reclamation>> {
        let httpParams = new HttpParams()
            .set('page', (params.page || 0).toString())
            .set('size', (params.size || 20).toString());

        if (params.status) {
            return this.getReclamationsByStatus(params.status, params.page || 0, params.size || 20);
        }

        return this.http.get<Page<Reclamation>>(this.apiUrl, { params: httpParams });
    }

    // ==================== AGENT PERFORMANCE ====================

    /**
     * Get current agent's performance statistics
     */
    getMyPerformance(): Observable<AgentPerformanceStats> {
        return this.http.get<AgentPerformanceStats>(`${this.apiUrl}/my-performance`);
    }

    /**
     * Get current agent's weekly statistics
     */
    getMyWeeklyStats(): Observable<WeeklyStats[]> {
        return this.http.get<WeeklyStats[]>(`${this.apiUrl}/my-weekly-stats`);
    }

    /**
     * Get current agent's recent activities
     */
    getMyActivities(limit: number = 10): Observable<RecentActivity[]> {
        const params = new HttpParams().set('limit', limit.toString());
        return this.http.get<RecentActivity[]>(`${this.apiUrl}/my-activities`, { params });
    }
}

// Additional interfaces for agent performance
export interface AgentPerformanceStats {
    totalAssigned: number;
    totalResolved: number;
    resolvedThisMonth: number;
    resolvedToday: number;
    inProgress: number;
    openTickets: number;
    avgResolutionTimeHours: number;
}

export interface WeeklyStats {
    day: string;
    tickets: number;
    resolved: number;
}

export interface RecentActivity {
    id: string;
    action: string;
    ticketSubject: string;
    time: string;
    type: 'resolved' | 'replied' | 'assigned' | 'escalated';
}
