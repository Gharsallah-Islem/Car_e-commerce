export enum PaymentStatus {
    PENDING = 'PENDING',
    PROCESSING = 'PROCESSING',
    SUCCEEDED = 'SUCCEEDED',
    FAILED = 'FAILED',
    CANCELLED = 'CANCELLED',
    REFUNDED = 'REFUNDED'
}

export enum PaymentMethod {
    STRIPE = 'STRIPE',
    PAYPAL = 'PAYPAL',
    CASH_ON_DELIVERY = 'CASH_ON_DELIVERY'
}

export interface Payment {
    id: number;
    orderId: number;
    amount: number;
    currency: string;
    method: PaymentMethod;
    status: PaymentStatus;
    stripePaymentIntentId?: string;
    stripeClientSecret?: string;
    transactionId?: string;
    failureReason?: string;
    createdAt: Date;
    updatedAt: Date;
}

export interface CreatePaymentIntentRequest {
    orderId: number;
    amount: number;
    currency?: string;
}

export interface PaymentIntentResponse {
    paymentIntentId: string;
    clientSecret: string;
    amount: number;
    currency: string;
}

export interface ConfirmPaymentRequest {
    paymentIntentId: string;
    paymentMethodId?: string;
}

export interface PaymentConfirmationResponse {
    success: boolean;
    paymentId: number;
    orderId: number;
    status: PaymentStatus;
    message?: string;
}

export interface RefundRequest {
    paymentId: number;
    amount?: number; // Partial refund if specified
    reason?: string;
}

export interface RefundResponse {
    success: boolean;
    refundId: string;
    amount: number;
    status: string;
}
