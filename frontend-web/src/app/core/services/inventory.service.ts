import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Supplier {
    id: string;
    name: string;
    contactPerson: string;
    email: string;
    phone: string;
    address: string;
    isActive: boolean;
    rating?: number;
    createdAt: Date;
    updatedAt: Date;
}

export interface PurchaseOrder {
    id: string;
    poNumber: string;
    supplier: Supplier;
    orderDate: Date;
    expectedDeliveryDate: Date;
    status: 'DRAFT' | 'PENDING' | 'APPROVED' | 'RECEIVED' | 'CANCELLED';
    totalAmount: number;
    items: PurchaseOrderItem[];
    notes?: string;
    createdAt: Date;
    updatedAt: Date;
}

export interface PurchaseOrderItem {
    id: string;
    product: any;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
}

export interface StockMovement {
    id: string;
    product: any;
    movementType: 'PURCHASE' | 'SALE' | 'ADJUSTMENT' | 'RETURN' | 'RETURN_FROM_CUSTOMER' | 'RETURN_TO_SUPPLIER' | 'DAMAGED' | 'TRANSFER' | 'INITIAL';
    quantity: number;
    previousStock?: number;
    newStock?: number;
    referenceId?: string;
    referenceType?: string;
    notes?: string;
    performedBy?: string;
    movementDate: Date;
}

export interface ReorderSetting {
    id: string;
    product: any;
    productId?: string;
    supplierId?: string;
    reorderPoint: number;
    reorderQuantity: number;
    supplier?: Supplier;
    autoReorder?: boolean;
    isEnabled: boolean;
    createdAt: Date;
    updatedAt: Date;
}

export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface InventoryStats {
    totalProducts: number;
    totalValue: number;
    lowStockItems: number;
    outOfStockItems: number;
    pendingPOs: number;
    activeSuppliers: number;
}

@Injectable({
    providedIn: 'root'
})
export class InventoryService {
    private apiUrl = `${environment.apiUrl}/inventory`;

    constructor(private http: HttpClient) { }

    // ==================== SUPPLIER METHODS ====================

    getSuppliers(page: number = 0, size: number = 20, sort: string = 'name'): Observable<Page<Supplier>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sort', sort);
        return this.http.get<Page<Supplier>>(`${this.apiUrl}/suppliers`, { params });
    }

    getSupplierById(id: string): Observable<Supplier> {
        return this.http.get<Supplier>(`${this.apiUrl}/suppliers/${id}`);
    }

    searchSuppliers(keyword: string, page: number = 0, size: number = 20): Observable<Page<Supplier>> {
        const params = new HttpParams()
            .set('keyword', keyword)
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Supplier>>(`${this.apiUrl}/suppliers/search`, { params });
    }

    getActiveSuppliers(): Observable<Supplier[]> {
        return this.http.get<Supplier[]>(`${this.apiUrl}/suppliers/active`);
    }

    createSupplier(supplier: Partial<Supplier>): Observable<Supplier> {
        return this.http.post<Supplier>(`${this.apiUrl}/suppliers`, supplier);
    }

    updateSupplier(id: string, supplier: Partial<Supplier>): Observable<Supplier> {
        return this.http.put<Supplier>(`${this.apiUrl}/suppliers/${id}`, supplier);
    }

    deleteSupplier(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/suppliers/${id}`);
    }

    getSupplierStatistics(): Observable<any> {
        return this.http.get(`${this.apiUrl}/suppliers/statistics`);
    }

    // ==================== PURCHASE ORDER METHODS ====================

    getPurchaseOrders(page: number = 0, size: number = 20, sort: string = 'orderDate'): Observable<Page<PurchaseOrder>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sort', sort);
        return this.http.get<Page<PurchaseOrder>>(`${this.apiUrl}/purchase-orders`, { params });
    }

    getPurchaseOrderById(id: string): Observable<PurchaseOrder> {
        return this.http.get<PurchaseOrder>(`${this.apiUrl}/purchase-orders/${id}`);
    }

    getPurchaseOrdersByStatus(status: string, page: number = 0, size: number = 20): Observable<Page<PurchaseOrder>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<PurchaseOrder>>(`${this.apiUrl}/purchase-orders/status/${status}`, { params });
    }

    getPurchaseOrdersBySupplier(supplierId: string, page: number = 0, size: number = 20): Observable<Page<PurchaseOrder>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<PurchaseOrder>>(`${this.apiUrl}/purchase-orders/supplier/${supplierId}`, { params });
    }

    createPurchaseOrder(po: Partial<PurchaseOrder>): Observable<PurchaseOrder> {
        return this.http.post<PurchaseOrder>(`${this.apiUrl}/purchase-orders`, po);
    }

    updatePurchaseOrder(id: string, po: Partial<PurchaseOrder>): Observable<PurchaseOrder> {
        return this.http.put<PurchaseOrder>(`${this.apiUrl}/purchase-orders/${id}`, po);
    }

    updatePurchaseOrderStatus(id: string, status: string): Observable<PurchaseOrder> {
        return this.http.patch<PurchaseOrder>(`${this.apiUrl}/purchase-orders/${id}/status`, { status });
    }

    deletePurchaseOrder(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/purchase-orders/${id}`);
    }

    getPurchaseOrderStatistics(): Observable<any> {
        return this.http.get(`${this.apiUrl}/purchase-orders/statistics`);
    }

    // ==================== STOCK MOVEMENT METHODS ====================

    getStockMovements(page: number = 0, size: number = 20, sort: string = 'movementDate'): Observable<Page<StockMovement>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sort', sort);
        return this.http.get<Page<StockMovement>>(`${this.apiUrl}/stock-movements`, { params });
    }

    getStockMovementById(id: string): Observable<StockMovement> {
        return this.http.get<StockMovement>(`${this.apiUrl}/stock-movements/${id}`);
    }

    getStockMovementsByProduct(productId: string, page: number = 0, size: number = 20): Observable<Page<StockMovement>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<StockMovement>>(`${this.apiUrl}/stock-movements/product/${productId}`, { params });
    }

    getStockMovementsByType(type: string, page: number = 0, size: number = 20): Observable<Page<StockMovement>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<StockMovement>>(`${this.apiUrl}/stock-movements/type/${type}`, { params });
    }

    getRecentStockMovements(limit: number = 10): Observable<StockMovement[]> {
        const params = new HttpParams().set('limit', limit.toString());
        return this.http.get<StockMovement[]>(`${this.apiUrl}/stock-movements/recent`, { params });
    }

    recordStockMovement(movement: Partial<StockMovement>): Observable<StockMovement> {
        return this.http.post<StockMovement>(`${this.apiUrl}/stock-movements`, movement);
    }

    // ==================== REORDER SETTING METHODS ====================

    getReorderSettings(page: number = 0, size: number = 20): Observable<Page<ReorderSetting>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<ReorderSetting>>(`${this.apiUrl}/reorder-settings`, { params });
    }

    getReorderSettingById(id: string): Observable<ReorderSetting> {
        return this.http.get<ReorderSetting>(`${this.apiUrl}/reorder-settings/${id}`);
    }

    getReorderSettingByProduct(productId: string): Observable<ReorderSetting> {
        return this.http.get<ReorderSetting>(`${this.apiUrl}/reorder-settings/product/${productId}`);
    }

    getProductsBelowReorderPoint(): Observable<ReorderSetting[]> {
        return this.http.get<ReorderSetting[]>(`${this.apiUrl}/reorder-settings/below-reorder-point`);
    }

    createReorderSetting(setting: Partial<ReorderSetting>): Observable<ReorderSetting> {
        return this.http.post<ReorderSetting>(`${this.apiUrl}/reorder-settings`, setting);
    }

    updateReorderSetting(id: string, setting: Partial<ReorderSetting>): Observable<ReorderSetting> {
        return this.http.put<ReorderSetting>(`${this.apiUrl}/reorder-settings/${id}`, setting);
    }

    deleteReorderSetting(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/reorder-settings/${id}`);
    }

    triggerAutoReorder(): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/reorder-settings/check-auto-reorders`, {});
    }

    checkAutoReorders(): Observable<void> {
        return this.triggerAutoReorder();
    }

    // ==================== STATISTICS ====================

    getInventoryStatistics(): Observable<InventoryStats> {
        return this.http.get<InventoryStats>(`${this.apiUrl}/statistics`);
    }
}
