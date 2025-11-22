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

import { NotificationService } from '../../../core/services/notification.service';
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
        MatBadgeModule
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

    // Stats
    stats = signal<InventoryStats>({
        totalProducts: 0,
        totalValue: 0,
        lowStockItems: 0,
        outOfStockItems: 0,
        pendingPOs: 0,
        activeSuppliers: 0
    });

    // Suppliers
    suppliers = signal<Supplier[]>([]);
    editingSupplier = signal<Supplier | null>(null);
    supplierColumns: string[] = ['name', 'contact', 'email', 'phone', 'products', 'orders', 'rating', 'status', 'actions'];

    // Purchase Orders
    purchaseOrders = signal<PurchaseOrder[]>([]);
    editingPO = signal<PurchaseOrder | null>(null);
    poColumns: string[] = ['orderNumber', 'supplier', 'orderDate', 'expectedDelivery', 'status', 'items', 'total', 'actions'];

    // Stock Movements
    stockMovements = signal<StockMovement[]>([]);
    movementColumns: string[] = ['date', 'product', 'type', 'quantity', 'reason', 'reference', 'performedBy', 'actions'];

    // Reorder Settings
    reorderSettings = signal<ReorderSetting[]>([]);
    reorderColumns: string[] = ['product', 'currentStock', 'reorderPoint', 'reorderQuantity', 'supplier', 'autoReorder', 'status', 'actions'];

    // Pagination
    pageSize = signal<number>(10);
    pageIndex = signal<number>(0);

    constructor(
        private fb: FormBuilder,
        private dialog: MatDialog,
        private notificationService: NotificationService,
        private inventoryService: InventoryService
    ) {
        this.initForms();
    }

    ngOnInit(): void {
        this.loadInventoryStats();
        this.loadSuppliers();
        this.loadPurchaseOrders();
        this.loadStockMovements();
        this.loadReorderSettings();
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
            quantity: [0, [Validators.required, Validators.min(1)]],
            reason: ['', [Validators.required]],
            reference: ['']
        });

        // Reorder Setting Form
        this.reorderSettingForm = this.fb.group({
            productId: ['', [Validators.required]],
            reorderPoint: [0, [Validators.required, Validators.min(0)]],
            reorderQuantity: [0, [Validators.required, Validators.min(1)]],
            supplierId: ['', [Validators.required]],
            autoReorder: [false]
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
                this.supplierForm.reset({ status: 'ACTIVE' });
                this.editingSupplier.set(null);
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

        // Map form type to backend enum with proper typing
        const movementTypeMap: { [key: string]: 'PURCHASE' | 'SALE' | 'ADJUSTMENT' | 'RETURN' } = {
            'IN': 'PURCHASE',
            'OUT': 'SALE',
            'ADJUSTMENT': 'ADJUSTMENT'
        };

        const movementType = movementTypeMap[formValue.type] || 'ADJUSTMENT';

        const movementData = {
            productId: formValue.productId,
            movementType: movementType,
            quantity: formValue.quantity,
            referenceType: formValue.reference || 'MANUAL',
            notes: formValue.reason
        } as Partial<StockMovement>;

        this.inventoryService.recordStockMovement(movementData).subscribe({
            next: (newMovement) => {
                this.stockMovements.update(movements => [newMovement, ...movements]);
                this.notificationService.success('Mouvement de stock enregistré');
                this.stockMovementForm.reset({ type: 'IN' });
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error recording stock movement:', error);
                this.notificationService.error('Erreur lors de l\'enregistrement du mouvement');
                this.loading.set(false);
            }
        });
    }

    // Reorder Setting Management
    saveReorderSetting(): void {
        if (this.reorderSettingForm.invalid) {
            this.notificationService.warning('Veuillez remplir tous les champs obligatoires');
            return;
        }

        this.loading.set(true);
        const formValue = this.reorderSettingForm.value;

        const settingData = {
            productId: formValue.productId,
            reorderPoint: formValue.reorderPoint,
            reorderQuantity: formValue.reorderQuantity,
            supplierId: formValue.supplierId,
            isEnabled: formValue.autoReorder
        };

        this.inventoryService.createReorderSetting(settingData).subscribe({
            next: (newSetting) => {
                this.reorderSettings.update(settings => [...settings, newSetting]);
                this.notificationService.success('Paramètre de réapprovisionnement enregistré');
                this.reorderSettingForm.reset({ autoReorder: false });
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error creating reorder setting:', error);
                this.notificationService.error('Erreur lors de la création du paramètre');
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

    exportToCSV(type: string): void {
        this.notificationService.info(`Export ${type} en cours...`);
        // Implement CSV export logic
    }
}
