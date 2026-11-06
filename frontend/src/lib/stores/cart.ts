
import { writable } from 'svelte/store';
import type { Product } from '../types/Product';

const browser = typeof window !== 'undefined';

export type CartItem = {
    product: Product;
    quantity: number;
};

const STORAGE_KEY = 'viral-vault-cart';

function createCart() {
    let initialItems: CartItem[] = [];

    if (browser) {
        try {
            const savedCart = localStorage.getItem(STORAGE_KEY);

            if (savedCart) {
                const parsed = JSON.parse(savedCart);

                if (Array.isArray(parsed)) {
                    initialItems = parsed;
                }
            }
        } catch {
            initialItems = [];
        }
    }

    const { subscribe, update, set } = writable<CartItem[]>(initialItems);

    if (browser) {
        subscribe((items) => {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
        });
    }

    return {
        subscribe,

        addItem(product: Product) {
            update((items) => {
                if (product.quantity <= 0) {
                    return items;
                }

                const existing = items.find(
                    (item) => item.product.id === product.id
                );

                if (existing) {
                    return items.map((item) =>
                        item.product.id === product.id
                            ? {
                                  ...item,
                                  quantity: Math.min(
                                      item.quantity + 1,
                                      product.quantity
                                  )
                              }
                            : item
                    );
                }

                return [...items, { product, quantity: 1 }];
            });
        },

        removeItem(productId: number) {
            update((items) =>
                items.filter((item) => item.product.id !== productId)
            );
        },

        updateQuantity(productId: number, quantity: number) {
            update((items) =>
                items.map((item) =>
                    item.product.id === productId
                        ? {
                              ...item,
                              quantity: Math.max(
                                  1,
                                  Math.min(quantity, item.product.quantity)
                              )
                          }
                        : item
                )
            );
        },

        clearCart() {
            set([]);
        }
    };
}

export const cart = createCart();
