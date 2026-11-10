
import { writable, get } from 'svelte/store';
import type { Product } from '../types/Product';

export type CartItem = {
    product: Product;
    quantity: number;
};

const API_URL = 'http://localhost:8080/api/cart';
const SESSION_URL = 'http://localhost:8080/api/users/session';
const GUEST_KEY = 'viral-vault-guest-cart';

const browser = typeof window !== 'undefined';

function readGuestCart(): CartItem[] {
    if (!browser) return [];

    try {
        const saved = localStorage.getItem(GUEST_KEY);
        const parsed = saved ? JSON.parse(saved) : [];

        return Array.isArray(parsed) ? parsed : [];
    } catch {
        return [];
    }
}

function writeGuestCart(items: CartItem[]) {
    if (browser) {
        localStorage.setItem(GUEST_KEY, JSON.stringify(items));
    }
}

function createCart() {
    const { subscribe, set, update } = writable<CartItem[]>([]);

    let activeUserId: number | null = null;
    let generation = 0;
    let pending: Promise<void> = Promise.resolve();
    let initialized = false;
    let loading = false;

    function showGuestCart() {
        activeUserId = null;
        set(readGuestCart());
    }

    async function fetchAccountCart(): Promise<CartItem[]> {
        const response = await fetch(API_URL, {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error(`Failed to load cart: ${response.status}`);
        }

        return response.json();
    }

    async function putQuantity(
        productId: number,
        quantity: number
    ): Promise<CartItem[]> {
        const response = await fetch(`${API_URL}/${productId}`, {
            method: 'PUT',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ quantity })
        });

        if (!response.ok) {
            throw new Error(`Cart update failed: ${response.status}`);
        }

        return response.json();
    }

    async function mergeGuestCart(
        accountItems: CartItem[],
        currentGeneration: number
    ): Promise<CartItem[]> {
        const guestItems = readGuestCart();

        if (guestItems.length === 0) {
            return accountItems;
        }

        let mergedItems = accountItems;

        for (const guestItem of guestItems) {
            if (currentGeneration !== generation) {
                return mergedItems;
            }

            const product = guestItem.product;

            if (!product || product.quantity <= 0) {
                continue;
            }

            const existing = mergedItems.find(
                (item) => item.product.id === product.id
            );

            const quantity = Math.min(
                (existing?.quantity ?? 0) + guestItem.quantity,
                product.quantity
            );

            if (quantity > 0) {
                mergedItems = await putQuantity(product.id, quantity);
            }
        }

        if (currentGeneration === generation) {
            localStorage.removeItem(GUEST_KEY);
        }

        return mergedItems;
    }

    async function loadCart() {
        if (loading) return;

        loading = true;
        const currentGeneration = ++generation;

        try {
            const response = await fetch(SESSION_URL, {
                credentials: 'include'
            });

            if (currentGeneration !== generation) return;

            if (!response.ok) {
                showGuestCart();
                return;
            }

            const session = await response.json();

            if (!session.authenticated || !session.userId) {
                showGuestCart();
                return;
            }

            activeUserId = Number(session.userId);
            set([]);

            let items = await fetchAccountCart();

            if (currentGeneration !== generation) return;

            items = await mergeGuestCart(items, currentGeneration);

            if (currentGeneration === generation) {
                set(items);
            }
        } catch (error) {
            console.error('Error loading cart:', error);

            if (currentGeneration === generation) {
                if (activeUserId === null) {
                    showGuestCart();
                }
            }
        } finally {
            initialized = true;
            loading = false;
        }
    }

    function saveChange(
        request: () => Promise<CartItem[]>
    ) {
        const userId = activeUserId;
        const currentGeneration = generation;

        if (userId === null) return;

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
                    const items = await request();

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
            initialized = true;
            loading = false;
            showGuestCart();
        },

        addItem(product: Product) {
            if (product.quantity <= 0) return;

            if (activeUserId === null) {
                update((items) => {
                    const existing = items.find(
                        (item) => item.product.id === product.id
                    );

                    let nextItems: CartItem[];

                    if (existing) {
                        nextItems = items.map((item) =>
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
                    } else {
                        nextItems = [
                            ...items,
                            { product, quantity: 1 }
                        ];
                    }

                    writeGuestCart(nextItems);
                    return nextItems;
                });

                return;
            }

            saveChange(async () => {
                const items = await fetchAccountCart();

                const existing = items.find(
                    (item) => item.product.id === product.id
                );

                const quantity = Math.min(
                    (existing?.quantity ?? 0) + 1,
                    product.quantity
                );

                return putQuantity(product.id, quantity);
            });
        },

        removeItem(productId: number) {
            if (activeUserId === null) {
                update((items) => {
                    const nextItems = items.filter(
                        (item) => item.product.id !== productId
                    );

                    writeGuestCart(nextItems);
                    return nextItems;
                });

                return;
            }

            saveChange(async () => {
                const response = await fetch(
                    `${API_URL}/${productId}`,
                    {
                        method: 'DELETE',
                        credentials: 'include'
                    }
                );

                if (!response.ok) {
                    throw new Error('Failed to remove item');
                }

                return response.json();
            });
        },

        updateQuantity(productId: number, quantity: number) {
            if (activeUserId === null) {
                update((items) => {
                    const nextItems = items.map((item) =>
                        item.product.id === productId
                            ? {
                                  ...item,
                                  quantity: Math.max(
                                      1,
                                      Math.min(
                                          quantity,
                                          item.product.quantity
                                      )
                                  )
                              }
                            : item
                    );

                    writeGuestCart(nextItems);
                    return nextItems;
                });

                return;
            }

            saveChange(() => putQuantity(productId, quantity));
        },

        clearCart() {
            if (activeUserId === null) {
                set([]);
                writeGuestCart([]);
                return;
            }

            saveChange(async () => {
                const response = await fetch(API_URL, {
                    method: 'DELETE',
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('Failed to clear cart');
                }

                return response.json();
            });
        }
    };
}

export const cart = createCart();
