import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
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
import { RegisterRequest } from '../../../core/models';

@Component({
    selector: 'app-register',
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
    templateUrl: './register.component.html',
    styleUrl: './register.component.scss'
})
export class RegisterComponent {
    registerForm: FormGroup;
    hidePassword = signal(true);
    hideConfirmPassword = signal(true);
    isLoading = signal(false);

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.registerForm = this.fb.group({
            firstName: ['', [Validators.required, Validators.minLength(2)]],
            lastName: ['', [Validators.required, Validators.minLength(2)]],
            email: ['', [Validators.required, Validators.email]],
            phoneNumber: ['', [Validators.pattern(/^[0-9]{8,15}$/)]],
            password: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', [Validators.required]],
            termsAccepted: [false, [Validators.requiredTrue]]
        }, {
            validators: this.passwordMatchValidator
        });
    }

    /**
     * Custom validator to check if passwords match
     */
    passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
        const password = control.get('password');
        const confirmPassword = control.get('confirmPassword');

        if (!password || !confirmPassword) {
            return null;
        }

        return password.value === confirmPassword.value ? null : { passwordMismatch: true };
    }

    /**
     * Toggle password visibility
     */
    togglePasswordVisibility(): void {
        this.hidePassword.set(!this.hidePassword());
    }

    /**
     * Toggle confirm password visibility
     */
    toggleConfirmPasswordVisibility(): void {
        this.hideConfirmPassword.set(!this.hideConfirmPassword());
    }

    /**
     * Get form controls for template
     */
    get firstNameControl() { return this.registerForm.get('firstName'); }
    get lastNameControl() { return this.registerForm.get('lastName'); }
    get emailControl() { return this.registerForm.get('email'); }
    get phoneControl() { return this.registerForm.get('phoneNumber'); }
    get passwordControl() { return this.registerForm.get('password'); }
    get confirmPasswordControl() { return this.registerForm.get('confirmPassword'); }
    get termsControl() { return this.registerForm.get('termsAccepted'); }

    /**
     * Handle form submission
     */
    onSubmit(): void {
        if (this.registerForm.invalid) {
            this.registerForm.markAllAsTouched();
            return;
        }

        this.isLoading.set(true);
        const { confirmPassword, termsAccepted, ...userData } = this.registerForm.value;
        const registerData: RegisterRequest = userData;

        this.authService.register(registerData).subscribe({
            next: (response) => {
                this.isLoading.set(false);
                this.router.navigate(['/']);
            },
            error: (error) => {
                this.isLoading.set(false);
                console.error('Registration failed:', error);
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
     * Error message getters
     */
    getFirstNameError(): string {
        const control = this.firstNameControl;
        if (control?.hasError('required')) return 'First name is required';
        if (control?.hasError('minlength')) return 'First name must be at least 2 characters';
        return '';
    }

    getLastNameError(): string {
        const control = this.lastNameControl;
        if (control?.hasError('required')) return 'Last name is required';
        if (control?.hasError('minlength')) return 'Last name must be at least 2 characters';
        return '';
    }

    getEmailError(): string {
        const control = this.emailControl;
        if (control?.hasError('required')) return 'Email is required';
        if (control?.hasError('email')) return 'Please enter a valid email';
        return '';
    }

    getPhoneError(): string {
        const control = this.phoneControl;
        if (control?.hasError('pattern')) return 'Please enter a valid phone number (8-15 digits)';
        return '';
    }

    getPasswordError(): string {
        const control = this.passwordControl;
        if (control?.hasError('required')) return 'Password is required';
        if (control?.hasError('minlength')) return 'Password must be at least 6 characters';
        return '';
    }

    getConfirmPasswordError(): string {
        const control = this.confirmPasswordControl;
        if (control?.hasError('required')) return 'Please confirm your password';
        if (this.registerForm.hasError('passwordMismatch')) return 'Passwords do not match';
        return '';
    }
}
