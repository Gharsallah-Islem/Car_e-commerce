import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
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
import { MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { User, UserProfile, Address } from '../../core/models';

interface Order {
    id: string;
    orderNumber: string;
    date: Date;
    status: 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled';
    total: number;
    itemCount: number;
    paymentMethod: string;
}

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
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
        MatDialogModule,
        MatTooltipModule
    ],
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
    // Forms
    profileForm!: FormGroup;
    passwordForm!: FormGroup;
    addressForm!: FormGroup;

    // State
    currentUser = signal<User | null>(null);
    userProfile = signal<UserProfile | null>(null);
    orders = signal<Order[]>([]);
    addresses = signal<Address[]>([]);
    loading = signal<boolean>(false);
    editingProfile = signal<boolean>(false);
    editingAddress = signal<number | null>(null);
    selectedTabIndex = signal<number>(0);

    // Table columns
    orderColumns: string[] = ['orderNumber', 'date', 'status', 'items', 'total', 'actions'];

    // Computed
    fullName = computed(() => {
        const user = this.currentUser();
        return user ? `${user.firstName} ${user.lastName}`.trim() : '';
    });

    orderStats = computed(() => {
        const allOrders = this.orders();
        return {
            total: allOrders.length,
            pending: allOrders.filter(o => o.status === 'pending').length,
            delivered: allOrders.filter(o => o.status === 'delivered').length,
            totalSpent: allOrders.reduce((sum, o) => sum + o.total, 0)
        };
    });

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private authService: AuthService,
        private notificationService: NotificationService
    ) {
        this.initForms();
    }

    ngOnInit(): void {
        this.loadUserData();
        this.loadOrders();
        this.loadAddresses();
    }

    initForms(): void {
        // Profile form
        this.profileForm = this.fb.group({
            firstName: ['', [Validators.required, Validators.minLength(2)]],
            lastName: ['', [Validators.required, Validators.minLength(2)]],
            email: ['', [Validators.required, Validators.email]],
            phoneNumber: ['', [Validators.pattern(/^[0-9]{10}$/)]]
        });

        // Password form
        this.passwordForm = this.fb.group({
            currentPassword: ['', [Validators.required, Validators.minLength(6)]],
            newPassword: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', [Validators.required]]
        }, {
            validators: this.passwordMatchValidator
        });

        // Address form
        this.addressForm = this.fb.group({
            street: ['', [Validators.required, Validators.minLength(5)]],
            city: ['', [Validators.required]],
            postalCode: ['', [Validators.required, Validators.pattern(/^[0-9]{5}$/)]],
            country: ['Maroc', [Validators.required]]
        });
    }

    passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
        const newPassword = group.get('newPassword')?.value;
        const confirmPassword = group.get('confirmPassword')?.value;
        return newPassword === confirmPassword ? null : { passwordMismatch: true };
    }

    loadUserData(): void {
        this.loading.set(true);
        this.authService.currentUser$.subscribe({
            next: (user) => {
                if (user) {
                    this.currentUser.set(user);
                    this.profileForm.patchValue({
                        firstName: user.firstName,
                        lastName: user.lastName,
                        email: user.email,
                        phoneNumber: user.phoneNumber || ''
                    });

                    // Disable email if OAuth user
                    if (user.provider === 'GOOGLE') {
                        this.profileForm.get('email')?.disable();
                    }
                }
                this.loading.set(false);
            },
            error: () => {
                this.notificationService.error('Erreur lors du chargement du profil');
                this.loading.set(false);
            }
        });
    }

    loadOrders(): void {
        // Simulate loading orders - replace with actual API call
        setTimeout(() => {
            const mockOrders: Order[] = [
                {
                    id: '1',
                    orderNumber: 'ORD-2024-001',
                    date: new Date('2024-10-15'),
                    status: 'delivered',
                    total: 1250.50,
                    itemCount: 3,
                    paymentMethod: 'Carte bancaire'
                },
                {
                    id: '2',
                    orderNumber: 'ORD-2024-002',
                    date: new Date('2024-10-28'),
                    status: 'shipped',
                    total: 450.00,
                    itemCount: 2,
                    paymentMethod: 'Paiement à la livraison'
                },
                {
                    id: '3',
                    orderNumber: 'ORD-2024-003',
                    date: new Date('2024-11-01'),
                    status: 'processing',
                    total: 890.25,
                    itemCount: 5,
                    paymentMethod: 'Carte bancaire'
                }
            ];
            this.orders.set(mockOrders);
        }, 500);
    }

    loadAddresses(): void {
        // Simulate loading addresses - replace with actual API call
        setTimeout(() => {
            const mockAddresses: Address[] = [
                {
                    id: 1,
                    street: '123 Rue Mohammed V',
                    city: 'Casablanca',
                    postalCode: '20000',
                    country: 'Maroc'
                },
                {
                    id: 2,
                    street: '456 Avenue Hassan II',
                    city: 'Rabat',
                    postalCode: '10000',
                    country: 'Maroc'
                }
            ];
            this.addresses.set(mockAddresses);
        }, 500);
    }

    toggleEditProfile(): void {
        this.editingProfile.set(!this.editingProfile());
        if (!this.editingProfile()) {
            // Reset form if cancelled
            this.loadUserData();
        }
    }

    saveProfile(): void {
        if (this.profileForm.invalid) {
            this.notificationService.warning('Veuillez remplir tous les champs obligatoires');
            return;
        }

        this.loading.set(true);
        // Simulate API call
        setTimeout(() => {
            const formValue = this.profileForm.getRawValue();
            this.notificationService.success('Profil mis à jour avec succès');
            this.editingProfile.set(false);
            this.loading.set(false);
        }, 1000);
    }

    changePassword(): void {
        if (this.passwordForm.invalid) {
            if (this.passwordForm.hasError('passwordMismatch')) {
                this.notificationService.error('Les mots de passe ne correspondent pas');
            } else {
                this.notificationService.warning('Veuillez remplir tous les champs');
            }
            return;
        }

        this.loading.set(true);
        // Simulate API call
        setTimeout(() => {
            this.notificationService.success('Mot de passe modifié avec succès');
            this.passwordForm.reset();
            this.loading.set(false);
        }, 1000);
    }

    addAddress(): void {
        if (this.addressForm.invalid) {
            this.notificationService.warning('Veuillez remplir tous les champs obligatoires');
            return;
        }

        const newAddress: Address = {
            id: this.addresses().length + 1,
            ...this.addressForm.value
        };

        this.addresses.update(addresses => [...addresses, newAddress]);
        this.addressForm.reset({ country: 'Maroc' });
        this.notificationService.success('Adresse ajoutée avec succès');
    }

    editAddress(address: Address): void {
        this.editingAddress.set(address.id || null);
        this.addressForm.patchValue(address);
    }

    updateAddress(): void {
        if (this.addressForm.invalid) {
            this.notificationService.warning('Veuillez remplir tous les champs obligatoires');
            return;
        }

        const id = this.editingAddress();
        if (id) {
            this.addresses.update(addresses =>
                addresses.map(addr =>
                    addr.id === id ? { ...addr, ...this.addressForm.value } : addr
                )
            );
            this.editingAddress.set(null);
            this.addressForm.reset({ country: 'Maroc' });
            this.notificationService.success('Adresse mise à jour avec succès');
        }
    }

    deleteAddress(addressId: number): void {
        if (confirm('Êtes-vous sûr de vouloir supprimer cette adresse ?')) {
            this.addresses.update(addresses =>
                addresses.filter(addr => addr.id !== addressId)
            );
            this.notificationService.success('Adresse supprimée avec succès');
        }
    }

    cancelAddressEdit(): void {
        this.editingAddress.set(null);
        this.addressForm.reset({ country: 'Maroc' });
    }

    viewOrderDetails(orderId: string): void {
        this.router.navigate(['/profile/orders', orderId]);
    }

    getStatusColor(status: string): string {
        const colors: { [key: string]: string } = {
            pending: 'accent',
            processing: 'primary',
            shipped: 'primary',
            delivered: 'primary',
            cancelled: 'warn'
        };
        return colors[status] || 'primary';
    }

    getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            pending: 'En attente',
            processing: 'En préparation',
            shipped: 'Expédiée',
            delivered: 'Livrée',
            cancelled: 'Annulée'
        };
        return labels[status] || status;
    }

    logout(): void {
        if (confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
            this.authService.logout();
            this.router.navigate(['/']);
        }
    }
}
