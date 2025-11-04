// Barrel export for all models
export * from './user.model';
export * from './product.model';
export * from './cart.model';
export * from './order.model';
export * from './payment.model';
export * from './chat.model';
export * from './claim.model';
export * from './ai-mechanic.model';

// Common utility types
export interface ApiResponse<T> {
    data: T;
    message?: string;
    success: boolean;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
}

export interface ApiError {
    message: string;
    code?: string;
    status: number;
    timestamp: Date;
    details?: any;
}

export interface FileUpload {
    file: File;
    progress: number;
    status: 'pending' | 'uploading' | 'completed' | 'error';
    url?: string;
    error?: string;
}
