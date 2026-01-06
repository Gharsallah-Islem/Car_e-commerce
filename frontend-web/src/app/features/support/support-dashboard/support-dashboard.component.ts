import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ReclamationService, Reclamation, AgentPerformanceStats } from '../../../core/services/reclamation.service';

interface DashboardStats {
    openTickets: number;
    inProgressTickets: number;
    resolvedToday: number;
    avgResponseTime: string;
}

@Component({
    selector: 'app-support-dashboard',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule,
        MatTableModule,
        MatChipsModule,
        MatProgressSpinnerModule
    ],
    templateUrl: './support-dashboard.component.html',
    styleUrls: ['./support-dashboard.component.scss']
})
export class SupportDashboardComponent implements OnInit {
    private reclamationService = inject(ReclamationService);

    loading = signal<boolean>(true);
    stats = signal<DashboardStats>({
        openTickets: 0,
        inProgressTickets: 0,
        resolvedToday: 0,
        avgResponseTime: '0h'
    });
    recentTickets = signal<Reclamation[]>([]);
    myAssignedTickets = signal<Reclamation[]>([]);

    displayedColumns = ['id', 'subject', 'user', 'status', 'createdAt', 'actions'];

    ngOnInit(): void {
        this.loadDashboardData();
    }

    private loadDashboardData(): void {
        this.loading.set(true);

        // Load agent-specific performance stats
        this.reclamationService.getMyPerformance().subscribe({
            next: (data: AgentPerformanceStats) => {
                this.stats.set({
                    openTickets: data.openTickets || 0,
                    inProgressTickets: data.inProgress || 0,
                    resolvedToday: data.resolvedToday || 0,
                    avgResponseTime: this.formatTime(data.avgResolutionTimeHours || 0)
                });
            },
            error: (err) => {
                console.error('Error loading agent stats:', err);
            }
        });

        // Load recent tickets
        this.reclamationService.getReclamations({ page: 0, size: 5 }).subscribe({
            next: (response) => {
                this.recentTickets.set(response.content || []);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });

        // Load my assigned tickets
        this.reclamationService.getMyAssignedReclamations({ page: 0, size: 5 }).subscribe({
            next: (response) => {
                this.myAssignedTickets.set(response.content || []);
            }
        });
    }

    private formatTime(hours: number): string {
        if (hours < 1) return `${Math.round(hours * 60)}m`;
        if (hours < 24) return `${Math.round(hours)}h`;
        return `${Math.round(hours / 24)}j`;
    }

    getStatusColor(status: string): string {
        const colors: { [key: string]: string } = {
            'OPEN': 'warn',
            'IN_PROGRESS': 'accent',
            'RESOLVED': 'primary',
            'CLOSED': 'default'
        };
        return colors[status] || 'default';
    }

    getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            'OPEN': 'Ouvert',
            'IN_PROGRESS': 'En cours',
            'RESOLVED': 'Résolu',
            'CLOSED': 'Fermé'
        };
        return labels[status] || status;
    }

    getPriorityIcon(priority: string): string {
        const icons: { [key: string]: string } = {
            'LOW': 'arrow_downward',
            'MEDIUM': 'remove',
            'HIGH': 'arrow_upward',
            'URGENT': 'priority_high'
        };
        return icons[priority] || 'remove';
    }

    formatDate(date: Date | string): string {
        if (!date) return '-';
        const d = new Date(date);
        const now = new Date();
        const diff = now.getTime() - d.getTime();
        const hours = Math.floor(diff / (1000 * 60 * 60));

        if (hours < 1) return 'À l\'instant';
        if (hours < 24) return `Il y a ${hours}h`;
        if (hours < 48) return 'Hier';
        return d.toLocaleDateString('fr-FR');
    }

    getGreeting(): string {
        const hour = new Date().getHours();
        if (hour < 12) return 'Bonjour';
        if (hour < 18) return 'Bon après-midi';
        return 'Bonsoir';
    }

    getPriorityClass(priority: string): string {
        if (!priority) return 'normal';
        const p = priority.toLowerCase();
        if (p === 'urgent' || p === 'high') return 'high';
        if (p === 'medium' || p === 'normal') return 'medium';
        return 'low';
    }

    getStatusClass(status: string): string {
        if (!status) return 'open';
        const s = status.toLowerCase().replace('_', '-');
        if (s === 'open' || s === 'new') return 'open';
        if (s === 'in-progress' || s === 'pending') return 'in-progress';
        if (s === 'resolved' || s === 'closed') return 'resolved';
        return 'open';
    }

    refresh(): void {
        this.loadDashboardData();
    }
}
