import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Order } from '../models';

@Injectable({
    providedIn: 'root'
})
export class OrderService {
    private readonly endpoint = 'orders';

    constructor(private apiService: ApiService) { }

    getAllOrders(page: number = 0, size: number = 20, sortBy: string = 'createdAt', sortDir: string = 'desc'): Observable<any> {
        return this.apiService.get<any>(this.endpoint, { page, size, sortBy, sortDir });
    }

    getOrderById(id: string): Observable<Order> {
        return this.apiService.get<Order>(`${this.endpoint}/${id}`);
    }

    updateOrderStatus(id: string, status: string): Observable<Order> {
        return this.apiService.patch<Order>(`${this.endpoint}/${id}/status`, { status });
    }
}
