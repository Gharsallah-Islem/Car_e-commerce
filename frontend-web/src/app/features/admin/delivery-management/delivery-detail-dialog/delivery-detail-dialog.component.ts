import { Component, Inject, OnInit, signal, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { Delivery } from '../../../../core/services/delivery.service';
import * as L from 'leaflet';

@Component({
    selector: 'app-delivery-detail-dialog',
    standalone: true,
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        MatChipsModule,
        MatDividerModule,
        MatProgressBarModule,
        MatTooltipModule,
        RouterLink
    ],
    templateUrl: './delivery-detail-dialog.component.html',
    styleUrls: ['./delivery-detail-dialog.component.scss']
})
export class DeliveryDetailDialogComponent implements OnInit, AfterViewInit {
    @ViewChild('mapContainer') mapContainer!: ElementRef;

    delivery = signal<Delivery | null>(null);
    private map: L.Map | null = null;

    // Delivery timeline steps
    timelineSteps = [
        { key: 'PROCESSING', label: 'Commande préparée', icon: 'inventory' },
        { key: 'PICKED_UP', label: 'Récupérée par livreur', icon: 'local_shipping' },
        { key: 'IN_TRANSIT', label: 'En transit', icon: 'directions_car' },
        { key: 'OUT_FOR_DELIVERY', label: 'En cours de livraison', icon: 'delivery_dining' },
        { key: 'DELIVERED', label: 'Livrée', icon: 'check_circle' }
    ];

    constructor(
        public dialogRef: MatDialogRef<DeliveryDetailDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: { delivery: Delivery }
    ) { }

    ngOnInit(): void {
        this.delivery.set(this.data.delivery);
    }

    ngAfterViewInit(): void {
        setTimeout(() => this.initMap(), 100);
    }

    private initMap(): void {
        if (!this.mapContainer?.nativeElement) return;

        const delivery = this.delivery();
        const lat = delivery?.currentLatitude || 36.8065;
        const lng = delivery?.currentLongitude || 10.1815;

        this.map = L.map(this.mapContainer.nativeElement, {
            center: [lat, lng],
            zoom: 13,
            zoomControl: false,
            attributionControl: false
        });

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19
        }).addTo(this.map);

        // Add marker
        const icon = L.divIcon({
            html: `<div class="delivery-marker"><span>🚚</span></div>`,
            className: 'custom-marker',
            iconSize: [40, 40],
            iconAnchor: [20, 20]
        });

        L.marker([lat, lng], { icon }).addTo(this.map);

        // Invalidate size after render
        setTimeout(() => this.map?.invalidateSize(), 200);
    }

    getStatusIndex(): number {
        const delivery = this.delivery();
        if (!delivery) return 0;
        const idx = this.timelineSteps.findIndex(s => s.key === delivery.status);
        return idx >= 0 ? idx : 0;
    }

    getProgressPercent(): number {
        const idx = this.getStatusIndex();
        return ((idx + 1) / this.timelineSteps.length) * 100;
    }

    isStepCompleted(stepIndex: number): boolean {
        return stepIndex <= this.getStatusIndex();
    }

    isStepCurrent(stepIndex: number): boolean {
        return stepIndex === this.getStatusIndex();
    }

    getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            'PROCESSING': 'En préparation',
            'PICKED_UP': 'Récupéré',
            'IN_TRANSIT': 'En transit',
            'OUT_FOR_DELIVERY': 'En cours de livraison',
            'DELIVERED': 'Livrée',
            'FAILED': 'Échec',
            'CANCELLED': 'Annulée'
        };
        return labels[status] || status;
    }

    getStatusColor(status: string): string {
        const colors: { [key: string]: string } = {
            'PROCESSING': '#F59E0B',
            'PICKED_UP': '#3B82F6',
            'IN_TRANSIT': '#6366F1',
            'OUT_FOR_DELIVERY': '#8B5CF6',
            'DELIVERED': '#10B981',
            'FAILED': '#EF4444',
            'CANCELLED': '#6B7280'
        };
        return colors[status] || '#6B7280';
    }

    close(): void {
        this.dialogRef.close();
    }

    trackOnMap(): void {
        const delivery = this.delivery();
        if (delivery?.trackingNumber) {
            this.dialogRef.close({ action: 'track', trackingNumber: delivery.trackingNumber });
        }
    }

    ngOnDestroy(): void {
        if (this.map) {
            this.map.remove();
            this.map = null;
        }
    }
}
