import { products } from './products';
import type { Product } from '../types/Product';

export type CartItem = {
    product: Product;
    quantity: number;
};

let cartItems: CartItem[] = [];

export async function getCart(): Promise<CartItem[]> {
    return [...cartItems];
}

export async function addToCart(productId: number): Promise<CartItem[]> {
    const product = products.find(p => p.id === productId);

    if (!product) {
        throw new Error('Product not found');
    }

    const existingItem = cartItems.find(
        item => item.product.id === productId
    );

    const currentQuantity = existingItem?.quantity ?? 0;

    if (currentQuantity >= product.quantity) {
        throw new Error('Insufficient stock');
    }

    if (existingItem) {
        existingItem.quantity++;
    } else {
        cartItems.push({ product, quantity: 1 });
    }

    return getCart();
}

export async function updateQuantity(
    productId: number,
    quantity: number
): Promise<CartItem[]> {
    const item = cartItems.find(
        item => item.product.id === productId
    );

    if (!item) {
        throw new Error('Item not found in cart');
    }

    if (!Number.isInteger(quantity) || quantity < 0) {
        throw new Error('Invalid quantity');
    }

    if (quantity > item.product.quantity) {
        throw new Error('Insufficient stock');
    }

    if (quantity === 0) {
        return removeFromCart(productId);
    }

    item.quantity = quantity;

    return getCart();
}

export async function removeFromCart(
    productId: number
): Promise<CartItem[]> {
    cartItems = cartItems.filter(
        item => item.product.id !== productId
    );

    return getCart();
}

export async function clearCart(): Promise<CartItem[]> {
    cartItems = [];
    return getCart();
}