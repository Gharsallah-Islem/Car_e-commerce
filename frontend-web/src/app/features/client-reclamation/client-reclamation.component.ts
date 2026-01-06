import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';

import { ReclamationService, Reclamation } from '../../core/services/reclamation.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-client-reclamation',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatProgressSpinnerModule,
        MatSnackBarModule,
        MatDividerModule
    ],
    templateUrl: './client-reclamation.component.html',
    styleUrl: './client-reclamation.component.scss'
})
export class ClientReclamationComponent {
    private reclamationService = inject(ReclamationService);
    private authService = inject(AuthService);
    private router = inject(Router);
    private fb = inject(FormBuilder);
    private snackBar = inject(MatSnackBar);

    loading = signal<boolean>(false);
    submitting = signal<boolean>(false);
    myReclamations = signal<Reclamation[]>([]);
    showForm = signal<boolean>(false);

    reclamationForm: FormGroup = this.fb.group({
        subject: ['', [Validators.required, Validators.minLength(5)]],
        category: ['', Validators.required],
        description: ['', [Validators.required, Validators.minLength(20)]]
    });

    categoryOptions = [
        { value: 'ORDER', label: 'Problème de commande' },
        { value: 'PRODUCT', label: 'Produit défectueux' },
        { value: 'DELIVERY', label: 'Problème de livraison' },
        { value: 'PAYMENT', label: 'Problème de paiement' },
        { value: 'OTHER', label: 'Autre' }
    ];

    ngOnInit(): void {
        this.loadMyReclamations();
    }

    loadMyReclamations(): void {
        this.loading.set(true);
        this.reclamationService.getMyReclamations(0, 20).subscribe({
            next: (response) => {
                this.myReclamations.set(response.content || []);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    toggleForm(): void {
        this.showForm.update(v => !v);
        if (!this.showForm()) {
            this.reclamationForm.reset();
        }
    }

    submitReclamation(): void {
        if (this.reclamationForm.invalid) {
            this.reclamationForm.markAllAsTouched();
            return;
        }

        this.submitting.set(true);
        const formData = this.reclamationForm.value;

        this.reclamationService.createReclamation({
            subject: formData.subject,
            category: formData.category,
            description: formData.description
        }).subscribe({
            next: () => {
                this.snackBar.open('Réclamation envoyée avec succès !', 'OK', { duration: 3000 });
                this.reclamationForm.reset();
                this.showForm.set(false);
                this.loadMyReclamations();
                this.submitting.set(false);
            },
            error: (err) => {
                console.error('Error:', err);
                this.snackBar.open('Erreur lors de l\'envoi', 'Fermer', { duration: 3000 });
                this.submitting.set(false);
            }
        });
    }

    getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            'OPEN': 'En attente',
            'IN_PROGRESS': 'En cours',
            'RESOLVED': 'Résolu',
            'CLOSED': 'Fermé'
        };
        return labels[status] || status;
    }

    getStatusClass(status: string): string {
        return `status-${status.toLowerCase().replace('_', '-')}`;
    }

    formatDate(date: Date | string): string {
        if (!date) return '-';
        return new Date(date).toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: 'short',
            year: 'numeric'
        });
    }
}
