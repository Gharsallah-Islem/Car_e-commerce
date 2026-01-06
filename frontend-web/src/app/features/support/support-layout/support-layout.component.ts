import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';

import { AuthService } from '../../../core/services/auth.service';
import { ReclamationService } from '../../../core/services/reclamation.service';

interface NavItem {
    icon: string;
    label: string;
    route: string;
    badge?: number;
}

@Component({
    selector: 'app-support-layout',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        RouterOutlet,
        MatSidenavModule,
        MatToolbarModule,
        MatListModule,
        MatIconModule,
        MatButtonModule,
        MatBadgeModule,
        MatMenuModule,
        MatDividerModule
    ],
    templateUrl: './support-layout.component.html',
    styleUrl: './support-layout.component.scss'
})
export class SupportLayoutComponent {
    private authService = inject(AuthService);
    private router = inject(Router);
    private reclamationService = inject(ReclamationService);

    currentUser = this.authService.currentUser$;
    sidenavOpened = signal<boolean>(true);
    pendingTickets = signal<number>(0);
    unreadMessages = signal<number>(0);

    navItems: NavItem[] = [
        { icon: 'dashboard', label: 'Tableau de bord', route: '/support/dashboard' },
        { icon: 'confirmation_number', label: 'Tickets', route: '/support/tickets' },
        { icon: 'chat', label: 'Chat en direct', route: '/support/chat' },
        { icon: 'analytics', label: 'Ma Performance', route: '/support/performance' }
    ];

    constructor() {
        this.loadCounts();
    }

    private loadCounts(): void {
        this.reclamationService.getStats().subscribe({
            next: (stats) => {
                this.pendingTickets.set(stats.openTickets || 0);
            }
        });
    }

    toggleSidenav(): void {
        this.sidenavOpened.update(v => !v);
    }

    getBadge(route: string): number | undefined {
        if (route.includes('tickets')) {
            return this.pendingTickets() > 0 ? this.pendingTickets() : undefined;
        }
        if (route.includes('chat')) {
            return this.unreadMessages() > 0 ? this.unreadMessages() : undefined;
        }
        return undefined;
    }

    logout(): void {
        this.authService.logout();
        this.router.navigate(['/auth/login']);
    }

    goToHome(): void {
        this.router.navigate(['/']);
    }

    getInitials(): string {
        const user = this.authService.getCurrentUser();
        if (!user) return 'S';
        const firstName = (user as any).firstName || '';
        const lastName = (user as any).lastName || '';
        return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase() || 'S';
    }
}
