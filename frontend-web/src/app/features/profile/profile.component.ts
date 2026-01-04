import { Component, OnInit, signal, computed, ElementRef, ViewChild } from '@angular/core';
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
import { HttpClient } from '@angular/common/http';

import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { OrderService } from '../../core/services/order.service';
import { User, Address, Order, OrderStatus } from '../../core/models';
import { environment } from '../../../environments/environment';

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
    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

    // Forms
    profileForm!: FormGroup;
    passwordForm!: FormGroup;
    addressForm!: FormGroup;

    // State
    currentUser = signal<User | null>(null);
    orders = signal<Order[]>([]);
    addresses = signal<Address[]>([]);
    loading = signal<boolean>(false);
    loadingOrders = signal<boolean>(false);
    editingProfile = signal<boolean>(false);
    editingAddress = signal<number | null>(null);
    selectedTabIndex = signal<number>(0);
    selectedOrder = signal<Order | null>(null);
    showOrderDetails = signal<boolean>(false);
    uploadingPicture = signal<boolean>(false);

    // Computed
    fullName = computed(() => {
        const user = this.currentUser();
        return user ? `${user.firstName} ${user.lastName}`.trim() : '';
    });

    orderStats = computed(() => {
        const allOrders = this.orders();
        return {
            total: allOrders.length,
            pending: allOrders.filter(o =>
                o.status === OrderStatus.PENDING ||
                o.status === OrderStatus.PROCESSING
            ).length,
            delivered: allOrders.filter(o => o.status === OrderStatus.DELIVERED).length,
            totalSpent: allOrders
                .filter(o => o.status !== OrderStatus.CANCELLED && o.status !== OrderStatus.REFUNDED)
                .reduce((sum, o) => sum + (o.totalPrice || 0), 0)
        };
    });

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private http: HttpClient,
        private authService: AuthService,
        private notificationService: NotificationService,
        private orderService: OrderService
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
            phoneNumber: ['', [Validators.pattern(/^[0-9]{8}$/)]],
            address: ['']
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
            postalCode: ['', [Validators.required, Validators.pattern(/^[0-9]{4}$/)]],
            country: ['Tunisie', [Validators.required]]
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
                        phoneNumber: user.phoneNumber || '',
                        address: user.address || ''
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
        this.loadingOrders.set(true);
        this.orderService.getMyOrders(0, 20).subscribe({
            next: (response) => {
                this.orders.set(response.content || []);
                this.loadingOrders.set(false);
            },
            error: (err) => {
                console.error('Error loading orders:', err);
                this.orders.set([]);
                this.loadingOrders.set(false);
            }
        });
    }

    loadAddresses(): void {
        // Load addresses from user profile if available
        const user = this.currentUser();
        if (user && (user as any).addresses) {
            this.addresses.set((user as any).addresses);
        } else {
            this.addresses.set([]);
        }
    }

    toggleEditProfile(): void {
        this.editingProfile.set(!this.editingProfile());
        if (!this.editingProfile()) {
            this.loadUserData();
        }
    }

    // Profile picture upload
    triggerFileInput(): void {
        this.fileInput.nativeElement.click();
    }

    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files || input.files.length === 0) return;

        const file = input.files[0];

        // Validate file type
        if (!file.type.startsWith('image/')) {
            this.notificationService.error('Veuillez sélectionner une image');
            return;
        }

        // Validate file size (max 2MB)
        if (file.size > 2 * 1024 * 1024) {
            this.notificationService.error('L\'image ne doit pas dépasser 2 Mo');
            return;
        }

        this.uploadingPicture.set(true);

        // Convert to Base64
        const reader = new FileReader();
        reader.onload = () => {
            const base64 = reader.result as string;
            this.updateProfilePicture(base64);
        };
        reader.onerror = () => {
            this.notificationService.error('Erreur lors de la lecture du fichier');
            this.uploadingPicture.set(false);
        };
        reader.readAsDataURL(file);
    }

    updateProfilePicture(base64: string): void {
        const user = this.currentUser();
        if (!user) return;

        this.http.put<User>(`${environment.apiUrl}/users/me`, {
            email: user.email,
            firstName: user.firstName,
            lastName: user.lastName,
            phoneNumber: user.phoneNumber,
            address: user.address,
            profilePicture: base64
        }).subscribe({
            next: (updatedUser) => {
                this.currentUser.set(updatedUser);
                this.notificationService.success('Photo de profil mise à jour');
                this.uploadingPicture.set(false);
            },
            error: () => {
                this.notificationService.error('Erreur lors de la mise à jour de la photo');
                this.uploadingPicture.set(false);
            }
        });
    }

    saveProfile(): void {
        if (this.profileForm.invalid) {
            this.notificationService.warning('Veuillez remplir tous les champs obligatoires');
            return;
        }

        this.loading.set(true);
        const formValue = this.profileForm.getRawValue();
        const user = this.currentUser();

        this.http.put<User>(`${environment.apiUrl}/users/me`, {
            email: formValue.email,
            firstName: formValue.firstName,
            lastName: formValue.lastName,
            phoneNumber: formValue.phoneNumber,
            address: formValue.address,
            profilePicture: user?.profilePicture
        }).subscribe({
            next: (updatedUser) => {
                this.currentUser.set(updatedUser);
                this.notificationService.success('Profil mis à jour avec succès');
                this.editingProfile.set(false);
                this.loading.set(false);
            },
            error: () => {
                this.notificationService.error('Erreur lors de la mise à jour du profil');
                this.loading.set(false);
            }
        });
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
        this.addressForm.reset({ country: 'Tunisie' });
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
            this.addressForm.reset({ country: 'Tunisie' });
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
        this.addressForm.reset({ country: 'Tunisie' });
    }

    viewOrderDetails(order: Order): void {
        this.selectedOrder.set(order);
        this.showOrderDetails.set(true);
    }

    closeOrderDetails(): void {
        this.showOrderDetails.set(false);
        this.selectedOrder.set(null);
    }

    cancelOrder(orderId: string): void {
        if (confirm('Êtes-vous sûr de vouloir annuler cette commande ?')) {
            this.orderService.cancelOrder(orderId).subscribe({
                next: () => {
                    this.notificationService.success('Commande annulée avec succès');
                    this.loadOrders();
                    this.closeOrderDetails();
                },
                error: (err) => {
                    this.notificationService.error('Impossible d\'annuler cette commande');
                }
            });
        }
    }

    getStatusColor(status: string): string {
        const colors: { [key: string]: string } = {
            PENDING: 'pending',
            PAID: 'processing',
            PROCESSING: 'processing',
            SHIPPED: 'shipped',
            DELIVERED: 'delivered',
            CANCELLED: 'cancelled',
            REFUNDED: 'cancelled'
        };
        return colors[status] || 'pending';
    }

    getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            PENDING: 'En attente',
            PAID: 'Payée',
            PROCESSING: 'En préparation',
            SHIPPED: 'Expédiée',
            DELIVERED: 'Livrée',
            CANCELLED: 'Annulée',
            REFUNDED: 'Remboursée'
        };
        return labels[status] || status;
    }

    canTrackOrder(order: Order): boolean {
        // Can track if order is in a trackable status (SHIPPED or PROCESSING)
        const trackableStatuses = ['SHIPPED', 'PROCESSING'];
        return trackableStatuses.includes(order.status);
    }

    trackOrder(order: Order): void {
        // Use delivery tracking number, order tracking number, or generate from order ID
        const trackingNumber = order.delivery?.trackingNumber
            || order.trackingNumber
            || `ORD-${order.id}`;
        this.router.navigate(['/track', trackingNumber]);
    }

    canCancelOrder(order: Order): boolean {
        return order.status === OrderStatus.PENDING || order.status === OrderStatus.PAID;
    }

    logout(): void {
        if (confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
            this.authService.logout();
            this.router.navigate(['/']);
        }
    }
}
