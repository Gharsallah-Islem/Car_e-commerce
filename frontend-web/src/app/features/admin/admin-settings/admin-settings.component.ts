import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

interface NotificationPreference {
    key: string;
    label: string;
    description: string;
    email: boolean;
    push: boolean;
}

@Component({
    selector: 'app-admin-settings',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatTabsModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatSlideToggleModule,
        MatSelectModule,
        MatDividerModule,
        MatProgressSpinnerModule
    ],
    templateUrl: './admin-settings.component.html',
    styleUrl: './admin-settings.component.scss'
})
export class AdminSettingsComponent implements OnInit {
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);
    private notificationService = inject(NotificationService);

    // Tab state
    selectedTab = signal<number>(0);
    loading = signal<boolean>(false);
    saving = signal<boolean>(false);

    // Current user
    currentUser = this.authService.currentUser$;

    // Forms
    profileForm!: FormGroup;
    securityForm!: FormGroup;
    appearanceForm!: FormGroup;

    // Password visibility
    hideCurrentPassword = true;
    hideNewPassword = true;
    hideConfirmPassword = true;

    // Profile picture
    profilePictureUrl = signal<string>('');
    uploadingPicture = signal<boolean>(false);

    // Notification preferences
    notificationPreferences = signal<NotificationPreference[]>([
        { key: 'newOrder', label: 'Nouvelles commandes', description: 'Recevoir une notification à chaque nouvelle commande', email: true, push: true },
        { key: 'lowStock', label: 'Stock faible', description: 'Alerte quand un produit atteint le seuil minimum', email: true, push: true },
        { key: 'newUser', label: 'Nouveaux utilisateurs', description: 'Notification lors de l\'inscription d\'un nouveau client', email: false, push: true },
        { key: 'orderStatus', label: 'Changement de statut', description: 'Mises à jour sur les commandes en cours', email: true, push: false },
        { key: 'deliveryComplete', label: 'Livraisons terminées', description: 'Notification de fin de livraison', email: false, push: true },
        { key: 'systemAlerts', label: 'Alertes système', description: 'Notifications importantes du système', email: true, push: true }
    ]);

    // Theme options
    themes = [
        { value: 'light', label: 'Clair', icon: 'light_mode' },
        { value: 'dark', label: 'Sombre', icon: 'dark_mode' },
        { value: 'system', label: 'Système', icon: 'settings_brightness' }
    ];

    // Language options
    languages = [
        { value: 'fr', label: 'Français', flag: '🇫🇷' },
        { value: 'en', label: 'English', flag: '🇬🇧' },
        { value: 'ar', label: 'العربية', flag: '🇹🇳' }
    ];

    ngOnInit(): void {
        this.initForms();
        this.loadUserData();
    }

    private initForms(): void {
        this.profileForm = this.fb.group({
            fullName: ['', [Validators.required, Validators.minLength(3)]],
            email: ['', [Validators.required, Validators.email]],
            phone: ['', [Validators.pattern(/^[+]?[(]?[0-9]{1,4}[)]?[-\s./0-9]*$/)]],
            address: ['']
        });

        this.securityForm = this.fb.group({
            currentPassword: ['', Validators.required],
            newPassword: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword: ['', Validators.required],
            twoFactorEnabled: [false]
        }, { validators: this.passwordMatchValidator });

        this.appearanceForm = this.fb.group({
            theme: ['light'],
            language: ['fr'],
            compactSidebar: [false],
            animationsEnabled: [true]
        });
    }

    private passwordMatchValidator(form: FormGroup) {
        const newPassword = form.get('newPassword')?.value;
        const confirmPassword = form.get('confirmPassword')?.value;
        return newPassword === confirmPassword ? null : { passwordMismatch: true };
    }

    private loadUserData(): void {
        this.currentUser.subscribe(user => {
            if (user) {
                this.profileForm.patchValue({
                    fullName: (user as any).fullName || `${user.firstName || ''} ${user.lastName || ''}`.trim(),
                    email: user.email,
                    phone: (user as any).phone || '',
                    address: (user as any).address || ''
                });
                this.profilePictureUrl.set((user as any).profilePicture || '');
            }
        });

        // Load saved preferences from localStorage
        const savedTheme = localStorage.getItem('admin_theme') || 'light';
        const savedLanguage = localStorage.getItem('admin_language') || 'fr';
        const compactSidebar = localStorage.getItem('admin_compact_sidebar') === 'true';
        const animationsEnabled = localStorage.getItem('admin_animations') !== 'false';

        this.appearanceForm.patchValue({
            theme: savedTheme,
            language: savedLanguage,
            compactSidebar,
            animationsEnabled
        });
    }

    // Profile methods
    onProfilePictureChange(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files[0]) {
            const file = input.files[0];
            if (file.size > 5 * 1024 * 1024) {
                this.notificationService.error('L\'image ne doit pas dépasser 5MB');
                return;
            }

            this.uploadingPicture.set(true);
            const reader = new FileReader();
            reader.onload = (e) => {
                this.profilePictureUrl.set(e.target?.result as string);
                this.uploadingPicture.set(false);
                this.notificationService.success('Photo de profil mise à jour');
            };
            reader.readAsDataURL(file);
        }
    }

    saveProfile(): void {
        if (this.profileForm.invalid) {
            this.notificationService.error('Veuillez corriger les erreurs du formulaire');
            return;
        }

        this.saving.set(true);
        // Simulate API call
        setTimeout(() => {
            this.saving.set(false);
            this.notificationService.success('Profil mis à jour avec succès');
        }, 1000);
    }

    // Security methods
    changePassword(): void {
        if (this.securityForm.invalid) {
            if (this.securityForm.hasError('passwordMismatch')) {
                this.notificationService.error('Les mots de passe ne correspondent pas');
            } else {
                this.notificationService.error('Veuillez remplir tous les champs requis');
            }
            return;
        }

        this.saving.set(true);
        setTimeout(() => {
            this.saving.set(false);
            this.securityForm.reset();
            this.notificationService.success('Mot de passe modifié avec succès');
        }, 1000);
    }

    toggleTwoFactor(): void {
        const enabled = this.securityForm.get('twoFactorEnabled')?.value;
        this.notificationService.info(enabled ? '2FA activée' : '2FA désactivée');
    }

    // Notification methods
    toggleNotificationPref(key: string, type: 'email' | 'push'): void {
        this.notificationPreferences.update(prefs =>
            prefs.map(p => p.key === key ? { ...p, [type]: !p[type] } : p)
        );
        this.notificationService.success('Préférences mises à jour');
    }

    // Appearance methods
    saveAppearance(): void {
        const values = this.appearanceForm.value;
        localStorage.setItem('admin_theme', values.theme);
        localStorage.setItem('admin_language', values.language);
        localStorage.setItem('admin_compact_sidebar', values.compactSidebar);
        localStorage.setItem('admin_animations', values.animationsEnabled);

        this.notificationService.success('Paramètres d\'apparence sauvegardés');

        // Apply theme
        if (values.theme === 'dark') {
            document.body.classList.add('dark-theme');
        } else {
            document.body.classList.remove('dark-theme');
        }
    }

    getInitials(): string {
        const name = this.profileForm.get('fullName')?.value || '';
        return name.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2);
    }
}
