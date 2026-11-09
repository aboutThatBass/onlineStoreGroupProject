
import {
    getCart,
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart,
    type CartItem
} from '../data/cart';

class CartStore {
    items = $state<CartItem[]>([]);
    loading = $state(false);
    error = $state<string | null>(null);

    hasItems = $derived(this.items.length > 0);

    subtotal = $derived(
        this.items.reduce(
            (total, item) =>
                total + item.product.price * item.quantity,
            0
        )
    );

    async load() {
        this.loading = true;
        this.error = null;

        try {
            this.items = await getCart();
        } catch (error) {
            this.handleError(error);
        } finally {
            this.loading = false;
        }
    }

    async add(productId: number) {
        await this.execute(() => addToCart(productId));
    }

    async update(productId: number, quantity: number) {
        await this.execute(() =>
            updateQuantity(productId, quantity)
        );
    }

    async remove(productId: number) {
        await this.execute(() => removeFromCart(productId));
    }

    async clear() {
        await this.execute(() => clearCart());
    }

    private async execute(
        operation: () => Promise<CartItem[]>
    ) {
        this.loading = true;
        this.error = null;

        try {
            this.items = await operation();
        } catch (error) {
            this.handleError(error);
        } finally {
            this.loading = false;
        }
    }

    private handleError(error: unknown) {
        this.error =
            error instanceof Error
                ? error.message
                : 'Something went wrong';
    }
}

export const cart = new CartStore();
