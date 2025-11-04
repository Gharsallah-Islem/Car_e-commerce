export interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    stock: number;
    imageUrl?: string;
    images?: string[];
    category: Category;
    brand: Brand;
    compatibility?: VehicleCompatibility[];
    compatibilityString?: string; // Simple string from backend (e.g., "Toyota Corolla, Camry")
    specifications?: ProductSpecification[];
    sku?: string;
    weight?: number;
    dimensions?: Dimensions;
    discount?: number;
    rating?: number;
    reviewCount?: number;
    isActive?: boolean;
    createdAt?: Date;
    updatedAt?: Date;
}

export interface Category {
    id: number;
    name: string;
    description?: string;
    parentId?: number;
    imageUrl?: string;
}

export interface Brand {
    id: number;
    name: string;
    logoUrl?: string;
    description?: string;
}

export interface VehicleCompatibility {
    id: number;
    brand: string;
    model: string;
    year: number;
    engineType?: string;
}

export interface ProductSpecification {
    key: string;
    value: string;
}

export interface Dimensions {
    length: number;
    width: number;
    height: number;
    unit: 'cm' | 'in';
}

export interface ProductFilter {
    categoryId?: number;
    brandId?: number;
    minPrice?: number;
    maxPrice?: number;
    search?: string;
    inStock?: boolean;
    vehicleBrand?: string;
    vehicleModel?: string;
    vehicleYear?: number;
    sort?: 'price_asc' | 'price_desc' | 'name' | 'newest';
    page?: number;
    size?: number;
}

export interface ProductResponse {
    content: Product[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}
