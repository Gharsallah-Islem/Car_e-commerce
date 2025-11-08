import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-verify-email',
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
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.scss'
})
export class VerifyEmailComponent implements OnInit {
  verifyForm: FormGroup;
  isLoading = signal(false);
  isResending = signal(false);
  email: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.verifyForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });
  }

  ngOnInit(): void {
    // Get email from query params
    this.route.queryParams.subscribe(params => {
      this.email = params['email'] || '';
      if (!this.email) {
        // If no email provided, redirect to login
        this.router.navigate(['/auth/login']);
      }
    });
  }

  get codeControl() {
    return this.verifyForm.get('code');
  }

  onSubmit(): void {
    if (this.verifyForm.invalid) {
      this.verifyForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const code = this.verifyForm.value.code;

    this.authService.verifyEmail(this.email, code).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        // Redirect to login after successful verification
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 1500);
      },
      error: (error) => {
        this.isLoading.set(false);
        console.error('Verification failed:', error);
      }
    });
  }

  resendCode(): void {
    this.isResending.set(true);

    this.authService.resendVerificationEmail(this.email).subscribe({
      next: (response) => {
        this.isResending.set(false);
      },
      error: (error) => {
        this.isResending.set(false);
        console.error('Resend failed:', error);
      }
    });
  }

  getCodeErrorMessage(): string {
    const control = this.codeControl;
    if (control?.hasError('required')) {
      return 'Verification code is required';
    }
    if (control?.hasError('pattern')) {
      return 'Code must be 6 digits';
    }
    return '';
  }
}
