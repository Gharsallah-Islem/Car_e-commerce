import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reset-password',
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
    MatProgressSpinnerModule
  ],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm: FormGroup;
  isLoading = signal(false);
  email: string = '';
  hidePassword = signal(true);
  hideConfirmPassword = signal(true);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.resetPasswordForm = this.fb.group({
      code: ['', [
        Validators.required,
        Validators.pattern(/^\d{6}$/)
      ]],
      newPassword: ['', [
        Validators.required,
        Validators.minLength(6)
      ]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  ngOnInit(): void {
    // Get email from query params
    this.route.queryParams.subscribe(params => {
      this.email = params['email'];
      if (!this.email) {
        // Redirect to forgot password if no email provided
        this.router.navigate(['/auth/forgot-password']);
      }
    });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const newPassword = control.get('newPassword');
    const confirmPassword = control.get('confirmPassword');

    if (!newPassword || !confirmPassword) {
      return null;
    }

    return newPassword.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  onSubmit(): void {
    if (this.resetPasswordForm.valid && !this.isLoading()) {
      this.isLoading.set(true);
      const { code, newPassword } = this.resetPasswordForm.value;

      this.authService.resetPassword(this.email, code, newPassword).subscribe({
        next: () => {
          this.isLoading.set(false);
          // Navigate to login after successful reset
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 1500);
        },
        error: () => {
          this.isLoading.set(false);
        }
      });
    }
  }

  getCodeErrorMessage(): string {
    const codeControl = this.resetPasswordForm.get('code');
    if (codeControl?.hasError('required')) {
      return 'Verification code is required';
    }
    if (codeControl?.hasError('pattern')) {
      return 'Code must be exactly 6 digits';
    }
    return '';
  }

  getPasswordErrorMessage(): string {
    const passwordControl = this.resetPasswordForm.get('newPassword');
    if (passwordControl?.hasError('required')) {
      return 'Password is required';
    }
    if (passwordControl?.hasError('minlength')) {
      return 'Password must be at least 6 characters';
    }
    return '';
  }

  getConfirmPasswordErrorMessage(): string {
    const confirmPasswordControl = this.resetPasswordForm.get('confirmPassword');
    if (confirmPasswordControl?.hasError('required')) {
      return 'Please confirm your password';
    }
    if (this.resetPasswordForm.hasError('passwordMismatch')) {
      return 'Passwords do not match';
    }
    return '';
  }

  togglePasswordVisibility(): void {
    this.hidePassword.update(value => !value);
  }

  toggleConfirmPasswordVisibility(): void {
    this.hideConfirmPassword.update(value => !value);
  }
}
