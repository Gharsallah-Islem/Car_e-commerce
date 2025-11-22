import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface ProductImage {
    id?: string;
    productId?: string;
    imageUrl: string;
    displayOrder: number;
    isPrimary: boolean;
    createdAt?: Date;
}

@Injectable({
    providedIn: 'root'
})
export class ProductImageService {
    private readonly endpoint = '/products';

    constructor(private apiService: ApiService) { }

    getProductImages(productId: string): Observable<ProductImage[]> {
        return this.apiService.get<ProductImage[]>(`${this.endpoint}/${productId}/images`);
    }

    addProductImage(productId: string, imageUrl: string, isPrimary: boolean = false): Observable<ProductImage> {
        return this.apiService.post<ProductImage>(`${this.endpoint}/${productId}/images`, {
            imageUrl,
            isPrimary
        });
    }

    deleteProductImage(productId: string, imageId: string): Observable<void> {
        return this.apiService.delete<void>(`${this.endpoint}/${productId}/images/${imageId}`);
    }

    reorderImages(productId: string, imageOrders: Array<{ id: string; displayOrder: number }>): Observable<ProductImage[]> {
        return this.apiService.patch<ProductImage[]>(`${this.endpoint}/${productId}/images/reorder`, imageOrders);
    }

    setPrimaryImage(productId: string, imageId: string): Observable<ProductImage> {
        return this.apiService.patch<ProductImage>(`${this.endpoint}/${productId}/images/${imageId}/set-primary`, {});
    }
}
