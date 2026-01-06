import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Address } from '../models';

@Injectable({
    providedIn: 'root'
})
export class AddressService {
    private readonly endpoint = 'addresses';

    constructor(private apiService: ApiService) { }

    getMyAddresses(): Observable<Address[]> {
        return this.apiService.get<Address[]>(this.endpoint);
    }

    addAddress(address: Partial<Address>): Observable<Address> {
        return this.apiService.post<Address>(this.endpoint, address);
    }

    updateAddress(id: number, address: Partial<Address>): Observable<Address> {
        return this.apiService.put<Address>(`${this.endpoint}/${id}`, address);
    }

    deleteAddress(id: number): Observable<void> {
        return this.apiService.delete<void>(`${this.endpoint}/${id}`);
    }

    setDefaultAddress(id: number): Observable<Address> {
        return this.apiService.patch<Address>(`${this.endpoint}/${id}/default`, {});
    }
}
