
import { products } from './products';
import type { Product } from '../types/Product';

export type CartItem = {
    product: Product;
    quantity: number;
};

const API = 'http://localhost:8080/api';
const GUEST_CART_KEY = 'viralVaultGuestCart';

type GuestCartEntry = {
    productId: number;
    quantity: number;
};

function getGuestEntries(): GuestCartEntry[] {
    if (typeof localStorage === 'undefined') return [];

    try {
        return JSON.parse(
            localStorage.getItem(GUEST_CART_KEY) || '[]'
        );
    } catch {
        return [];
    }
}

function saveGuestEntries(items: GuestCartEntry[]) {
    localStorage.setItem(GUEST_CART_KEY, JSON.stringify(items));
}

async function isLoggedIn(): Promise<boolean> {
    const response = await fetch(`${API}/users/session`, {
        credentials: 'include'
    });

    return response.ok;
}

async function getProducts(): Promise<Product[]> {
    const response = await fetch(`${API}/products`);

    if (!response.ok) {
        throw new Error('Unable to load products');
    }

    return response.json();
}

async function getGuestCart(): Promise<CartItem[]> {
    const allProducts = await getProducts();

    return getGuestEntries().flatMap(entry => {
        const product = allProducts.find(
            p => p.id === entry.productId
        );

        if (!product) return [];

        return [{
            product,
            quantity: entry.quantity
        }];
    });
}

async function requestCart(
    path: string = '',
    method: string = 'GET',
    body?: object
): Promise<CartItem[]> {
    const response = await fetch(`${API}/cart${path}`, {
        method,
        credentials: 'include',
        headers: body
            ? { 'Content-Type': 'application/json' }
            : undefined,
        body: body ? JSON.stringify(body) : undefined
    });

    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || 'Cart request failed');
    }

    return response.json();
}

export async function getCart(): Promise<CartItem[]> {
    if (await isLoggedIn()) {
        return requestCart();
    }

    return getGuestCart();
}

export async function addToCart(
    productId: number
): Promise<CartItem[]> {
    const items = await getCart();
    const existing = items.find(
        item => item.product.id === productId
    );

    const quantity = (existing?.quantity ?? 0) + 1;

    if (await isLoggedIn()) {
        return requestCart(`/${productId}`, 'PUT', {
            quantity
        });
    }

    return updateQuantity(productId, quantity);
}

export async function updateQuantity(
    productId: number,
    quantity: number
): Promise<CartItem[]> {
    if (!Number.isInteger(quantity) || quantity < 0) {
        throw new Error('Invalid quantity');
    }

    if (await isLoggedIn()) {
        return requestCart(`/${productId}`, 'PUT', {
            quantity
        });
    }

    const allProducts = await getProducts();
    const product = allProducts.find(p => p.id === productId);

    if (!product) {
        throw new Error('Product not found');
    }

    if (quantity > product.quantity) {
        throw new Error('Insufficient stock');
    }

    const entries = getGuestEntries().filter(
        item => item.productId !== productId
    );

    if (quantity > 0) {
        entries.push({ productId, quantity });
    }

    saveGuestEntries(entries);

    return getGuestCart();
}

export async function removeFromCart(
    productId: number
): Promise<CartItem[]> {
    if (await isLoggedIn()) {
        return requestCart(`/${productId}`, 'DELETE');
    }

    const entries = getGuestEntries().filter(
        item => item.productId !== productId
    );

    saveGuestEntries(entries);
    return getGuestCart();
}

export async function clearCart(): Promise<CartItem[]> {
    if (await isLoggedIn()) {
        return requestCart('', 'DELETE');
    }

    saveGuestEntries([]);
    return [];
}


export async function mergeGuestCart(): Promise<void> {
    if (!(await isLoggedIn())) return;

    const guestEntries = getGuestEntries();
    if (guestEntries.length === 0) return;

    const serverCart = await requestCart();
    const allProducts = await getProducts();

    for (const entry of guestEntries) {
        const product = allProducts.find(
            p => p.id === entry.productId
        );

        if (!product) {
            continue;
        }

        const existing = serverCart.find(
            item => item.product.id === entry.productId
        );

        const quantity = Math.min(
            (existing?.quantity ?? 0) + entry.quantity,
            product.quantity
        );

        if (quantity <= 0) {
            continue;
        }

        await requestCart(`/${entry.productId}`, 'PUT', {
            quantity
        });

        // Remove this guest item immediately after
        // it has successfully transferred.
        const remainingEntries = getGuestEntries().filter(
            item => item.productId !== entry.productId
        );

        saveGuestEntries(remainingEntries);

        const index = serverCart.findIndex(
            item => item.product.id === entry.productId
        );

        if (index >= 0) {
            serverCart[index].quantity = quantity;
        } else {
            serverCart.push({ product, quantity });
        }
    }
}
