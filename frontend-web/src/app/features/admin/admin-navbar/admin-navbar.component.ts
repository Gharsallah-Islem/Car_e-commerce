import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-admin-navbar',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        MatToolbarModule,
        MatButtonModule,
        MatIconModule,
        MatMenuModule,
        MatBadgeModule,
        MatDividerModule
    ],
    templateUrl: './admin-navbar.component.html',
    styleUrls: ['./admin-navbar.component.scss']
})
export class AdminNavbarComponent {
    private authService = inject(AuthService);
    private router = inject(Router);

    currentUser$ = this.authService.currentUser$;

    logout() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }

    goToHome() {
        this.router.navigate(['/']);
    }
}
