
import { writable } from 'svelte/store';
import type { Product } from '../types/Product';

export type CartItem = {
    product: Product;
    quantity: number;
};

const API_URL = 'http://localhost:8080/api/cart';

function createCart() {
    const { subscribe, set, update } = writable<CartItem[]>([]);

    let activeUserId: number | null = null;
    let generation = 0;
    let pending = Promise.resolve();

    async function loadCart() {
        const currentGeneration = ++generation;
        activeUserId = null;
        set([]);

        try {
            const sessionResponse = await fetch(
                'http://localhost:8080/api/users/session',
                { credentials: 'include' }
            );

            if (!sessionResponse.ok) {
                return;
            }

            const session = await sessionResponse.json();

            if (!session.authenticated || !session.userId) {
                return;
            }

            const userId = Number(session.userId);

            const response = await fetch(API_URL, {
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Failed to load cart');
            }

            const items: CartItem[] = await response.json();

            if (currentGeneration === generation) {
                activeUserId = userId;
                set(items);
            }
        } catch (error) {
            console.error('Error loading cart:', error);
        }
    }

    function saveChange(
        request: () => Promise<Response>
    ) {
        const userId = activeUserId;
        const currentGeneration = generation;

        if (userId === null) {
            console.warn('Please log in to manage your cart.');
            return;
        }

        pending = pending
            .catch(() => {})
            .then(async () => {
                if (
                    currentGeneration !== generation ||
                    activeUserId !== userId
                ) {
                    return;
                }

                try {
                    const response = await request();

                    if (!response.ok) {
                        throw new Error(
                            `Cart update failed: ${response.status}`
                        );
                    }

                    const items: CartItem[] = await response.json();

                    if (
                        currentGeneration === generation &&
                        activeUserId === userId
                    ) {
                        set(items);
                    }
                } catch (error) {
                    console.error('Error updating cart:', error);
                }
            });
    }

    return {
        subscribe,

        loadCart,

        reset() {
            generation++;
            activeUserId = null;
            set([]);
        },

        addItem(product: Product) {
            if (product.quantity <= 0) return;

            saveChange(async () => {
                const response = await fetch(API_URL, {
                    credentials: 'include'
                });

                if (!response.ok) return response;

                const items: CartItem[] = await response.json();

                const existing = items.find(
                    (item) => item.product.id === product.id
                );

                const quantity = Math.min(
                    (existing?.quantity ?? 0) + 1,
                    product.quantity
                );

                return fetch(`${API_URL}/${product.id}`, {
                    method: 'PUT',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ quantity })
                });
            });
        },

        removeItem(productId: number) {
            saveChange(() =>
                fetch(`${API_URL}/${productId}`, {
                    method: 'DELETE',
                    credentials: 'include'
                })
            );
        },

        updateQuantity(productId: number, quantity: number) {
            saveChange(() =>
                fetch(`${API_URL}/${productId}`, {
                    method: 'PUT',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ quantity })
                })
            );
        },

        clearCart() {
            saveChange(() =>
                fetch(API_URL, {
                    method: 'DELETE',
                    credentials: 'include'
                })
            );
        }
    };
}

export const cart = createCart();
