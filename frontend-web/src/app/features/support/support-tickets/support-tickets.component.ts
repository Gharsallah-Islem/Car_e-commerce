import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';

import { ReclamationService, Reclamation } from '../../../core/services/reclamation.service';

@Component({
    selector: 'app-support-tickets',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        ReactiveFormsModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule,
        MatTableModule,
        MatPaginatorModule,
        MatChipsModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatProgressSpinnerModule,
        MatMenuModule,
        MatSnackBarModule,
        MatDividerModule
    ],
    templateUrl: './support-tickets.component.html',
    styleUrls: ['./support-tickets.component.scss']
})
export class SupportTicketsComponent implements OnInit {
    private reclamationService = inject(ReclamationService);
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private fb = inject(FormBuilder);
    private snackBar = inject(MatSnackBar);

    loading = signal<boolean>(true);
    tickets = signal<Reclamation[]>([]);
    totalElements = signal<number>(0);
    pageSize = signal<number>(10);
    pageIndex = signal<number>(0);
    selectedTicket = signal<Reclamation | null>(null);

    filterForm!: FormGroup;

    displayedColumns = ['subject', 'user', 'status', 'priority', 'createdAt', 'actions'];

    statusOptions = [
        { value: '', label: 'Tous les statuts' },
        { value: 'OPEN', label: 'Ouvert' },
        { value: 'IN_PROGRESS', label: 'En cours' },
        { value: 'RESOLVED', label: 'Résolu' },
        { value: 'CLOSED', label: 'Fermé' }
    ];

    ngOnInit(): void {
        this.initFilterForm();
        this.loadTickets();

        // Check for query params
        this.route.queryParams.subscribe(params => {
            if (params['status']) {
                this.filterForm.patchValue({ status: params['status'] });
                this.loadTickets();
            }
            if (params['filter'] === 'assigned') {
                this.loadMyAssignedTickets();
            }
        });
    }

    private initFilterForm(): void {
        this.filterForm = this.fb.group({
            search: [''],
            status: ['']
        });

        // Debounced search
        this.filterForm.get('search')?.valueChanges.subscribe(() => {
            this.loadTickets();
        });

        this.filterForm.get('status')?.valueChanges.subscribe(() => {
            this.loadTickets();
        });
    }

    loadTickets(): void {
        this.loading.set(true);
        const params = {
            page: this.pageIndex(),
            size: this.pageSize(),
            status: this.filterForm.get('status')?.value || undefined,
            search: this.filterForm.get('search')?.value || undefined
        };

        this.reclamationService.getReclamations(params).subscribe({
            next: (response) => {
                this.tickets.set(response.content || []);
                this.totalElements.set(response.totalElements || 0);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.snackBar.open('Erreur de chargement', 'Fermer', { duration: 3000 });
            }
        });
    }

    loadMyAssignedTickets(): void {
        this.loading.set(true);
        this.reclamationService.getMyAssignedReclamations({
            page: this.pageIndex(),
            size: this.pageSize()
        }).subscribe({
            next: (response) => {
                this.tickets.set(response.content || []);
                this.totalElements.set(response.totalElements || 0);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    onPageChange(event: PageEvent): void {
        this.pageIndex.set(event.pageIndex);
        this.pageSize.set(event.pageSize);
        this.loadTickets();
    }

    viewTicket(ticket: Reclamation): void {
        this.router.navigate(['/support/tickets', ticket.id]);
    }

    assignToMe(ticket: Reclamation): void {
        this.reclamationService.assignToMe(ticket.id).subscribe({
            next: () => {
                this.snackBar.open('Ticket assigné avec succès', 'OK', { duration: 3000 });
                this.loadTickets();
            },
            error: () => {
                this.snackBar.open('Erreur lors de l\'assignation', 'Fermer', { duration: 3000 });
            }
        });
    }

    updateStatus(ticket: Reclamation, status: string): void {
        this.reclamationService.updateStatus(ticket.id, status).subscribe({
            next: () => {
                this.snackBar.open('Statut mis à jour', 'OK', { duration: 3000 });
                this.loadTickets();
            },
            error: () => {
                this.snackBar.open('Erreur de mise à jour', 'Fermer', { duration: 3000 });
            }
        });
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

    getStatusClass(status: string): string {
        if (!status) return 'open';
        const s = status.toLowerCase().replace('_', '-');
        if (s === 'open' || s === 'new') return 'open';
        if (s === 'in-progress' || s === 'pending') return 'in-progress';
        if (s === 'resolved') return 'resolved';
        if (s === 'closed') return 'closed';
        return 'open';
    }

    getPriorityClass(priority: string): string {
        if (!priority) return 'medium';
        const p = priority.toLowerCase();
        if (p === 'urgent' || p === 'high') return 'high';
        if (p === 'medium' || p === 'normal') return 'medium';
        return 'low';
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

    getPriorityLabel(priority: string): string {
        const labels: { [key: string]: string } = {
            'LOW': 'Basse',
            'MEDIUM': 'Moyenne',
            'HIGH': 'Haute',
            'URGENT': 'Urgente'
        };
        return labels[priority] || priority || 'Normale';
    }

    formatDate(date: Date | string): string {
        if (!date) return '-';
        return new Date(date).toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    clearFilters(): void {
        this.filterForm.reset({ search: '', status: '' });
        this.pageIndex.set(0);
        this.loadTickets();
    }
}
