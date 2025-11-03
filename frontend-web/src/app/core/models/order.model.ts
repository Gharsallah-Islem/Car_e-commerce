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
    id: number;
    orderNumber: string;
    userId: number;
    items: OrderItem[];
    status: OrderStatus;
    totalAmount: number;
    shippingAddress: Address;
    billingAddress?: Address;
    paymentId?: string;
    trackingNumber?: string;
    notes?: string;
    createdAt: Date;
    updatedAt: Date;
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
