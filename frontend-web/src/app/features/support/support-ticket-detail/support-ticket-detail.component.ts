import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';

import { ReclamationService, Reclamation } from '../../../core/services/reclamation.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-support-ticket-detail',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        ReactiveFormsModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule,
        MatChipsModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatProgressSpinnerModule,
        MatDividerModule,
        MatSnackBarModule,
        MatTooltipModule,
        MatDialogModule,
        MatMenuModule
    ],
    templateUrl: './support-ticket-detail.component.html',
    styleUrls: ['./support-ticket-detail.component.scss']
})
export class SupportTicketDetailComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private fb = inject(FormBuilder);
    private reclamationService = inject(ReclamationService);
    private authService = inject(AuthService);
    private snackBar = inject(MatSnackBar);

    loading = signal<boolean>(true);
    submitting = signal<boolean>(false);
    ticket = signal<Reclamation | null>(null);
    ticketId: string = '';

    responseForm!: FormGroup;

    statusOptions = [
        { value: 'OPEN', label: 'Ouvert', icon: 'radio_button_unchecked', color: 'open' },
        { value: 'IN_PROGRESS', label: 'En cours', icon: 'pending', color: 'in-progress' },
        { value: 'RESOLVED', label: 'Résolu', icon: 'check_circle', color: 'resolved' },
        { value: 'CLOSED', label: 'Fermé', icon: 'lock', color: 'closed' }
    ];

    ngOnInit(): void {
        this.initForm();
        this.ticketId = this.route.snapshot.paramMap.get('id') || '';

        if (this.ticketId) {
            this.loadTicket();
        } else {
            this.router.navigate(['/support/tickets']);
        }
    }

    private initForm(): void {
        this.responseForm = this.fb.group({
            response: ['', [Validators.required, Validators.minLength(10)]]
        });
    }

    loadTicket(): void {
        this.loading.set(true);
        this.reclamationService.getReclamationById(this.ticketId).subscribe({
            next: (ticket: Reclamation) => {
                this.ticket.set(ticket);
                this.loading.set(false);
            },
            error: (err: Error) => {
                console.error('Error loading ticket:', err);
                this.snackBar.open('Erreur lors du chargement du ticket', 'Fermer', { duration: 3000 });
                this.loading.set(false);
                this.router.navigate(['/support/tickets']);
            }
        });
    }

    submitResponse(): void {
        if (this.responseForm.invalid || !this.ticket()) return;

        this.submitting.set(true);
        const response = this.responseForm.get('response')?.value;

        this.reclamationService.addResponse(this.ticketId, response).subscribe({
            next: (updatedTicket) => {
                this.ticket.set(updatedTicket);
                this.responseForm.reset();
                this.snackBar.open('Réponse envoyée avec succès', 'OK', {
                    duration: 3000,
                    panelClass: ['success-snackbar']
                });
                this.submitting.set(false);
            },
            error: () => {
                this.snackBar.open('Erreur lors de l\'envoi de la réponse', 'Fermer', { duration: 3000 });
                this.submitting.set(false);
            }
        });
    }

    updateStatus(status: string): void {
        if (!this.ticket()) return;

        this.reclamationService.updateStatus(this.ticketId, status).subscribe({
            next: (updatedTicket) => {
                this.ticket.set(updatedTicket);
                this.snackBar.open(`Statut mis à jour: ${this.getStatusLabel(status)}`, 'OK', { duration: 3000 });
            },
            error: () => {
                this.snackBar.open('Erreur lors de la mise à jour du statut', 'Fermer', { duration: 3000 });
            }
        });
    }

    assignToMe(): void {
        if (!this.ticket()) return;

        this.reclamationService.assignToMe(this.ticketId).subscribe({
            next: (updatedTicket) => {
                this.ticket.set(updatedTicket);
                this.snackBar.open('Ticket assigné avec succès', 'OK', { duration: 3000 });
            },
            error: () => {
                this.snackBar.open('Erreur lors de l\'assignation', 'Fermer', { duration: 3000 });
            }
        });
    }

    resolveTicket(): void {
        const response = this.responseForm.get('response')?.value;
        if (!response || response.trim().length < 10) {
            this.snackBar.open('Veuillez fournir une réponse avant de résoudre (minimum 10 caractères)', 'OK', { duration: 4000 });
            return;
        }

        this.submitting.set(true);

        // First add response, then update status
        this.reclamationService.addResponse(this.ticketId, response).subscribe({
            next: () => {
                this.reclamationService.updateStatus(this.ticketId, 'RESOLVED').subscribe({
                    next: (updatedTicket) => {
                        this.ticket.set(updatedTicket);
                        this.responseForm.reset();
                        this.snackBar.open('Ticket résolu avec succès!', 'OK', {
                            duration: 3000,
                            panelClass: ['success-snackbar']
                        });
                        this.submitting.set(false);
                    },
                    error: () => {
                        this.snackBar.open('Erreur lors de la résolution', 'Fermer', { duration: 3000 });
                        this.submitting.set(false);
                    }
                });
            },
            error: () => {
                this.snackBar.open('Erreur lors de l\'envoi de la réponse', 'Fermer', { duration: 3000 });
                this.submitting.set(false);
            }
        });
    }

    closeTicket(): void {
        if (!this.ticket()) return;

        this.reclamationService.closeReclamation(this.ticketId, 'Fermé par l\'agent support').subscribe({
            next: (updatedTicket) => {
                this.ticket.set(updatedTicket);
                this.snackBar.open('Ticket fermé', 'OK', { duration: 3000 });
            },
            error: () => {
                this.snackBar.open('Erreur lors de la fermeture', 'Fermer', { duration: 3000 });
            }
        });
    }

    goBack(): void {
        this.router.navigate(['/support/tickets']);
    }

    // Helper methods
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
        return status.toLowerCase().replace('_', '-');
    }

    getStatusIcon(status: string): string {
        const icons: { [key: string]: string } = {
            'OPEN': 'radio_button_unchecked',
            'IN_PROGRESS': 'pending',
            'RESOLVED': 'check_circle',
            'CLOSED': 'lock'
        };
        return icons[status] || 'help';
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

    getPriorityClass(priority: string): string {
        if (!priority) return 'medium';
        return priority.toLowerCase();
    }

    formatDate(date: Date | string): string {
        if (!date) return '-';
        return new Date(date).toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: 'long',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    formatRelativeTime(date: Date | string): string {
        if (!date) return '';
        const d = new Date(date);
        const now = new Date();
        const diff = now.getTime() - d.getTime();
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);

        if (minutes < 1) return 'À l\'instant';
        if (minutes < 60) return `Il y a ${minutes} min`;
        if (hours < 24) return `Il y a ${hours}h`;
        if (days === 1) return 'Hier';
        return `Il y a ${days} jours`;
    }

    isAssignedToMe(): boolean {
        const ticket = this.ticket();
        if (!ticket?.assignedAgent) return false;
        // Check if current user is the assigned agent
        return true; // Simplified - would need to compare with current user
    }

    canRespond(): boolean {
        const ticket = this.ticket();
        if (!ticket) return false;
        return ticket.status !== 'CLOSED';
    }

    canResolve(): boolean {
        const ticket = this.ticket();
        if (!ticket) return false;
        return ticket.status === 'IN_PROGRESS' || ticket.status === 'OPEN';
    }
}
