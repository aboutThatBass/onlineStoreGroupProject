
<script lang="ts">
    import { cart } from '../../lib/stores/cart';

    const TAX_RATE = 0.0825;

    const DISCOUNT_CODES = {
        LABUB00: { type: 'percentage', amount: 10 },
        '0HIO123': { type: 'percentage', amount: 20 },
        TUNGTUNG22: { type: 'fixed', amount: 5 }
    } as const;

    type DiscountCode = keyof typeof DISCOUNT_CODES;

    let discountInput = $state('');
    let appliedCode = $state<DiscountCode | null>(null);
    let discountMessage = $state('');
    let discountError = $state(false);

    const roundMoney = (amount: number) =>
        Math.round((amount + Number.EPSILON) * 100) / 100;

    let subtotal = $derived(
        roundMoney(
            $cart.reduce(
                (total, item) =>
                    total + item.product.price * item.quantity,
                0
            )
        )
    );

    let discount = $derived.by(() => {
        if (!appliedCode) return 0;

        const code = DISCOUNT_CODES[appliedCode];

        if (code.type === 'percentage') {
            return roundMoney(subtotal * (code.amount / 100));
        }

        return Math.min(subtotal, code.amount);
    });

    let discountedSubtotal = $derived(
        roundMoney(Math.max(0, subtotal - discount))
    );

    let tax = $derived(
        roundMoney(discountedSubtotal * TAX_RATE)
    );

    let total = $derived(
        roundMoney(discountedSubtotal + tax)
    );

    function applyDiscount() {
        const enteredCode = discountInput.trim().toUpperCase();

        if (
            Object.prototype.hasOwnProperty.call(
                DISCOUNT_CODES,
                enteredCode
            )
        ) {
            appliedCode = enteredCode as DiscountCode;
            discountMessage = `Discount code ${enteredCode} applied!`;
            discountError = false;
            discountInput = '';
        } else {
            discountMessage = 'Invalid discount code. Please try again.';
            discountError = true;
        }
    }

    function removeDiscount() {
        appliedCode = null;
        discountInput = '';
        discountMessage = 'Discount removed.';
        discountError = false;
    }

    function clearShoppingCart() {
        cart.clearCart();
        removeDiscount();
    }
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
                            onclick={() =>
                                cart.removeItem(item.product.id)
                            }
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

            <div class="discount-section">
                <h3>Discount Code</h3>

                {#if appliedCode}
                    <p>
                        Applied: <strong>{appliedCode}</strong>
                    </p>

                    <button onclick={removeDiscount}>
                        Remove Discount
                    </button>
                {:else}
                    <form onsubmit={(event) => {
                        event.preventDefault();
                        applyDiscount();
                    }}>
                        <input
                            type="text"
                            placeholder="Enter discount code"
                            aria-label="Discount code"
                            bind:value={discountInput}
                        />

                        <button type="submit">
                            Apply
                        </button>
                    </form>
                {/if}

                {#if discountMessage}
                    <p
                        class:error-message={discountError}
                        class:success-message={!discountError}
                        role="status"
                    >
                        {discountMessage}
                    </p>
                {/if}
            </div>

            <div class="summary-totals">
                <p>Subtotal: ${subtotal.toFixed(2)}</p>

                {#if appliedCode}
                    <p class="discount-amount">
                        Discount ({appliedCode}):
                        -${discount.toFixed(2)}
                    </p>

                    <p>
                        Discounted Subtotal:
                        ${discountedSubtotal.toFixed(2)}
                    </p>
                {/if}

                <p>Sales Tax (8.25%): ${tax.toFixed(2)}</p>

                <p>
                    <strong>Total: ${total.toFixed(2)}</strong>
                </p>
            </div>

            <button onclick={clearShoppingCart}>
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

    .discount-section {
        margin: 1.5rem 0;
        padding-bottom: 1.5rem;
        border-bottom: 1px solid #40342e;
    }

    .discount-section h3 {
        margin-bottom: 0.75rem;
    }

    .discount-section form {
        display: flex;
        flex-wrap: wrap;
        gap: 0.75rem;
    }

    .discount-section input {
        flex: 1;
        min-width: 160px;
        padding: 0.5rem;
        border: 2px solid #40342e;
        border-radius: 8px;
        background-color: #fffdf5;
        color: #40342e;
    }

    .success-message {
        color: #246b35;
        margin-top: 0.75rem;
    }

    .error-message {
        color: #a32020;
        margin-top: 0.75rem;
    }

    .discount-amount {
        color: #246b35;
        font-weight: bold;
    }

    .summary-totals {
        margin-bottom: 1.5rem;
    }
</style>
