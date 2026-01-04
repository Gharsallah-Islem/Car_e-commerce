export enum UserRole {
    CLIENT = 'CLIENT',
    SUPPORT = 'SUPPORT',
    ADMIN = 'ADMIN',
    SUPER_ADMIN = 'SUPER_ADMIN'
}

export interface User {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    phoneNumber?: string;
    address?: string;
    profilePicture?: string;
    role: UserRole;
    isActive?: boolean;
    enabled?: boolean; // Deprecated, use isActive
    provider?: string; // 'LOCAL' or 'GOOGLE'
    createdAt?: Date;
    updatedAt?: Date;
}

export interface UserProfile extends User {
    addresses?: Address[];
    orderCount?: number; // Use count instead of full Order array to avoid circular dependency
}

export interface Address {
    id?: number;
    street: string;
    city: string;
    postalCode: string;
    country: string;
    isDefault?: boolean;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phoneNumber?: string;
}

export interface AuthResponse {
    token: string;
    refreshToken?: string;
    user: User;
}

export interface TokenPayload {
    sub: string; // email
    role: UserRole;
    exp: number;
    iat: number;
}
