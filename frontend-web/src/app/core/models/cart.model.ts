import { Product } from './product.model';

export interface Cart {
    id?: string;
    items: CartItem[];
    totalAmount: number;
    totalItems: number;
    createdAt?: Date;
    updatedAt?: Date;
}

export interface CartItem {
    id?: string;
    product: Product;
    quantity: number;
    price: number; // Price at the time of adding to cart
    subtotal: number;
}

export interface AddToCartRequest {
    productId: string;
    quantity: number;
}

export interface UpdateCartItemRequest {
    cartItemId: string;
    quantity: number;
}

export interface CartSummary {
    subtotal: number;
    tax: number;
    shipping: number;
    discount: number;
    total: number;
    itemCount: number;
}
