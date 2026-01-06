import { Injectable } from '@angular/core';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

/**
 * Premium Export Service - PDF generation with professional design
 */
@Injectable({
    providedIn: 'root'
})
export class ExportService {
    // Brand colors
    private readonly primaryColor: [number, number, number] = [139, 92, 246]; // Purple
    private readonly secondaryColor: [number, number, number] = [16, 185, 129]; // Green
    private readonly darkColor: [number, number, number] = [31, 41, 55]; // Gray 800
    private readonly lightColor: [number, number, number] = [249, 250, 251]; // Gray 50

    /**
     * Export orders to PDF
     */
    exportOrders(orders: any[]): void {
        const doc = this.createPDF('Rapport des Commandes', 'landscape');

        const columns = [
            { header: 'N° Commande', dataKey: 'id' },
            { header: 'Client', dataKey: 'customer' },
            { header: 'Date', dataKey: 'date' },
            { header: 'Statut', dataKey: 'status' },
            { header: 'Articles', dataKey: 'items' },
            { header: 'Total (TND)', dataKey: 'total' }
        ];

        const data = orders.map(order => ({
            id: this.truncateText(order.id, 12),
            customer: order.user?.fullName || order.user?.username || 'N/A',
            date: this.formatDate(order.createdAt),
            status: this.getStatusLabel(order.status),
            items: order.orderItems?.length || 0,
            total: this.formatCurrency(order.totalPrice)
        }));

        this.addTable(doc, columns, data, 45);
        this.addStats(doc, [
            { label: 'Total Commandes', value: orders.length.toString() },
            { label: 'Chiffre d\'affaires', value: this.formatCurrency(orders.reduce((sum, o) => sum + (o.totalPrice || 0), 0)) + ' TND' }
        ]);

        this.savePDF(doc, 'commandes');
    }

    /**
     * Export deliveries to PDF
     */
    exportDeliveries(deliveries: any[]): void {
        const doc = this.createPDF('Rapport des Livraisons', 'landscape');

        const columns = [
            { header: 'Tracking', dataKey: 'tracking' },
            { header: 'Destinataire', dataKey: 'recipient' },
            { header: 'Téléphone', dataKey: 'phone' },
            { header: 'Adresse', dataKey: 'address' },
            { header: 'Statut', dataKey: 'status' },
            { header: 'Livreur', dataKey: 'driver' }
        ];

        const data = deliveries.map(d => ({
            tracking: d.trackingNumber || 'N/A',
            recipient: d.recipientName || 'N/A',
            phone: d.recipientPhone || 'N/A',
            address: this.truncateText(d.deliveryAddress, 30),
            status: this.getDeliveryStatusLabel(d.status),
            driver: d.driver?.user?.fullName || 'Non assigné'
        }));

        this.addTable(doc, columns, data, 45);

        const stats = this.calculateDeliveryStats(deliveries);
        this.addStats(doc, [
            { label: 'Total Livraisons', value: deliveries.length.toString() },
            { label: 'En cours', value: stats.inProgress.toString() },
            { label: 'Livrées', value: stats.delivered.toString() }
        ]);

        this.savePDF(doc, 'livraisons');
    }

    /**
     * Export inventory to PDF
     */
    exportInventory(products: any[]): void {
        const doc = this.createPDF('Rapport d\'Inventaire', 'landscape');

        const columns = [
            { header: 'SKU', dataKey: 'sku' },
            { header: 'Produit', dataKey: 'name' },
            { header: 'Catégorie', dataKey: 'category' },
            { header: 'Stock', dataKey: 'stock' },
            { header: 'Stock Min', dataKey: 'minStock' },
            { header: 'Prix (TND)', dataKey: 'price' },
            { header: 'Statut', dataKey: 'status' }
        ];

        const data = products.map(p => ({
            sku: p.sku || 'N/A',
            name: this.truncateText(p.name, 25),
            category: p.category?.name || 'N/A',
            stock: p.stock?.toString() || '0',
            minStock: (p.minStock || 5).toString(),
            price: this.formatCurrency(p.price),
            status: this.getStockStatus(p.stock, p.minStock)
        }));

        this.addTable(doc, columns, data, 45);

        const lowStock = products.filter(p => p.stock <= (p.minStock || 5)).length;
        const outOfStock = products.filter(p => p.stock <= 0).length;

        this.addStats(doc, [
            { label: 'Produits Total', value: products.length.toString() },
            { label: 'Stock Bas', value: lowStock.toString() },
            { label: 'Rupture', value: outOfStock.toString() }
        ]);

        this.savePDF(doc, 'inventaire');
    }

    /**
     * Export stock movements to PDF
     */
    exportStockMovements(movements: any[]): void {
        const doc = this.createPDF('Mouvements de Stock', 'landscape');

        const columns = [
            { header: 'Date', dataKey: 'date' },
            { header: 'Produit', dataKey: 'product' },
            { header: 'Type', dataKey: 'type' },
            { header: 'Quantité', dataKey: 'quantity' },
            { header: 'Avant', dataKey: 'before' },
            { header: 'Après', dataKey: 'after' },
            { header: 'Référence', dataKey: 'reference' }
        ];

        const data = movements.map(m => ({
            date: this.formatDate(m.createdAt),
            product: this.truncateText(m.product?.name || 'N/A', 20),
            type: this.getMovementTypeLabel(m.movementType),
            quantity: (m.quantity > 0 ? '+' : '') + m.quantity,
            before: m.previousStock?.toString() || '0',
            after: m.newStock?.toString() || '0',
            reference: m.referenceType || 'N/A'
        }));

        this.addTable(doc, columns, data, 45);
        this.savePDF(doc, 'mouvements_stock');
    }

    /**
     * Export analytics report to PDF
     */
    exportAnalyticsReport(stats: any): void {
        const doc = this.createPDF('Tableau de Bord Analytics', 'portrait');

        // KPI Section
        const kpis = [
            { label: 'Chiffre d\'affaires total', value: this.formatCurrency(stats.totalRevenue || 0) + ' TND' },
            { label: 'Nombre de commandes', value: (stats.totalOrders || 0).toString() },
            { label: 'Commandes en attente', value: (stats.pendingOrders || 0).toString() },
            { label: 'Commandes livrées', value: (stats.deliveredOrders || 0).toString() },
            { label: 'Nouveaux clients (ce mois)', value: (stats.newCustomers || 0).toString() },
            { label: 'Panier moyen', value: this.formatCurrency(stats.averageOrderValue || 0) + ' TND' },
            { label: 'Produits en stock', value: (stats.productsInStock || 0).toString() },
            { label: 'Produits en rupture', value: (stats.outOfStockProducts || 0).toString() }
        ];

        const columns = [
            { header: 'Indicateur', dataKey: 'label' },
            { header: 'Valeur', dataKey: 'value' }
        ];

        this.addTable(doc, columns, kpis, 45);
        this.savePDF(doc, 'rapport_analytics');
    }

    // ==================== PDF CREATION HELPERS ====================

    /**
     * Create PDF with professional header
     */
    private createPDF(title: string, orientation: 'portrait' | 'landscape' = 'portrait'): jsPDF {
        const doc = new jsPDF({ orientation, unit: 'mm', format: 'a4' });

        const pageWidth = doc.internal.pageSize.getWidth();

        // Header background gradient effect
        doc.setFillColor(...this.primaryColor);
        doc.rect(0, 0, pageWidth, 35, 'F');

        // Logo/Brand
        doc.setTextColor(255, 255, 255);
        doc.setFontSize(22);
        doc.setFont('helvetica', 'bold');
        doc.text('CarPartsHub', 15, 18);

        // Subtitle
        doc.setFontSize(10);
        doc.setFont('helvetica', 'normal');
        doc.text('Système de Gestion Admin', 15, 26);

        // Title
        doc.setFontSize(14);
        doc.setFont('helvetica', 'bold');
        doc.text(title, pageWidth - 15, 18, { align: 'right' });

        // Date
        doc.setFontSize(9);
        doc.setFont('helvetica', 'normal');
        const now = new Date();
        doc.text(`Généré le ${now.toLocaleDateString('fr-FR')} à ${now.toLocaleTimeString('fr-FR')}`, pageWidth - 15, 26, { align: 'right' });

        return doc;
    }

    /**
     * Add styled table to PDF
     */
    private addTable(doc: jsPDF, columns: any[], data: any[], startY: number): void {
        autoTable(doc, {
            columns: columns,
            body: data,
            startY: startY,
            theme: 'striped',
            headStyles: {
                fillColor: this.primaryColor,
                textColor: [255, 255, 255],
                fontStyle: 'bold',
                fontSize: 10,
                cellPadding: 4
            },
            bodyStyles: {
                fontSize: 9,
                cellPadding: 3,
                textColor: this.darkColor
            },
            alternateRowStyles: {
                fillColor: this.lightColor
            },
            styles: {
                overflow: 'linebreak',
                lineWidth: 0.1,
                lineColor: [200, 200, 200]
            },
            margin: { left: 15, right: 15 },
            didParseCell: (data: any) => {
                // Color code status cells
                if (data.column.dataKey === 'status') {
                    const value = data.cell.raw?.toString().toLowerCase();
                    if (value?.includes('livr') || value?.includes('ok') || value?.includes('confirm')) {
                        data.cell.styles.textColor = [16, 185, 129]; // Green
                    } else if (value?.includes('rupture') || value?.includes('annul') || value?.includes('échec')) {
                        data.cell.styles.textColor = [239, 68, 68]; // Red
                    } else if (value?.includes('attente') || value?.includes('bas') || value?.includes('prépar')) {
                        data.cell.styles.textColor = [245, 158, 11]; // Amber
                    }
                }
                // Highlight quantity changes
                if (data.column.dataKey === 'quantity') {
                    const value = data.cell.raw?.toString();
                    if (value?.startsWith('+')) {
                        data.cell.styles.textColor = [16, 185, 129]; // Green
                    } else if (value?.startsWith('-')) {
                        data.cell.styles.textColor = [239, 68, 68]; // Red
                    }
                }
            }
        });
    }

    /**
     * Add stats footer
     */
    private addStats(doc: jsPDF, stats: { label: string; value: string }[]): void {
        const pageHeight = doc.internal.pageSize.getHeight();
        const pageWidth = doc.internal.pageSize.getWidth();

        // Footer background
        doc.setFillColor(...this.darkColor);
        doc.rect(0, pageHeight - 25, pageWidth, 25, 'F');

        // Stats
        const startX = 20;
        const spacing = (pageWidth - 40) / stats.length;

        doc.setTextColor(255, 255, 255);
        stats.forEach((stat, index) => {
            const x = startX + (spacing * index);
            doc.setFontSize(8);
            doc.setFont('helvetica', 'normal');
            doc.text(stat.label, x, pageHeight - 15);
            doc.setFontSize(14);
            doc.setFont('helvetica', 'bold');
            doc.text(stat.value, x, pageHeight - 8);
        });
    }

    /**
     * Save PDF
     */
    private savePDF(doc: jsPDF, filename: string): void {
        const dateStamp = this.getDateStamp();
        doc.save(`${filename}_${dateStamp}.pdf`);
    }

    // ==================== FORMATTING HELPERS ====================

    private formatDate(date: Date | string | null): string {
        if (!date) return 'N/A';
        const d = new Date(date);
        return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
    }

    private formatCurrency(value: number | null): string {
        if (value === null || value === undefined) return '0.00';
        return value.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
    }

    private truncateText(text: string | null, maxLength: number): string {
        if (!text) return 'N/A';
        return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    }

    private getDateStamp(): string {
        const now = new Date();
        return `${now.getFullYear()}${(now.getMonth() + 1).toString().padStart(2, '0')}${now.getDate().toString().padStart(2, '0')}`;
    }

    // ==================== STATUS LABELS ====================

    private getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            'PENDING': 'En attente',
            'CONFIRMED': 'Confirmée',
            'PROCESSING': 'En préparation',
            'SHIPPED': 'Expédiée',
            'DELIVERED': 'Livrée',
            'CANCELLED': 'Annulée'
        };
        return labels[status] || status;
    }

    private getDeliveryStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            'PROCESSING': 'En attente',
            'IN_TRANSIT': 'En transit',
            'OUT_FOR_DELIVERY': 'En livraison',
            'DELIVERED': 'Livrée',
            'FAILED': 'Échec',
            'CANCELLED': 'Annulée'
        };
        return labels[status] || status;
    }

    private getStockStatus(stock: number, minStock: number = 5): string {
        if (stock <= 0) return 'Rupture';
        if (stock <= minStock) return 'Stock Bas';
        return 'OK';
    }

    private getMovementTypeLabel(type: string): string {
        const labels: { [key: string]: string } = {
            'SALE': 'Vente',
            'PURCHASE': 'Achat',
            'ADJUSTMENT': 'Ajustement',
            'RETURN': 'Retour',
            'TRANSFER': 'Transfert'
        };
        return labels[type] || type;
    }

    private calculateDeliveryStats(deliveries: any[]): { inProgress: number; delivered: number } {
        return {
            inProgress: deliveries.filter(d => ['IN_TRANSIT', 'OUT_FOR_DELIVERY', 'PROCESSING'].includes(d.status)).length,
            delivered: deliveries.filter(d => d.status === 'DELIVERED').length
        };
    }
}
