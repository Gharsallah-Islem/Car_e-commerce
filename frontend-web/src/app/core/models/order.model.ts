import { Product } from './product.model';
import { Address } from './user.model';

export enum OrderStatus {
    PENDING = 'PENDING',
    PAID = 'PAID',
    PROCESSING = 'PROCESSING',
    SHIPPED = 'SHIPPED',
    DELIVERED = 'DELIVERED',
    CANCELLED = 'CANCELLED',
    REFUNDED = 'REFUNDED'
}

export interface Order {
    id: string;
    orderNumber?: string;
    user: {
        id: string;
        firstName: string;
        lastName: string;
        email: string;
        phone?: string;
        address?: string;
        fullName?: string;
        username?: string;
    };
    orderItems: Array<{
        id?: string;
        product: {
            id: string;
            name: string;
            imageUrl?: string;
            sku?: string;
        };
        quantity: number;
        price: number;
    }>;
    items?: OrderItem[]; // Alias for orderItems
    totalPrice: number;
    totalAmount?: number; // Alias for totalPrice
    status: OrderStatus;
    shippingAddress?: Address;
    billingAddress?: Address;
    paymentId?: string;
    trackingNumber?: string;
    notes?: string;
    createdAt: Date;
    updatedAt?: Date;
    deliveredAt?: Date;
}

export interface OrderItem {
    id: number;
    product: Product;
    quantity: number;
    price: number; // Price at the time of purchase
    subtotal: number;
}

export interface CreateOrderRequest {
    cartId?: number;
    shippingAddressId: number;
    billingAddressId?: number;
    notes?: string;
}

export interface OrderSummary {
    id: number;
    orderNumber: string;
    status: OrderStatus;
    totalAmount: number;
    itemCount: number;
    createdAt: Date;
}

export interface OrderTracking {
    orderId: number;
    trackingNumber: string;
    carrier: string;
    status: string;
    estimatedDelivery?: Date;
    history: TrackingEvent[];
}

export interface TrackingEvent {
    timestamp: Date;
    status: string;
    location: string;
    description: string;
}
