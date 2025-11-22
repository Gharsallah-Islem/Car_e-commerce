import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatExpansionModule } from '@angular/material/expansion';

import { NotificationService } from '../../../core/services/notification.service';
import { ReclamationService, Reclamation, ReclamationStats } from '../../../core/services/reclamation.service';

// Using types from ReclamationService
type SupportTicket = Reclamation;
type SupportStats = ReclamationStats;

@Component({
    selector: 'app-support-management',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatTabsModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatInputModule,
        MatFormFieldModule,
        MatTableModule,
        MatChipsModule,
        MatDividerModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        MatTooltipModule,
        MatPaginatorModule,
        MatSortModule,
        MatMenuModule,
        MatBadgeModule,
        MatExpansionModule
    ],
    templateUrl: './support-management.component.html',
    styleUrls: ['./support-management.component.scss']
})
export class SupportManagementComponent implements OnInit {
    // Forms
    responseForm!: FormGroup;
    filterForm!: FormGroup;

    // State
    loading = signal<boolean>(false);
    selectedTabIndex = signal<number>(0);
    selectedTicket = signal<SupportTicket | null>(null);

    // Stats
    stats = signal<SupportStats>({
        totalTickets: 0,
        openTickets: 0,
        inProgress: 0,
        resolved: 0,
        closed: 0,
        averageResolutionTime: 0,
        satisfactionRate: 0
    });

    // Tickets
    tickets = signal<SupportTicket[]>([]);
    openTickets = signal<SupportTicket[]>([]);
    ticketColumns: string[] = ['ticketNumber', 'subject', 'customer', 'status', 'priority', 'category', 'agent', 'created', 'actions'];

    // Pagination
    pageSize = signal<number>(10);
    pageIndex = signal<number>(0);

    // Support Agents
    agents = ['Admin User', 'Support Agent 1', 'Support Agent 2', 'Support Agent 3'];
    categories = ['Produit', 'Livraison', 'Paiement', 'Compte', 'Technique', 'Autre'];

    constructor(
        private fb: FormBuilder,
        private notificationService: NotificationService,
        private reclamationService: ReclamationService
    ) {
        this.initForms();
    }

    ngOnInit(): void {
        this.loadSupportStats();
        this.loadTickets();
    }

    initForms(): void {
        this.responseForm = this.fb.group({
            message: ['', [Validators.required, Validators.minLength(10)]]
        });

        this.filterForm = this.fb.group({
            status: [''],
            priority: [''],
            category: [''],
            agent: ['']
        });
    }

    loadSupportStats(): void {
        this.loading.set(true);
        this.reclamationService.getStatistics().subscribe({
            next: (stats) => {
                this.stats.set(stats);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading support stats:', error);
                this.notificationService.error('Erreur lors du chargement des statistiques');
                this.loading.set(false);
            }
        });
    }

    loadTickets(): void {
        this.loading.set(true);
        const page = this.pageIndex();
        const size = this.pageSize();

        this.reclamationService.getAllReclamations(page, size).subscribe({
            next: (response) => {
                this.tickets.set(response.content);
                this.openTickets.set(response.content.filter(t => t.status === 'OPEN'));
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading tickets:', error);
                this.notificationService.error('Erreur lors du chargement des tickets');
                this.loading.set(false);
            }
        });
    }

    selectTicket(ticket: SupportTicket): void {
        this.selectedTicket.set(ticket);
        this.responseForm.reset();
    }

    assignTicket(ticketId: string, agentId: string): void {
        this.reclamationService.assignToAgent(ticketId, agentId).subscribe({
            next: (updated) => {
                this.tickets.update(tickets =>
                    tickets.map(t => t.id === ticketId ? updated : t)
                );
                this.notificationService.success('Ticket assigné avec succès');
            },
            error: (error) => {
                console.error('Error assigning ticket:', error);
                this.notificationService.error('Erreur lors de l\'assignation du ticket');
            }
        });
    }

    updateTicketStatus(ticketId: string, newStatus: SupportTicket['status']): void {
        this.reclamationService.updateStatus(ticketId, newStatus).subscribe({
            next: (updated) => {
                this.tickets.update(tickets =>
                    tickets.map(t => t.id === ticketId ? updated : t)
                );
                if (this.selectedTicket()?.id === ticketId) {
                    this.selectedTicket.set(updated);
                }
                this.loadTickets();
                this.notificationService.success('Statut mis à jour');
            },
            error: (error) => {
                console.error('Error updating status:', error);
                this.notificationService.error('Erreur lors de la mise à jour du statut');
            }
        });
    }

    updateTicketPriority(ticketId: string, newPriority: SupportTicket['priority']): void {
        // Note: Backend doesn't have a direct priority update endpoint
        // This would need to be added to the backend or handled differently
        this.tickets.update(tickets =>
            tickets.map(t => t.id === ticketId ? { ...t, priority: newPriority } : t)
        );
        this.notificationService.success('Priorité mise à jour');
    }

    addResponse(): void {
        if (this.responseForm.invalid || !this.selectedTicket()) {
            this.notificationService.warning('Veuillez entrer une réponse');
            return;
        }

        const message = this.responseForm.value.message;
        const ticketId = this.selectedTicket()!.id;

        this.reclamationService.addResponse(ticketId, message).subscribe({
            next: (updated) => {
                this.tickets.update(tickets =>
                    tickets.map(t => t.id === ticketId ? updated : t)
                );
                this.selectedTicket.set(updated);
                this.responseForm.reset();
                this.notificationService.success('Réponse ajoutée');
            },
            error: (error) => {
                console.error('Error adding response:', error);
                this.notificationService.error('Erreur lors de l\'ajout de la réponse');
            }
        });
    }

    getStatusColor(status: string): string {
        const colors: { [key: string]: string } = {
            'OPEN': 'accent',
            'IN_PROGRESS': 'primary',
            'RESOLVED': 'primary',
            'CLOSED': ''
        };
        return colors[status] || '';
    }

    getPriorityColor(priority: string): string {
        const colors: { [key: string]: string } = {
            'LOW': '',
            'MEDIUM': 'accent',
            'HIGH': 'warn',
            'URGENT': 'warn'
        };
        return colors[priority] || '';
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

    getPriorityLabel(priority: string): string {
        const labels: { [key: string]: string } = {
            'LOW': 'Faible',
            'MEDIUM': 'Moyen',
            'HIGH': 'Élevé',
            'URGENT': 'Urgent'
        };
        return labels[priority] || priority;
    }

    getResolutionTime(ticket: SupportTicket): string {
        if (!ticket.resolvedAt) return '-';
        const hours = Math.abs(ticket.resolvedAt.getTime() - ticket.createdAt.getTime()) / 36e5;
        if (hours < 24) return `${Math.round(hours)}h`;
        return `${Math.round(hours / 24)}j`;
    }

    handlePageEvent(event: PageEvent): void {
        this.pageSize.set(event.pageSize);
        this.pageIndex.set(event.pageIndex);
    }

    exportToCSV(): void {
        this.notificationService.info('Export en cours...');
    }
}
