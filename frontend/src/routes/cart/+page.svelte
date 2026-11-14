
<script lang="ts">
    import { cart } from '../../lib/stores/cart.svelte';
    import { calculatePricing } from '../../lib/data/pricing';
    import { ArrowLeft } from '@lucide/svelte';

    let pricing = $derived(calculatePricing(cart.subtotal));

    async function changeQuantity(
        productId: number,
        quantity: number
    ) {
        await cart.update(productId, quantity);
    }

    async function removeItem(productId: number) {
        await cart.remove(productId);
    }
</script>

<svelte:head>
    <title>Shopping Cart | Viral Vault</title>
</svelte:head>

<main class="cart-page">
    <h1>Your Cart</h1>

    {#if cart.error}
        <p class="error">{cart.error}</p>
    {/if}

    {#if cart.items.length === 0}
        <div class="empty-cart">
            <h2>Your cart is empty</h2>
            <p>Looks like you haven't added anything yet.</p>

            <a href="/" class="continue-shopping">
                <ArrowLeft size={20} strokeWidth={2.2} />
                <span>Continue Shopping</span>
            </a>
        </div>
    {:else}
        <div class="cart-layout">
            <section class="cart-items">
                {#each cart.items as item (item.product.id)}
                    <article class="cart-item">
                        <img
                            src={item.product.imageUrl}
                            alt={item.product.name}
                        />

                        <div class="item-details">
                            <h2>{item.product.name}</h2>
                            <p>${item.product.price.toFixed(2)}</p>

                            <div class="quantity-controls">
                                <button
                                    onclick={() => changeQuantity(
                                        item.product.id,
                                        item.quantity - 1
                                    )}
                                    disabled={cart.loading}
                                    aria-label="Decrease quantity"
                                >−</button>

                                <span>{item.quantity}</span>

                                <button
                                    onclick={() => changeQuantity(
                                        item.product.id,
                                        item.quantity + 1
                                    )}
                                    disabled={
                                        cart.loading ||
                                        item.quantity >= item.product.quantity
                                    }
                                    aria-label="Increase quantity"
                                >+</button>
                            </div>

                            <button
                                class="remove-button"
                                onclick={() => removeItem(item.product.id)}
                                disabled={cart.loading}
                            >
                                Remove
                            </button>
                        </div>

                        <strong class="item-total">
                            ${(item.product.price * item.quantity).toFixed(2)}
                        </strong>
                    </article>
                {/each}
            </section>

            <aside class="order-summary">
                <h2>Order Summary</h2>

                <div class="summary-row">
                    <span>Subtotal</span>
                    <strong>${pricing.subtotal.toFixed(2)}</strong>
                </div>

                <div class="summary-row">
                    <span>Sales Tax (8.25%)</span>
                    <strong>${pricing.tax.toFixed(2)}</strong>
                </div>

                <hr />

                <div class="summary-row">
                    <strong>Estimated Total</strong>
                    <strong>${pricing.total.toFixed(2)}</strong>
                </div>

                <p class="shipping-note">
                    Discounts can be applied at checkout. Shipping is free.
                </p>

                <a href="/checkout" class="checkout-button">
                    Proceed to Checkout
                </a>

                <a href="/" class="back-to-shop">
                    <ArrowLeft size={19} strokeWidth={2.2} />
                    <span>Continue Shopping</span>
                </a>
            </aside>
        </div>
    {/if}
</main>

<style>
    .cart-page {
        max-width: 1200px;
        margin: 0 auto;
        padding: 3rem 1.5rem;
    }

    h1 {
        font-size: 2rem;
        margin-bottom: 2rem;
    }

    .cart-layout {
        display: grid;
        grid-template-columns: 2fr 1fr;
        gap: 2rem;
        align-items: start;
    }

    .cart-items {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .cart-item {
        display: flex;
        align-items: center;
        gap: 1.5rem;
        padding: 1.5rem;
        border: 1px solid #ddd;
        border-radius: 8px;
    }

    .cart-item img {
        width: 110px;
        height: 110px;
        object-fit: contain;
    }

    .item-details {
        flex: 1;
    }

    .item-details h2 {
        font-size: 1.1rem;
        margin-bottom: 0.5rem;
    }

    .quantity-controls {
        display: flex;
        align-items: center;
        gap: 1rem;
        margin-top: 1rem;
    }

    .quantity-controls button {
        padding: 0.4rem 0.8rem;
        cursor: pointer;
    }

    .remove-button {
        margin-top: 0.75rem;
        background: none;
        border: none;
        color: #dc2626;
        cursor: pointer;
        padding: 0;
    }

    .item-total {
        white-space: nowrap;
    }

    .order-summary {
        padding: 1.5rem;
        border: 1px solid #ddd;
        border-radius: 8px;
    }

    .order-summary h2 {
        margin-bottom: 1.5rem;
    }

    .summary-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 1rem;
    }

    .shipping-note {
        font-size: 0.85rem;
        color: #666;
        margin-bottom: 1.5rem;
    }

    .checkout-button {
        display: block;
        padding: 0.9rem;
        text-align: center;
        background: #222;
        color: white;
        border-radius: 6px;
        text-decoration: none;
    }

    /* Continue Shopping buttons */

    .continue-shopping,
    .back-to-shop {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.6rem;

        padding: 0.9rem 1.6rem;

        background-color: #fffdf7;
        border: 2px solid var(--color-border);
        border-radius: 12px;
        box-shadow: 4px 4px 0 var(--color-border);

        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 1.1rem;
        font-weight: 600;
        text-align: center;
        text-decoration: none;

        cursor: pointer;

        transition:
            background-color 0.2s ease,
            transform 0.2s ease,
            box-shadow 0.2s ease;
    }

    .continue-shopping:hover,
    .back-to-shop:hover {
        background-color: #fff0e8;
        transform: translateY(-2px);
        box-shadow: 4px 6px 0 var(--color-border);
    }

    .continue-shopping:active,
    .back-to-shop:active {
        transform: translateY(2px);
        box-shadow: 1px 1px 0 var(--color-border);
    }

    .continue-shopping:focus-visible,
    .back-to-shop:focus-visible {
        outline: 3px solid var(--color-border);
        outline-offset: 4px;
    }

    .continue-shopping {
        margin-top: 1.5rem;
    }

    .back-to-shop {
        display: flex;
        width: 100%;
        margin-top: 1rem;
    }

    /* Empty cart */

    .empty-cart {
        text-align: center;
        padding: 4rem 1rem;
    }

    .empty-cart h2 {
        margin-bottom: 1rem;
    }

    .empty-cart p {
        margin-bottom: 0.5rem;
    }

    .error {
        color: #dc2626;
        margin-bottom: 1rem;
    }

    @media (max-width: 768px) {
        .cart-layout {
            grid-template-columns: 1fr;
        }

        .cart-item {
            flex-wrap: wrap;
        }

        .cart-item img {
            width: 80px;
            height: 80px;
        }
    }
</style>
