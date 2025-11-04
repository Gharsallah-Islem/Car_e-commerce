import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models';

@Component({
    selector: 'app-oauth-callback',
    standalone: true,
    imports: [
        CommonModule,
        MatProgressSpinnerModule,
        MatCardModule,
        MatIconModule
    ],
    templateUrl: './oauth-callback.component.html',
    styleUrl: './oauth-callback.component.scss'
})
export class OauthCallbackComponent implements OnInit {
    errorMessage: string | null = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        this.handleOAuthCallback();
    }

    /**
     * Handle OAuth2 callback from Google
     */
    private handleOAuthCallback(): void {
        // Get token and user from query params or fragment
        this.route.queryParams.subscribe(params => {
            const token = params['token'];
            const error = params['error'];

            if (error) {
                this.errorMessage = decodeURIComponent(error);
                setTimeout(() => {
                    this.router.navigate(['/auth/login']);
                }, 3000);
                return;
            }

            if (token) {
                console.log('OAuth token received:', token);

                // Store the token first
                this.authService.handleOAuthCallback(token);

                // The AuthService will automatically fetch user data and redirect
            } else {
                this.errorMessage = 'No authentication token received';
                setTimeout(() => {
                    this.router.navigate(['/auth/login']);
                }, 3000);
            }
        });

        // Also check URL fragment (in case backend uses fragment instead of query params)
        this.route.fragment.subscribe(fragment => {
            if (fragment) {
                const params = new URLSearchParams(fragment);
                const token = params.get('token');

                if (token) {
                    console.log('OAuth token received from fragment:', token);

                    // Store the token first
                    this.authService.handleOAuthCallback(token);

                    // The AuthService will automatically fetch user data and redirect
                }
            }
        });
    }
}
