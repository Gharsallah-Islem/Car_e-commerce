import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Product, ProductResponse, ProductFilter, Category, Brand } from '../models';

/**
 * Product Service
 * Handles all product-related API calls
 */
@Injectable({
    providedIn: 'root'
})
export class ProductService {
    private readonly endpoint = 'products';
    private readonly categoryEndpoint = 'categories';
    private readonly brandEndpoint = 'brands';

    constructor(private apiService: ApiService) { }

    /**
     * Get all products with optional filters and pagination
     */
    getProducts(filter?: ProductFilter): Observable<ProductResponse> {
        const params: any = {};

        if (filter) {
            if (filter.page !== undefined) params.page = filter.page.toString();
            if (filter.size !== undefined) params.size = filter.size.toString();
            if (filter.search) params.search = filter.search;
            if (filter.categoryId) params.categoryId = filter.categoryId.toString();
            if (filter.brandId) params.brandId = filter.brandId.toString();
            if (filter.minPrice !== undefined) params.minPrice = filter.minPrice.toString();
            if (filter.maxPrice !== undefined) params.maxPrice = filter.maxPrice.toString();
            if (filter.inStock !== undefined) params.inStock = filter.inStock.toString();
            if (filter.sort) params.sort = filter.sort;
        }

        return this.apiService.get<ProductResponse>(this.endpoint, params);
    }

    /**
     * Get a single product by ID
     */
    getProductById(id: number | string): Observable<Product> {
        // If id is a number, it's being used as-is (may fail if backend expects UUID)
        // If id is a string (UUID), use it directly
        return this.apiService.get<Product>(`${this.endpoint}/${id}`);
    }

    /**
     * Get featured products
     */
    getFeaturedProducts(limit: number = 8): Observable<Product[]> {
        return this.apiService.get<Product[]>(`${this.endpoint}/featured`, { limit: limit.toString() });
    }

    /**
     * Search products by query
     */
    searchProducts(query: string, page: number = 0, size: number = 20): Observable<ProductResponse> {
        return this.apiService.get<ProductResponse>(`${this.endpoint}/search`, {
            q: query,
            page: page.toString(),
            size: size.toString()
        });
    }

    /**
     * Get products by category
     */
    getProductsByCategory(categoryId: number, page: number = 0, size: number = 20): Observable<ProductResponse> {
        return this.apiService.get<ProductResponse>(`${this.endpoint}/category/${categoryId}`, {
            page: page.toString(),
            size: size.toString()
        });
    }

    /**
     * Get products by brand
     */
    getProductsByBrand(brandId: number, page: number = 0, size: number = 20): Observable<ProductResponse> {
        return this.apiService.get<ProductResponse>(`${this.endpoint}/brand/${brandId}`, {
            page: page.toString(),
            size: size.toString()
        });
    }

    /**
     * Get all categories
     */
    getCategories(): Observable<Category[]> {
        return this.apiService.get<Category[]>(this.categoryEndpoint);
    }

    /**
     * Get all brands
     */
    getBrands(): Observable<Brand[]> {
        return this.apiService.get<Brand[]>(this.brandEndpoint);
    }

    /**
     * Get related products
     */
    getRelatedProducts(productId: number, limit: number = 4): Observable<Product[]> {
        return this.apiService.get<Product[]>(`${this.endpoint}/${productId}/related`, {
            limit: limit.toString()
        });
    }
}
