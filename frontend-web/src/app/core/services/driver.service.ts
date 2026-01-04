import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Driver {
    id: string;
    user: {
        id: string;
        fullName: string;
        email: string;
        phone: string;
        profilePicture?: string;
    };
    vehicleType: string;
    vehiclePlate: string;
    vehicleModel: string;
    licenseNumber: string;
    isAvailable: boolean;
    isVerified: boolean;
    isActive: boolean;
    rating: number;
    completedDeliveries: number;
    cancelledDeliveries: number;
    currentLatitude?: number;
    currentLongitude?: number;
    currentSpeed?: number;
    currentHeading?: number;
    lastLocationUpdate?: Date;
    currentDelivery?: any;
    createdAt: Date;
    updatedAt: Date;
}

export interface DriverLocation {
    id: string;
    latitude: number;
    longitude: number;
    speed?: number;
    heading?: number;
    accuracy?: number;
    altitude?: number;
    timestamp: Date;
}

export interface LocationUpdateRequest {
    latitude: number;
    longitude: number;
    speed?: number;
    heading?: number;
    accuracy?: number;
    altitude?: number;
    deliveryId?: string;
}

export interface DriverRegistration {
    vehicleType: string;
    vehiclePlate?: string;
    vehicleModel?: string;
    licenseNumber?: string;
}

export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface DriverStats {
    totalDrivers: number;
    availableDrivers: number;
    pendingVerification: number;
    activeDeliveries: number;
}

@Injectable({
    providedIn: 'root'
})
export class DriverService {
    private apiUrl = `${environment.apiUrl}/drivers`;

    constructor(private http: HttpClient) { }

    // ==================== DRIVER REGISTRATION ====================

    registerAsDriver(data: DriverRegistration): Observable<Driver> {
        return this.http.post<Driver>(this.apiUrl, data);
    }

    getCurrentDriver(): Observable<Driver> {
        return this.http.get<Driver>(`${this.apiUrl}/me`);
    }

    updateCurrentDriver(data: Partial<DriverRegistration>): Observable<Driver> {
        return this.http.put<Driver>(`${this.apiUrl}/me`, data);
    }

    // ==================== AVAILABILITY ====================

    toggleAvailability(): Observable<Driver> {
        return this.http.post<Driver>(`${this.apiUrl}/me/toggle-availability`, {});
    }

    goOnline(): Observable<Driver> {
        return this.http.post<Driver>(`${this.apiUrl}/me/online`, {});
    }

    goOffline(): Observable<Driver> {
        return this.http.post<Driver>(`${this.apiUrl}/me/offline`, {});
    }

    // ==================== LOCATION ====================

    updateLocation(location: LocationUpdateRequest): Observable<Driver> {
        return this.http.post<Driver>(`${this.apiUrl}/me/location`, location);
    }

    getLocationHistory(limit: number = 100): Observable<DriverLocation[]> {
        const params = new HttpParams().set('limit', limit.toString());
        return this.http.get<DriverLocation[]>(`${this.apiUrl}/me/locations`, { params });
    }

    // ==================== DELIVERY ACTIONS ====================

    completeDelivery(): Observable<Driver> {
        return this.http.post<Driver>(`${this.apiUrl}/me/complete-delivery`, {});
    }

    // ==================== ADMIN QUERIES ====================

    getAllDrivers(page: number = 0, size: number = 20): Observable<Page<Driver>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Driver>>(this.apiUrl, { params });
    }

    getDriverById(id: string): Observable<Driver> {
        return this.http.get<Driver>(`${this.apiUrl}/${id}`);
    }

    getAvailableDrivers(): Observable<Driver[]> {
        return this.http.get<Driver[]>(`${this.apiUrl}/available`);
    }

    findNearestDriver(latitude: number, longitude: number): Observable<Driver> {
        const params = new HttpParams()
            .set('latitude', latitude.toString())
            .set('longitude', longitude.toString());
        return this.http.get<Driver>(`${this.apiUrl}/nearest`, { params });
    }

    findDriversNearby(latitude: number, longitude: number, radiusKm: number = 10): Observable<Driver[]> {
        const params = new HttpParams()
            .set('latitude', latitude.toString())
            .set('longitude', longitude.toString())
            .set('radiusKm', radiusKm.toString());
        return this.http.get<Driver[]>(`${this.apiUrl}/nearby`, { params });
    }

    // ==================== DRIVER ASSIGNMENT ====================

    assignDelivery(driverId: string, deliveryId: string): Observable<Driver> {
        return this.http.post<Driver>(`${this.apiUrl}/${driverId}/assign`, { deliveryId });
    }

    unassignDelivery(driverId: string): Observable<Driver> {
        return this.http.post<Driver>(`${this.apiUrl}/${driverId}/unassign`, {});
    }

    // ==================== ADMIN ACTIONS ====================

    verifyDriver(id: string): Observable<Driver> {
        return this.http.post<Driver>(`${this.apiUrl}/${id}/verify`, {});
    }

    suspendDriver(id: string): Observable<Driver> {
        return this.http.post<Driver>(`${this.apiUrl}/${id}/suspend`, {});
    }

    reactivateDriver(id: string): Observable<Driver> {
        return this.http.post<Driver>(`${this.apiUrl}/${id}/reactivate`, {});
    }

    getUnverifiedDrivers(page: number = 0, size: number = 20): Observable<Page<Driver>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Driver>>(`${this.apiUrl}/unverified`, { params });
    }

    searchDrivers(query: string, page: number = 0, size: number = 20): Observable<Page<Driver>> {
        const params = new HttpParams()
            .set('q', query)
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Driver>>(`${this.apiUrl}/search`, { params });
    }

    // ==================== STATISTICS ====================

    getStatistics(): Observable<DriverStats> {
        return this.http.get<DriverStats>(`${this.apiUrl}/statistics`);
    }

    getDeliveryLocationHistory(deliveryId: string): Observable<DriverLocation[]> {
        return this.http.get<DriverLocation[]>(`${this.apiUrl}/delivery/${deliveryId}/locations`);
    }
}
