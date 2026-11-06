
<script lang="ts">
    import { cart } from '../../lib/stores/cart';

    const TAX_RATE = 0.0825;

    let subtotal = $derived(
        $cart.reduce(
            (total, item) => total + item.product.price * item.quantity,
            0
        )
    );

    let tax = $derived(
        Math.round(subtotal * TAX_RATE * 100) / 100
    );

    let total = $derived(subtotal + tax);
</script>

<svelte:head>
    <title>Shopping Cart | Viral Vault</title>
</svelte:head>

<main>
    <h1>Your Shopping Cart</h1>

    {#if $cart.length === 0}
        <p>Your cart is empty.</p>
        <a href="/shop">Continue Shopping</a>
    {:else}
        <section class="cart-items">
            {#each $cart as item (item.product.id)}
                <article class="cart-item">
                    <img
                        src={item.product.imageUrl}
                        alt={item.product.name}
                    />

                    <div class="item-details">
                        <h2>{item.product.name}</h2>
                        <p>${item.product.price.toFixed(2)} each</p>

                        <div class="quantity-controls">
                            <button
                                onclick={() =>
                                    cart.updateQuantity(
                                        item.product.id,
                                        item.quantity - 1
                                    )}
                                disabled={item.quantity <= 1}
                            >
                                -
                            </button>

                            <span>{item.quantity}</span>

                            <button
                                onclick={() =>
                                    cart.updateQuantity(
                                        item.product.id,
                                        item.quantity + 1
                                    )}
                                disabled={item.quantity >= item.product.quantity}
                            >
                                +
                            </button>
                        </div>

                        <button
                            class="remove-button"
                            onclick={() => cart.removeItem(item.product.id)}
                        >
                            Remove
                        </button>
                    </div>

                    <p class="item-total">
                        ${(item.product.price * item.quantity).toFixed(2)}
                    </p>
                </article>
            {/each}
        </section>

        <section class="order-summary">
            <h2>Order Summary</h2>

            <p>Subtotal: ${subtotal.toFixed(2)}</p>
            <p>Sales Tax (8.25%): ${tax.toFixed(2)}</p>
            <p><strong>Total: ${total.toFixed(2)}</strong></p>

            <button onclick={() => cart.clearCart()}>
                Clear Cart
            </button>
        </section>
    {/if}
</main>

<style>
    main {
        max-width: 1000px;
        margin: 0 auto;
        padding: 2rem;
    }

    .cart-items {
        display: flex;
        flex-direction: column;
        gap: 1.5rem;
        margin-top: 2rem;
    }

    .cart-item {
        display: flex;
        align-items: center;
        gap: 1.5rem;
        padding: 1.25rem;
        background-color: #f9d976;
        border: 2px solid #40342e;
        border-radius: 12px;
    }

    .cart-item img {
        width: 100px;
        height: 100px;
        object-fit: contain;
    }

    .item-details {
        flex: 1;
    }

    .item-details h2 {
        font-size: 1.2rem;
    }

    .quantity-controls {
        display: flex;
        align-items: center;
        gap: 1rem;
        margin: 1rem 0;
    }

    button {
        padding: 0.5rem 0.75rem;
        border: 2px solid #40342e;
        border-radius: 8px;
        background-color: #e8a598;
        cursor: pointer;
    }

    button:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    .remove-button {
        background-color: #f0c0b8;
    }

    .item-total {
        font-weight: bold;
    }

    .order-summary {
        margin-top: 2rem;
        padding: 1.5rem;
        background-color: #f9d976;
        border: 2px solid #40342e;
        border-radius: 12px;
    }
</style>
