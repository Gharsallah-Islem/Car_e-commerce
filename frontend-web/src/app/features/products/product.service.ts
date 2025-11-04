import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Product } from './product.model';
import { catchError, map } from 'rxjs/operators';
import { of, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private backendApi = 'http://localhost:8080/api/products';
  private localMock = 'assets/products.json';

  constructor(private http: HttpClient) {}

  fetchProducts(): Observable<Product[]> {
    // try backend first, fallback to local mock
    return this.http.get<Product[]>(this.backendApi).pipe(
      catchError(() => this.http.get<Product[]>(this.localMock).pipe(catchError(() => of([]))))
    );
  }

  fetchProductById(id: number): Observable<Product | undefined> {
    return this.fetchProducts().pipe(map(list => list.find(p => p.id === id)));
  }
}