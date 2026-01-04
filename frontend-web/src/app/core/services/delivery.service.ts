import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface Delivery {
    id: string;
    trackingNumber: string;
    order: any;
    deliveryAddress: string;
    address: string;
    recipientName: string;
    recipientPhone: string;
    status: 'PROCESSING' | 'PICKED_UP' | 'IN_TRANSIT' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'FAILED' | 'CANCELLED';
    courierName?: string;
    driverName?: string;
    driverPhone?: string;
    estimatedDelivery?: Date;
    estimatedDeliveryDate?: Date;
    actualDeliveryDate?: Date;
    actualDelivery?: Date;
    deliveryNotes?: string;
    notes?: string;
    currentLatitude?: number;
    currentLongitude?: number;
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

export interface DeliveryStats {
    totalDeliveries: number;
    processing: number;
    inTransit: number;
    outForDelivery: number;
    delivered: number;
    failed: number;
    averageDeliveryTime: number;
    onTimeRate?: number;
}

// Backend response type (uses UPPER_CASE keys)
interface BackendDeliveryStats {
    TOTAL?: number;
    PROCESSING?: number;
    IN_TRANSIT?: number;
    OUT_FOR_DELIVERY?: number;
    DELIVERED?: number;
    FAILED?: number;
}

@Injectable({
    providedIn: 'root'
})
export class DeliveryService {
    private apiUrl = `${environment.apiUrl}/delivery`;

    constructor(private http: HttpClient) { }

    // ==================== DELIVERY CRUD ====================

    createDelivery(orderId: string, delivery: Partial<Delivery>): Observable<Delivery> {
        const params = new HttpParams().set('orderId', orderId);
        return this.http.post<Delivery>(this.apiUrl, delivery, { params });
    }

    getDeliveryById(id: string): Observable<Delivery> {
        return this.http.get<Delivery>(`${this.apiUrl}/${id}`);
    }

    getDeliveryByOrderId(orderId: string): Observable<Delivery> {
        return this.http.get<Delivery>(`${this.apiUrl}/order/${orderId}`);
    }

    trackDelivery(trackingNumber: string): Observable<Delivery> {
        return this.http.get<Delivery>(`${this.apiUrl}/track/${trackingNumber}`);
    }

    getDeliveryByTrackingNumber(trackingNumber: string): Observable<Delivery> {
        return this.http.get<Delivery>(`${this.apiUrl}/tracking/${trackingNumber}`);
    }

    // ==================== ADMIN QUERIES ====================

    getAllDeliveries(page: number = 0, size: number = 20, sort: string = 'createdAt'): Observable<Page<Delivery>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sort', sort);
        return this.http.get<Page<Delivery>>(this.apiUrl, { params });
    }

    getDeliveriesByStatus(status: string, page: number = 0, size: number = 20): Observable<Page<Delivery>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Delivery>>(`${this.apiUrl}/status/${status}`, { params });
    }

    getPendingDeliveries(page: number = 0, size: number = 20): Observable<Page<Delivery>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Delivery>>(`${this.apiUrl}/pending`, { params });
    }

    getActiveDeliveries(page: number = 0, size: number = 20): Observable<Page<Delivery>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Delivery>>(`${this.apiUrl}/active`, { params });
    }

    getDeliveriesByCourier(courierName: string, page: number = 0, size: number = 20): Observable<Page<Delivery>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Delivery>>(`${this.apiUrl}/courier/${courierName}`, { params });
    }

    // ==================== STATUS UPDATES ====================

    updateStatus(id: string, status: string): Observable<Delivery> {
        return this.http.patch<Delivery>(`${this.apiUrl}/${id}/status`, { status });
    }

    markAsPickedUp(id: string, courierName: string): Observable<Delivery> {
        return this.http.patch<Delivery>(`${this.apiUrl}/${id}/picked-up`, { courierName });
    }

    markAsInTransit(id: string): Observable<Delivery> {
        return this.http.patch<Delivery>(`${this.apiUrl}/${id}/in-transit`, {});
    }

    markAsDelivered(id: string): Observable<Delivery> {
        return this.http.patch<Delivery>(`${this.apiUrl}/${id}/delivered`, {});
    }

    // ==================== STATISTICS ====================

    getStatistics(): Observable<DeliveryStats> {
        return this.http.get<BackendDeliveryStats>(`${this.apiUrl}/statistics`).pipe(
            map(backendStats => ({
                totalDeliveries: backendStats.TOTAL || 0,
                processing: backendStats.PROCESSING || 0,
                inTransit: backendStats.IN_TRANSIT || 0,
                outForDelivery: backendStats.OUT_FOR_DELIVERY || 0,
                delivered: backendStats.DELIVERED || 0,
                failed: backendStats.FAILED || 0,
                averageDeliveryTime: 0, // Will be fetched separately
                onTimeRate: 0
            }))
        );
    }

    getAverageDeliveryTime(): Observable<number> {
        return this.http.get<number>(`${this.apiUrl}/average-time`);
    }

    /**
     * Sync confirmed orders - creates deliveries for confirmed orders that don't have one
     */
    syncConfirmedOrders(): Observable<{ success: boolean; message: string; deliveriesCreated: number }> {
        return this.http.post<{ success: boolean; message: string; deliveriesCreated: number }>(
            `${this.apiUrl}/sync-confirmed-orders`,
            {}
        );
    }
}

