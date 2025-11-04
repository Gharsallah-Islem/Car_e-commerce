import { Product } from './product.model';

export interface Cart {
    id?: number;
    items: CartItem[];
    totalAmount: number;
    totalItems: number;
    createdAt?: Date;
    updatedAt?: Date;
}

export interface CartItem {
    id?: number;
    product: Product;
    quantity: number;
    price: number; // Price at the time of adding to cart
    subtotal: number;
}

export interface AddToCartRequest {
    productId: number;
    quantity: number;
}

export interface UpdateCartItemRequest {
    cartItemId: number;
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
