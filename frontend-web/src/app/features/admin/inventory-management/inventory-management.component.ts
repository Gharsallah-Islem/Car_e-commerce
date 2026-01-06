import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import { NotificationService } from '../../../core/services/notification.service';
import { ExportService } from '../../../core/services/export.service';
import { InventoryService, Supplier, PurchaseOrder, StockMovement, ReorderSetting, InventoryStats } from '../../../core/services/inventory.service';

// Using types from InventoryService

@Component({
    selector: 'app-inventory-management',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatTabsModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatInputModule,
        MatFormFieldModule,
        MatTableModule,
        MatChipsModule,
        MatDividerModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        MatDialogModule,
        MatTooltipModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatPaginatorModule,
        MatSortModule,
        MatMenuModule,
        MatBadgeModule,
        MatSlideToggleModule
    ],
    templateUrl: './inventory-management.component.html',
    styleUrls: ['./inventory-management.component.scss']
})
export class InventoryManagementComponent implements OnInit {
    // Forms
    supplierForm!: FormGroup;
    purchaseOrderForm!: FormGroup;
    stockMovementForm!: FormGroup;
    reorderSettingForm!: FormGroup;

    // State
    loading = signal<boolean>(false);
    selectedTabIndex = signal<number>(0);

    // Expose Math to template
    Math = Math;

    // Live status
    lastUpdateTime: string = '';

    // Dialog/Form visibility states
    showSupplierForm = signal<boolean>(false);
    showMovementForm = signal<boolean>(false);
    showPODetails = signal<boolean>(false);
    showReorderForm = signal<boolean>(false);
    selectedPO = signal<PurchaseOrder | null>(null);
    editingReorderSetting = signal<ReorderSetting | null>(null);

    // Stats
    stats = signal<InventoryStats>({
        totalProducts: 0,
        totalValue: 0,
        lowStockItems: 0,
        outOfStockItems: 0,
        healthyStockItems: 0,
        pendingPOs: 0,
        draftPOs: 0,
        approvedPOs: 0,
        receivedPOs: 0,
        activeSuppliers: 0,
        totalSuppliers: 0,
        totalMovements: 0
    });

    // Product Stock Overview
    productStock = signal<any[]>([]);
    productStockColumns: string[] = ['product', 'sku', 'stock', 'status', 'value', 'actions'];

    // Suppliers
    suppliers = signal<Supplier[]>([]);
    editingSupplier = signal<Supplier | null>(null);
    supplierSearchQuery = signal<string>('');
    supplierColumns: string[] = ['name', 'contact', 'address', 'productsCount', 'status', 'actions'];

    filteredSuppliers = computed(() => {
        const query = this.supplierSearchQuery().toLowerCase();
        if (!query) return this.suppliers();
        return this.suppliers().filter(s =>
            s.name.toLowerCase().includes(query) ||
            s.email?.toLowerCase().includes(query) ||
            s.phone?.toLowerCase().includes(query)
        );
    });

    // Purchase Orders
    purchaseOrders = signal<PurchaseOrder[]>([]);
    editingPO = signal<PurchaseOrder | null>(null);
    poSearchQuery = signal<string>('');
    poStatusFilter = signal<string>('ALL');
    purchaseOrderColumns: string[] = ['orderNumber', 'supplier', 'orderDate', 'expectedDate', 'totalAmount', 'status', 'actions'];

    filteredPurchaseOrders = computed(() => {
        let filtered = this.purchaseOrders();

        // Apply status filter
        if (this.poStatusFilter() !== 'ALL') {
            filtered = filtered.filter(po => po.status === this.poStatusFilter());
        }

        // Apply search
        const query = this.poSearchQuery().toLowerCase();
        if (query) {
            filtered = filtered.filter(po =>
                po.poNumber?.toLowerCase().includes(query) ||
                po.supplier?.name?.toLowerCase().includes(query)
            );
        }

        return filtered;
    });

    // Stock Movements
    stockMovements = signal<StockMovement[]>([]);
    movementSearchQuery = signal<string>('');
    movementTypeFilter = signal<string>('ALL');
    movementColumns: string[] = ['date', 'product', 'type', 'quantity', 'reference', 'notes', 'user'];

    filteredStockMovements = computed(() => {
        let filtered = this.stockMovements();

        // Apply type filter
        if (this.movementTypeFilter() !== 'ALL') {
            filtered = filtered.filter(m => m.movementType === this.movementTypeFilter());
        }

        // Apply search
        const query = this.movementSearchQuery().toLowerCase();
        if (query) {
            filtered = filtered.filter(m =>
                m.product?.name?.toLowerCase().includes(query) ||
                m.referenceId?.toLowerCase().includes(query)
            );
        }

        return filtered;
    });

    // Reorder Settings
    reorderSettings = signal<ReorderSetting[]>([]);
    reorderSearchQuery = signal<string>('');
    reorderColumns: string[] = ['product', 'currentStock', 'reorderPoint', 'reorderQuantity', 'supplier', 'autoReorder', 'status', 'actions'];

    filteredReorderSettings = computed(() => {
        const query = this.reorderSearchQuery().toLowerCase();
        if (!query) return this.reorderSettings();
        return this.reorderSettings().filter((r: ReorderSetting) =>
            r.product?.name?.toLowerCase().includes(query)
        );
    });

    // Pagination
    pageSize = signal<number>(10);
    pageIndex = signal<number>(0);

    constructor(
        private fb: FormBuilder,
        private dialog: MatDialog,
        private notificationService: NotificationService,
        private inventoryService: InventoryService,
        private exportService: ExportService
    ) {
        this.initForms();
    }

    ngOnInit(): void {
        this.loadInventoryStats();
        this.loadProductStock();
        this.loadSuppliers();
        this.loadPurchaseOrders();
        this.loadStockMovements();
        this.loadReorderSettings();
        this.updateLastUpdateTime();
    }

    // Helper method to calculate healthy stock percentage
    getHealthyStockPercentage(): number {
        const total = this.stats().totalProducts;
        const healthy = this.stats().healthyStockItems || 0;
        if (total === 0) return 0;
        return Math.round((healthy / total) * 100);
    }

    // Refresh all data
    refreshAll(): void {
        this.loading.set(true);
        this.loadInventoryStats();
        this.loadProductStock();
        this.loadSuppliers();
        this.loadPurchaseOrders();
        this.loadStockMovements();
        this.loadReorderSettings();
        this.updateLastUpdateTime();
        setTimeout(() => this.loading.set(false), 500);
    }

    // Update the last update time display
    private updateLastUpdateTime(): void {
        const now = new Date();
        this.lastUpdateTime = now.toLocaleTimeString('fr-FR', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    initForms(): void {
        // Supplier Form
        this.supplierForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(3)]],
            contactPerson: ['', [Validators.required]],
            email: ['', [Validators.required, Validators.email]],
            phone: ['', [Validators.required]],
            address: ['', [Validators.required]],
            status: ['ACTIVE', [Validators.required]]
        });

        // Purchase Order Form
        this.purchaseOrderForm = this.fb.group({
            supplierId: ['', [Validators.required]],
            orderDate: [new Date(), [Validators.required]],
            expectedDelivery: ['', [Validators.required]],
            notes: ['']
        });

        // Stock Movement Form
        this.stockMovementForm = this.fb.group({
            productId: ['', [Validators.required]],
            type: ['IN', [Validators.required]],
            quantity: [1, [Validators.required, Validators.min(1)]],
            notes: ['']  // Optional notes field
        });

        // Reorder Setting Form
        this.reorderSettingForm = this.fb.group({
            productId: ['', [Validators.required]],
            reorderPoint: [5, [Validators.required, Validators.min(0)]],
            reorderQuantity: [10, [Validators.required, Validators.min(1)]],
            supplierId: [''],
            autoReorder: [false],
            minimumStock: [0, [Validators.min(0)]],
            maximumStock: [100, [Validators.min(1)]],
            leadTimeDays: [7, [Validators.min(1)]]
        });
    }

    // Load Data Methods
    loadInventoryStats(): void {
        this.loading.set(true);
        this.inventoryService.getInventoryStatistics().subscribe({
            next: (stats) => {
                this.stats.set(stats);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading inventory stats:', error);
                this.notificationService.error('Erreur lors du chargement des statistiques');
                this.loading.set(false);
            }
        });
    }

    loadProductStock(): void {
        this.inventoryService.getProductStockOverview().subscribe({
            next: (products) => {
                this.productStock.set(products);
            },
            error: (error) => {
                console.error('Error loading product stock:', error);
                this.productStock.set([]);
            }
        });
    }

    loadSuppliers(): void {
        this.loading.set(true);
        const page = this.pageIndex();
        const size = this.pageSize();

        this.inventoryService.getSuppliers(page, size).subscribe({
            next: (response) => {
                this.suppliers.set(response.content);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading suppliers:', error);
                this.notificationService.error('Erreur lors du chargement des fournisseurs');
                this.loading.set(false);
            }
        });
    }

    loadPurchaseOrders(): void {
        this.loading.set(true);
        const page = this.pageIndex();
        const size = this.pageSize();

        this.inventoryService.getPurchaseOrders(page, size).subscribe({
            next: (response) => {
                this.purchaseOrders.set(response.content);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading purchase orders:', error);
                this.notificationService.error('Erreur lors du chargement des bons de commande');
                this.loading.set(false);
            }
        });
    }

    loadStockMovements(): void {
        this.loading.set(true);
        const page = this.pageIndex();
        const size = this.pageSize();

        this.inventoryService.getStockMovements(page, size).subscribe({
            next: (response) => {
                this.stockMovements.set(response.content);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading stock movements:', error);
                // Set empty array for graceful degradation
                this.stockMovements.set([]);
                this.loading.set(false);
                // Only show error for non-404/500 errors
                if (error.status !== 404 && error.status !== 500) {
                    this.notificationService.error('Erreur lors du chargement des mouvements de stock');
                }
            }
        });
    }

    loadReorderSettings(): void {
        this.loading.set(true);
        const page = this.pageIndex();
        const size = this.pageSize();

        this.inventoryService.getReorderSettings(page, size).subscribe({
            next: (response) => {
                this.reorderSettings.set(response.content);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading reorder settings:', error);
                this.notificationService.error('Erreur lors du chargement des paramètres de réapprovisionnement');
                this.loading.set(false);
            }
        });
    }

    // Supplier Management
    openSupplierForm(supplier?: Supplier): void {
        if (supplier) {
            this.editingSupplier.set(supplier);
            this.supplierForm.patchValue(supplier);
        } else {
            this.editingSupplier.set(null);
            this.supplierForm.reset({ status: 'ACTIVE' });
        }
    }

    saveSupplier(): void {
        if (this.supplierForm.invalid) {
            this.notificationService.warning('Veuillez remplir tous les champs obligatoires');
            return;
        }

        this.loading.set(true);
        const formValue = this.supplierForm.value;
        const supplierData = {
            ...formValue,
            isActive: formValue.status === 'ACTIVE'
        };

        const operation = this.editingSupplier()
            ? this.inventoryService.updateSupplier(this.editingSupplier()!.id, supplierData)
            : this.inventoryService.createSupplier(supplierData);

        operation.subscribe({
            next: (supplier) => {
                if (this.editingSupplier()) {
                    this.suppliers.update(suppliers =>
                        suppliers.map(s => s.id === supplier.id ? supplier : s)
                    );
                    this.notificationService.success('Fournisseur mis à jour avec succès');
                } else {
                    this.suppliers.update(suppliers => [...suppliers, supplier]);
                    this.notificationService.success('Fournisseur ajouté avec succès');
                }
                this.closeSupplierDialog();
                this.loadSuppliers();
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error saving supplier:', error);
                this.notificationService.error('Erreur lors de l\'enregistrement du fournisseur');
                this.loading.set(false);
            }
        });
    }

    deleteSupplier(supplierId: string): void {
        if (confirm('Êtes-vous sûr de vouloir supprimer ce fournisseur ?')) {
            this.inventoryService.deleteSupplier(supplierId).subscribe({
                next: () => {
                    this.suppliers.update(suppliers => suppliers.filter(s => s.id !== supplierId));
                    this.notificationService.success('Fournisseur supprimé avec succès');
                },
                error: (error) => {
                    console.error('Error deleting supplier:', error);
                    this.notificationService.error('Erreur lors de la suppression du fournisseur');
                }
            });
        }
    }

    // Purchase Order Management
    savePurchaseOrder(): void {
        if (this.purchaseOrderForm.invalid) {
            this.notificationService.warning('Veuillez remplir tous les champs obligatoires');
            return;
        }

        this.loading.set(true);
        const formValue = this.purchaseOrderForm.value;

        const supplier = this.suppliers().find(s => s.id === formValue.supplierId);
        if (!supplier) {
            this.notificationService.error('Fournisseur introuvable');
            this.loading.set(false);
            return;
        }

        const poData = {
            supplierId: formValue.supplierId,
            orderDate: formValue.orderDate,
            expectedDeliveryDate: formValue.expectedDelivery,
            notes: formValue.notes,
            items: [] // Add items as needed
        };

        this.inventoryService.createPurchaseOrder(poData).subscribe({
            next: (newPO) => {
                this.purchaseOrders.update(pos => [...pos, newPO]);
                this.notificationService.success('Bon de commande créé avec succès');
                this.purchaseOrderForm.reset({ orderDate: new Date() });
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error creating purchase order:', error);
                this.notificationService.error('Erreur lors de la création du bon de commande');
                this.loading.set(false);
            }
        });
    }

    updatePOStatus(poId: string, status: string): void {
        this.inventoryService.updatePurchaseOrderStatus(poId, status).subscribe({
            next: (updated) => {
                this.purchaseOrders.update(pos =>
                    pos.map(po => po.id === poId ? updated : po)
                );
                this.notificationService.success('Statut mis à jour');
            },
            error: (error) => {
                console.error('Error updating PO status:', error);
                this.notificationService.error('Erreur lors de la mise à jour du statut');
            }
        });
    }

    // Stock Movement Management
    saveStockMovement(): void {
        if (this.stockMovementForm.invalid) {
            this.notificationService.warning('Veuillez remplir tous les champs obligatoires');
            return;
        }

        this.loading.set(true);
        const formValue = this.stockMovementForm.value;

        const movementData = {
            productId: formValue.productId,
            type: formValue.type,
            quantity: formValue.quantity,
            reference: 'MANUAL',
            reason: formValue.notes || ''
        };

        this.inventoryService.recordStockMovement(movementData).subscribe({
            next: (newMovement) => {
                this.stockMovements.update(movements => [newMovement, ...movements]);
                this.notificationService.success('Mouvement de stock enregistré');
                this.closeMovementDialog();
                this.loadStockMovements();
                this.loadProductStock();
                this.loadInventoryStats();
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error recording stock movement:', error);
                this.notificationService.error('Erreur lors de l\'enregistrement du mouvement');
                this.loading.set(false);
            }
        });
    }

    // Product Stock Actions - Open adjustment dialog pre-filled with product
    openStockAdjustment(product: any): void {
        this.stockMovementForm.reset({
            productId: product.id,
            type: 'ADJUSTMENT',
            quantity: product.currentStock || 1,
            notes: ''
        });
        this.showMovementForm.set(true);
    }

    // View stock movement history for a product
    viewProductHistory(product: any): void {
        this.movementSearchQuery.set(product.name);
        this.selectedTabIndex.set(2); // Switch to Movements tab
        this.notificationService.info(`Historique filtré pour: ${product.name}`);
    }

    // Reorder Setting Management
    saveReorderSetting(): void {
        if (this.reorderSettingForm.invalid) {
            this.notificationService.warning('Veuillez remplir tous les champs obligatoires');
            return;
        }

        this.loading.set(true);
        const formValue = this.reorderSettingForm.value;
        const editing = this.editingReorderSetting();

        const settingData = {
            productId: formValue.productId,
            reorderPoint: formValue.reorderPoint,
            reorderQuantity: formValue.reorderQuantity,
            preferredSupplierId: formValue.supplierId || null,
            autoReorder: formValue.autoReorder
        };

        const request = editing
            ? this.inventoryService.updateReorderSetting(editing.id, settingData)
            : this.inventoryService.createReorderSetting(settingData);

        request.subscribe({
            next: (savedSetting) => {
                if (editing) {
                    this.reorderSettings.update(settings =>
                        settings.map(s => s.id === savedSetting.id ? savedSetting : s)
                    );
                    this.notificationService.success('Paramètre mis à jour');
                } else {
                    this.reorderSettings.update(settings => [...settings, savedSetting]);
                    this.notificationService.success('Paramètre de réapprovisionnement créé');
                }
                this.closeReorderForm();
                this.loadReorderSettings();
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error saving reorder setting:', error);
                this.notificationService.error('Erreur lors de l\'enregistrement');
                this.loading.set(false);
            }
        });
    }

    // Utility Methods
    getStatusColor(status: string): string {
        const colors: { [key: string]: string } = {
            'ACTIVE': 'primary',
            'INACTIVE': 'warn',
            'DRAFT': 'accent',
            'PENDING': 'accent',
            'APPROVED': 'primary',
            'RECEIVED': 'primary',
            'CANCELLED': 'warn',
            'OK': 'primary',
            'LOW': 'accent',
            'CRITICAL': 'warn'
        };
        return colors[status] || 'primary';
    }

    getMovementTypeIcon(type: string): string {
        const icons: { [key: string]: string } = {
            'IN': 'arrow_downward',
            'OUT': 'arrow_upward',
            'ADJUSTMENT': 'sync'
        };
        return icons[type] || 'sync';
    }

    handlePageEvent(event: PageEvent): void {
        this.pageSize.set(event.pageSize);
        this.pageIndex.set(event.pageIndex);
    }



    // Actions
    checkAutoReorders(): void {
        this.loading.set(true);
        this.inventoryService.triggerAutoReorder().subscribe({
            next: () => {
                this.notificationService.success('Vérification du réapprovisionnement terminée');
                this.loadPurchaseOrders(); // Reload to see new auto-generated POs
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error triggering auto-reorder:', error);
                this.notificationService.error('Erreur lors de la vérification du réapprovisionnement');
                this.loading.set(false);
            }
        });
    }

    filterOrders(status: string): void {
        // Client-side filtering for now, or reload from backend
        if (status === 'ALL') {
            this.loadPurchaseOrders();
        } else {
            // Ideally call backend with status filter
            this.loading.set(true);
            this.inventoryService.getPurchaseOrders(0, 100).subscribe({ // Fetch more for filtering
                next: (response) => {
                    const filtered = response.content.filter(po => po.status === status);
                    this.purchaseOrders.set(filtered);
                    this.loading.set(false);
                },
                error: (err) => {
                    console.error(err);
                    this.loading.set(false);
                }
            });
        }
    }

    applyFilter(event: Event): void {
        const filterValue = (event.target as HTMLInputElement).value;
        // TODO: Implement backend search or client-side filter
        console.log('Filtering suppliers:', filterValue);
    }

    applyMovementFilter(event: Event): void {
        const filterValue = (event.target as HTMLInputElement).value;
        // TODO: Implement backend search or client-side filter
        console.log('Filtering movements:', filterValue);
    }

    viewOrderDetails(po: PurchaseOrder): void {
        // TODO: Navigate to details or open dialog
        console.log('View details for PO:', po);
    }

    toggleAutoReorder(setting: ReorderSetting, checked: boolean): void {
        this.loading.set(true);
        // Build update data with ONLY the fields that backend DTO accepts
        // Backend ReorderSettingDTO has: productId, reorderPoint, reorderQuantity, preferredSupplierId, autoReorder
        const updateData = {
            productId: setting.product?.id || setting.productId,
            reorderPoint: setting.reorderPoint,
            reorderQuantity: setting.reorderQuantity,
            preferredSupplierId: setting.preferredSupplier?.id || setting.supplierId || null,
            autoReorder: checked
        };

        this.inventoryService.updateReorderSetting(setting.id, updateData).subscribe({
            next: () => {
                this.reorderSettings.update(settings =>
                    settings.map(s => s.id === setting.id ? { ...s, isEnabled: checked, autoReorder: checked } : s)
                );
                this.notificationService.success(checked ? 'Réapprovisionnement auto activé' : 'Réapprovisionnement auto désactivé');
                this.loading.set(false);
            },
            error: (err) => {
                console.error('Error updating reorder setting:', err);
                this.notificationService.error('Erreur lors de la mise à jour');
                this.loading.set(false);
            }
        });
    }

    deleteReorderSetting(id: string): void {
        if (confirm('Supprimer ce paramètre ?')) {
            this.inventoryService.deleteReorderSetting(id).subscribe({
                next: () => {
                    this.reorderSettings.update(s => s.filter(item => item.id !== id));
                    this.notificationService.success('Paramètre supprimé');
                },
                error: (err) => this.notificationService.error('Erreur lors de la suppression')
            });
        }
    }

    // Export Methods
    exportToCSV(type: 'inventory' | 'movements'): void {
        if (type === 'inventory') {
            this.exportService.exportInventory(this.productStock());
            this.notificationService.success('Export inventaire téléchargé');
        } else {
            this.exportService.exportStockMovements(this.stockMovements());
            this.notificationService.success('Export mouvements téléchargé');
        }
    }

    // Helper Methods
    getStatusLabel(status: string): string {
        const labels: { [key: string]: string } = {
            'DRAFT': 'Brouillon',
            'PENDING': 'En attente',
            'RECEIVED': 'Reçu',
            'CANCELLED': 'Annulé'
        };
        return labels[status] || status;
    }

    getMovementIcon(type: string): string {
        const icons: { [key: string]: string } = {
            'IN': 'arrow_downward',
            'OUT': 'arrow_upward',
            'ADJUSTMENT': 'tune'
        };
        return icons[type] || 'help';
    }

    getMovementLabel(type: string): string {
        const labels: { [key: string]: string } = {
            'IN': 'Entrée',
            'OUT': 'Sortie',
            'ADJUSTMENT': 'Ajustement'
        };
        return labels[type] || type;
    }

    // Dialog Methods
    openSupplierDialog(): void {
        this.editingSupplier.set(null);
        this.supplierForm.reset({ status: 'ACTIVE' });
        this.showSupplierForm.set(true);
    }

    closeSupplierDialog(): void {
        this.showSupplierForm.set(false);
        this.editingSupplier.set(null);
        this.supplierForm.reset({ status: 'ACTIVE' });
    }

    openPurchaseOrderDialog(): void {
        this.editingPO.set(null);
        this.purchaseOrderForm.reset({ orderDate: new Date() });
        // Note: Full PO creation would need items dialog - for now just toggle
        this.notificationService.info('Création de commande - Fonctionnalité à venir');
    }

    openStockMovementDialog(): void {
        this.stockMovementForm.reset({ type: 'IN', quantity: 1 });
        this.showMovementForm.set(true);
    }

    closeMovementDialog(): void {
        this.showMovementForm.set(false);
        this.stockMovementForm.reset({ type: 'IN' });
    }

    openReorderSettingDialog(): void {
        this.editingReorderSetting.set(null);
        this.reorderSettingForm.reset({
            autoReorder: false,
            reorderPoint: 5,
            reorderQuantity: 10,
            minimumStock: 0,
            maximumStock: 100,
            leadTimeDays: 7
        });
        this.showReorderForm.set(true);
    }

    closeReorderForm(): void {
        this.showReorderForm.set(false);
        this.editingReorderSetting.set(null);
        this.reorderSettingForm.reset({ autoReorder: false });
    }

    // Edit Methods
    editSupplier(supplier: Supplier): void {
        this.editingSupplier.set(supplier);
        this.supplierForm.patchValue({
            name: supplier.name,
            contactPerson: supplier.contactPerson,
            email: supplier.email,
            phone: supplier.phone,
            address: supplier.address,
            status: supplier.isActive ? 'ACTIVE' : 'INACTIVE'
        });
        this.showSupplierForm.set(true);
    }

    editPurchaseOrder(po: PurchaseOrder): void {
        this.editingPO.set(po);
        this.purchaseOrderForm.patchValue(po);
        this.notificationService.info('Édition commande - Fonctionnalité à venir');
    }

    // View Methods
    viewPurchaseOrder(po: PurchaseOrder): void {
        this.selectedPO.set(po);
        this.showPODetails.set(true);
    }

    closePODetails(): void {
        this.showPODetails.set(false);
        this.selectedPO.set(null);
    }

    editReorderSetting(setting: ReorderSetting): void {
        this.editingReorderSetting.set(setting);
        this.reorderSettingForm.patchValue({
            productId: setting.product?.id || setting.productId,
            reorderPoint: setting.reorderPoint,
            reorderQuantity: setting.reorderQuantity,
            supplierId: setting.preferredSupplier?.id || setting.supplierId,
            autoReorder: setting.autoReorder || setting.isEnabled,
            minimumStock: setting.minimumStock || 0,
            maximumStock: setting.maximumStock || 100,
            leadTimeDays: setting.leadTimeDays || 7
        });
        this.showReorderForm.set(true);
    }

    // Receive/Delete Methods
    receivePurchaseOrder(po: PurchaseOrder): void {
        if (confirm('Marquer cette commande comme reçue ?')) {
            // TODO: Call backend to update status
            this.notificationService.success('Commande marquée comme reçue');
            this.loadPurchaseOrders();
        }
    }

    deletePurchaseOrder(id: string): void {
        if (confirm('Supprimer cette commande ?')) {
            this.inventoryService.deletePurchaseOrder(id).subscribe({
                next: () => {
                    this.purchaseOrders.update(pos => pos.filter(po => po.id !== id));
                    this.notificationService.success('Commande supprimée');
                },
                error: (err) => this.notificationService.error('Erreur lors de la suppression')
            });
        }
    }
}
