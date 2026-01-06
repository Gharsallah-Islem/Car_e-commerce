import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest } from '../../../core/models';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatDividerModule,
        MatProgressSpinnerModule,
        MatCheckboxModule
    ],
    templateUrl: './login.component.html',
    styleUrl: './login.component.scss'
})
export class LoginComponent {
    loginForm: FormGroup;
    hidePassword = signal(true);
    isLoading = signal(false);

    // Form field focus states for custom styling
    emailFocused = false;
    passwordFocused = false;

    features = [
        {
            icon: 'inventory_2',
            title: '10,000+ Parts',
            description: 'Extensive inventory for all car models'
        },
        {
            icon: 'verified_user',
            title: '100% Authentic',
            description: 'Guaranteed genuine auto parts'
        },
        {
            icon: 'bolt',
            title: 'Express Shipping',
            description: 'Fast delivery to your doorstep'
        }
    ];

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(6)]]
        });
    }

    /**
     * Toggle password visibility
     */
    togglePasswordVisibility(): void {
        this.hidePassword.set(!this.hidePassword());
    }

    /**
     * Get form control for template
     */
    get emailControl() {
        return this.loginForm.get('email');
    }

    get passwordControl() {
        return this.loginForm.get('password');
    }

    /**
     * Handle form submission
     */
    onSubmit(): void {
        if (this.loginForm.invalid) {
            this.loginForm.markAllAsTouched();
            return;
        }

        this.isLoading.set(true);
        const credentials: LoginRequest = this.loginForm.value;

        this.authService.login(credentials).subscribe({
            next: (response) => {
                this.isLoading.set(false);
                // Navigate to dashboard or return URL
                const returnUrl = this.router.parseUrl(this.router.url).queryParams['returnUrl'] || '/';
                this.router.navigate([returnUrl]);
            },
            error: (error) => {
                this.isLoading.set(false);
                console.error('Login failed:', error);

                // Check if it's an email verification error (403 status)
                if (error.status === 403 && error.error?.emailVerified === false) {
                    // Redirect to verify-email page
                    this.router.navigate(['/auth/verify-email'], {
                        queryParams: { email: credentials.email }
                    });
                }
            }
        });
    }

    /**
     * Login with Google OAuth2
     */
    loginWithGoogle(): void {
        this.authService.loginWithGoogle();
    }

    /**
     * Get email error message
     */
    getEmailErrorMessage(): string {
        const control = this.emailControl;
        if (control?.hasError('required')) {
            return 'Email is required';
        }
        if (control?.hasError('email')) {
            return 'Please enter a valid email';
        }
        return '';
    }

    /**
     * Get password error message
     */
    getPasswordErrorMessage(): string {
        const control = this.passwordControl;
        if (control?.hasError('required')) {
            return 'Password is required';
        }
        if (control?.hasError('minlength')) {
            return 'Password must be at least 6 characters';
        }
        return '';
    }
}
