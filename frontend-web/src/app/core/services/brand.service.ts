import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Brand {
    id: number;
    name: string;
    description: string;
    country?: string;
    logoUrl?: string;
}

@Injectable({
    providedIn: 'root'
})
export class BrandService {
    private apiUrl = `${environment.apiUrl}/brands`;

    constructor(private http: HttpClient) { }

    getAllBrands(): Observable<Brand[]> {
        return this.http.get<Brand[]>(this.apiUrl);
    }

    getBrandById(id: number): Observable<Brand> {
        return this.http.get<Brand>(`${this.apiUrl}/${id}`);
    }

    createBrand(brand: Partial<Brand>): Observable<Brand> {
        return this.http.post<Brand>(this.apiUrl, brand);
    }

    updateBrand(id: number, brand: Partial<Brand>): Observable<Brand> {
        return this.http.put<Brand>(`${this.apiUrl}/${id}`, brand);
    }

    deleteBrand(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
