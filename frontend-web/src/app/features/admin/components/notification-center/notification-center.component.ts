import { Component, OnInit, OnDestroy, signal, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';

import { AdminNotificationService } from '../../../../core/services/admin-notification.service';
import { AdminNotification } from '../../../../core/services/websocket.service';

@Component({
    selector: 'app-notification-center',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        MatIconModule,
        MatButtonModule,
        MatTooltipModule,
        MatBadgeModule
    ],
    templateUrl: './notification-center.component.html',
    styleUrl: './notification-center.component.scss'
})
export class NotificationCenterComponent implements OnInit, OnDestroy {
    isOpen = signal<boolean>(false);

    constructor(
        public notificationService: AdminNotificationService,
        private elementRef: ElementRef
    ) { }

    ngOnInit(): void {
        this.notificationService.initialize();
    }

    ngOnDestroy(): void { }

    toggleDropdown(): void {
        this.isOpen.update(open => !open);
        if (this.isOpen() && this.notificationService.unreadCount() > 0) {
            // Mark notifications as seen when dropdown is opened
        }
    }

    closeDropdown(): void {
        this.isOpen.set(false);
    }

    markAsRead(notification: AdminNotification, event: Event): void {
        event.stopPropagation();
        this.notificationService.markAsRead(notification.id);
    }

    markAllAsRead(): void {
        this.notificationService.markAllAsRead();
    }

    handleNotificationClick(notification: AdminNotification): void {
        if (!notification.isRead) {
            this.notificationService.markAsRead(notification.id);
        }
        this.closeDropdown();
    }

    getTimeAgo(dateString: string): string {
        const date = new Date(dateString);
        const now = new Date();
        const diffMs = now.getTime() - date.getTime();
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMins / 60);
        const diffDays = Math.floor(diffHours / 24);

        if (diffMins < 1) return 'À l\'instant';
        if (diffMins < 60) return `Il y a ${diffMins} min`;
        if (diffHours < 24) return `Il y a ${diffHours}h`;
        if (diffDays === 1) return 'Hier';
        if (diffDays < 7) return `Il y a ${diffDays} jours`;
        return date.toLocaleDateString('fr-FR');
    }

    @HostListener('document:click', ['$event'])
    onDocumentClick(event: MouseEvent): void {
        if (!this.elementRef.nativeElement.contains(event.target)) {
            this.closeDropdown();
        }
    }
}
