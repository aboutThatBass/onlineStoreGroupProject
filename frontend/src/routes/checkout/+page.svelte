
<script lang="ts">
    import { Check, ShoppingBag, ArrowRight } from '@lucide/svelte';
    import { cart } from '../../lib/stores/cart.svelte';
    import { calculatePricing } from '../../lib/data/pricing';
    import {
        submitOrder,
        type CheckoutDetails,
        type OrderConfirmation
    } from '../../lib/data/orders';

    let customer = $state<CheckoutDetails>({
        firstName: '',
        lastName: '',
        email: '',
        address: '',
        city: '',
        state: '',
        zipCode: ''
    });

    let submitting = $state(false);
    let error = $state('');
    let confirmation = $state<OrderConfirmation | null>(null);

    let discountCode = $state('');
    let appliedDiscountCode = $state('');
    let discountError = $state('');

    let pricing = $derived(
        calculatePricing(cart.subtotal, appliedDiscountCode)
    );

    function applyDiscount() {
        const result = calculatePricing(
            cart.subtotal,
            discountCode
        );

        if (!result.validDiscount) {
            discountError = 'Invalid discount code.';
            return;
        }

        appliedDiscountCode =
            discountCode.trim().toUpperCase();

        discountError = '';
    }

    async function handleCheckout(event: SubmitEvent) {
        event.preventDefault();

        if (submitting || cart.items.length === 0) {
            return;
        }

        submitting = true;
        error = '';

        try {
            confirmation = await submitOrder(
                customer,
                appliedDiscountCode
            );
        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Checkout failed. Please try again.';
        } finally {
            submitting = false;
        }

        // The order is already saved at this point.
        // Refreshing the cart must not trigger a second purchase.
        if (confirmation) {
            try {
                await cart.load();
            } catch (err) {
                console.error(
                    'Order saved, but cart refresh failed:',
                    err
                );
            }
        }
    }
</script>

<svelte:head>
    <title>Checkout | Viral Vault</title>
</svelte:head>

<main class="checkout-page">

    {#if confirmation}

        <!-- Redesigned order confirmation -->
        <section
            class="confirmation"
            aria-labelledby="confirmation-title"
        >
            <div class="confirmation-icon">
                <Check size={38} strokeWidth={2.5} />
            </div>

            <h1 id="confirmation-title">Order Placed!</h1>

            <p class="confirmation-subtitle">
                Thank you, {confirmation.customer.firstName}!
                <span>
                    Your order has been saved successfully.
                </span>
            </p>

            <div class="confirmation-summary">
                <p class="summary-label">
                    ORDER CONFIRMATION
                </p>

                <h2>
                    Order #{confirmation.orderId}
                </h2>

                <div class="summary-divider"></div>

                <div class="confirmation-total">
                    <span>Order Total</span>

                    <strong>
                        ${confirmation.total.toFixed(2)}
                    </strong>
                </div>
            </div>

            <div class="confirmation-links">
                <a
                    href="/orders"
                    class="confirmation-primary"
                >
                    <ShoppingBag size={19} />
                    View My Orders
                    <ArrowRight size={18} />
                </a>

                <a
                    href="/"
                    class="confirmation-secondary"
                >
                    Return to Store
                </a>
            </div>
        </section>

    {:else if cart.items.length === 0}

        <!-- Empty cart -->
        <section class="empty-checkout">
            <h1>Your cart is empty</h1>

            <p>
                Add some products before checking out.
            </p>

            <a href="/">Continue Shopping</a>
        </section>

    {:else}

        <!-- Existing checkout form -->
        <h1>Checkout</h1>

        <div class="checkout-layout">

            <form onsubmit={handleCheckout}>
                <h2>Customer Information</h2>

                <div class="name-row">
                    <label>
                        First Name

                        <input
                            bind:value={customer.firstName}
                            required
                            autocomplete="given-name"
                        />
                    </label>

                    <label>
                        Last Name

                        <input
                            bind:value={customer.lastName}
                            required
                            autocomplete="family-name"
                        />
                    </label>
                </div>

                <label>
                    Email Address

                    <input
                        type="email"
                        bind:value={customer.email}
                        required
                        autocomplete="email"
                    />
                </label>

                <h2>Shipping Address</h2>

                <label>
                    Street Address

                    <input
                        bind:value={customer.address}
                        required
                        autocomplete="street-address"
                    />
                </label>

                <label>
                    City

                    <input
                        bind:value={customer.city}
                        required
                        autocomplete="address-level2"
                    />
                </label>

                <div class="name-row">
                    <label>
                        State

                        <input
                            bind:value={customer.state}
                            required
                            autocomplete="address-level1"
                        />
                    </label>

                    <label>
                        ZIP Code

                        <input
                            bind:value={customer.zipCode}
                            required
                            autocomplete="postal-code"
                        />
                    </label>
                </div>

                {#if error}
                    <p class="error" role="alert">
                        {error}
                    </p>
                {/if}

                <button
                    type="submit"
                    disabled={submitting || cart.loading}
                >
                    {submitting
                        ? 'Placing Order...'
                        : 'Place Order'}
                </button>

                <p class="demo-note">
                    Demo checkout — no payment will be collected.
                    Please log in to place an order.
                    Shipping details are not saved.
                </p>
            </form>

            <!-- Existing order summary -->
            <aside class="order-summary">
                <h2>Your Order</h2>

                {#each cart.items as item (item.product.id)}
                    <div class="summary-item">
                        <span>
                            {item.product.name} × {item.quantity}
                        </span>

                        <strong>
                            ${(item.product.price * item.quantity).toFixed(2)}
                        </strong>
                    </div>
                {/each}

                <hr />

                <div class="discount-section">
                    <label for="discount-code">
                        Discount Code
                    </label>

                    <input
                        id="discount-code"
                        type="text"
                        bind:value={discountCode}
                        placeholder="Enter discount code"
                    />

                    <button
                        type="button"
                        class="apply-discount"
                        onclick={applyDiscount}
                    >
                        Apply
                    </button>

                    {#if discountError}
                        <p class="error">
                            {discountError}
                        </p>
                    {/if}

                    {#if pricing.discount > 0}
                        <p class="discount-success">
                            Discount applied successfully!
                        </p>
                    {/if}
                </div>

                <div class="summary-item">
                    <strong>Subtotal</strong>

                    <strong>
                        ${pricing.subtotal.toFixed(2)}
                    </strong>
                </div>

                {#if pricing.discount > 0}
                    <div class="summary-item">
                        <span>
                            Discount ({pricing.discountCode})
                        </span>

                        <strong>
                            -${pricing.discount.toFixed(2)}
                        </strong>
                    </div>
                {/if}

                <div class="summary-item">
                    <span>Sales Tax (8.25%)</span>

                    <strong>
                        ${pricing.tax.toFixed(2)}
                    </strong>
                </div>

                <hr />

                <div class="summary-item">
                    <strong>Total</strong>

                    <strong>
                        ${pricing.total.toFixed(2)}
                    </strong>
                </div>

                <p class="demo-note">
                    Sales tax included. Shipping is not
                    charged in this demo.
                </p>
            </aside>

        </div>
    {/if}
</main>

<style>
    /* Existing checkout styles */

    .checkout-page {
        max-width: 1100px;
        margin: 0 auto;
        padding: 3rem 1.5rem;
    }

    h1 {
        font-size: 2rem;
        margin-bottom: 2rem;
    }

    h2 {
        font-size: 1.25rem;
        margin: 1.5rem 0 1rem;
    }

    .checkout-layout {
        display: grid;
        grid-template-columns: 1.5fr 1fr;
        gap: 3rem;
        align-items: start;
    }

    form,
    .order-summary {
        padding: 2rem;
        border: 1px solid #ddd;
        border-radius: 8px;
    }

    label {
        display: flex;
        flex-direction: column;
        gap: 0.4rem;
        margin-bottom: 1rem;
        font-weight: 500;
    }

    input {
        padding: 0.8rem;
        border: 1px solid #ccc;
        border-radius: 5px;
        font: inherit;
        width: 100%;
        box-sizing: border-box;
    }

    .name-row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 1rem;
    }

    form button {
        width: 100%;
        padding: 1rem;
        margin-top: 1rem;
        background: #222;
        color: white;
        border: none;
        border-radius: 6px;
        font: inherit;
        cursor: pointer;
    }

    form button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .summary-item {
        display: flex;
        justify-content: space-between;
        gap: 1rem;
        margin: 1rem 0;
    }

    .summary-item strong {
        white-space: nowrap;
    }

    .demo-note {
        font-size: 0.85rem;
        color: #777;
        margin-top: 1rem;
    }

    .error {
        color: #dc2626;
    }

    .empty-checkout {
        text-align: center;
        padding: 4rem 1rem;
    }

    .empty-checkout a {
        display: inline-block;
        margin-top: 1.5rem;
    }

    .discount-section {
        margin: 1.5rem 0;
    }

    .apply-discount {
        margin-top: 0.5rem;
        padding: 0.6rem 1rem;
        background: #222;
        color: white;
        border: none;
        border-radius: 5px;
        cursor: pointer;
    }

    .discount-success {
        color: #15803d;
        font-size: 0.85rem;
        margin-top: 0.5rem;
    }

    /* Redesigned order confirmation */

    .confirmation {
        width: 100%;
        max-width: 560px;
        margin: 1.5rem auto 3rem;
        padding: 2.5rem;
        background: #fffdf7;
        color: #40342e;
        border: 3px solid #40342e;
        border-radius: 18px;
        box-shadow: 7px 7px 0 #40342e;
        text-align: center;
        font-family: var(--font-body, 'Nunito', sans-serif);
        box-sizing: border-box;
    }

    .confirmation-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 76px;
        height: 76px;
        margin: 0 auto 1.25rem;
        background: #f9dc7a;
        border: 2px solid #40342e;
        border-radius: 50%;
    }

    .confirmation h1 {
        margin: 0 0 1rem;
        color: #40342e;
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 2.3rem;
        line-height: 1.2;
    }

    .confirmation-subtitle {
        margin: 0 0 1.75rem;
        color: #73645a;
        font-size: 1rem;
        line-height: 1.7;
    }

    .confirmation-subtitle span {
        display: block;
    }

    .confirmation-summary {
        padding: 1.5rem;
        background: #fff4d1;
        border: 1px solid #e9ded2;
        border-radius: 12px;
        text-align: center;
    }

    .summary-label {
        margin: 0 0 0.5rem;
        color: #73645a;
        font-size: 0.75rem;
        font-weight: 800;
        letter-spacing: 1.5px;
    }

    .confirmation-summary h2 {
        margin: 0.5rem 0 1.25rem;
        color: #40342e;
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 1.6rem;
    }

    .summary-divider {
        height: 1px;
        background: #e9ded2;
        margin-bottom: 1.25rem;
    }

    .confirmation-total {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        font-size: 1rem;
    }

    .confirmation-total strong {
        color: #40342e;
        font-size: 1.4rem;
        font-weight: 800;
    }

    .confirmation-demo-note {
        display: flex;
        align-items: flex-start;
        gap: 0.65rem;
        margin: 1.5rem 0;
        text-align: left;
        color: #73645a;
    }

    .confirmation-demo-note :global(svg) {
        flex-shrink: 0;
        margin-top: 2px;
    }

    .confirmation-demo-note p {
        margin: 0;
        font-size: 0.85rem;
        line-height: 1.6;
    }

    .confirmation-links {
        display: flex;
        flex-direction: column;
        gap: 0.85rem;
        margin-top: 1.5rem;
    }

    .confirmation-links a {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.65rem;
        width: 100%;
        margin: 0;
        padding: 0.9rem 1rem;
        border: 2px solid #40342e;
        border-radius: 10px;
        color: #40342e;
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 1.1rem;
        font-weight: 700;
        text-decoration: none;
        box-sizing: border-box;
        transition: background 0.2s ease, transform 0.2s ease;
    }

    .confirmation-links .confirmation-primary {
        background: #e8a598;
        box-shadow: 3px 3px 0 #40342e;
    }

    .confirmation-links .confirmation-primary:hover {
        background: #f2b8ad;
        transform: translateY(-2px);
    }

    .confirmation-links .confirmation-secondary {
        background: #fff;
    }

    .confirmation-links .confirmation-secondary:hover {
        background: #f5f1e9;
    }

    .confirmation-links a:focus-visible {
        outline: 2px solid #40342e;
        outline-offset: 4px;
    }

    @media (max-width: 768px) {
        .checkout-layout,
        .name-row {
            grid-template-columns: 1fr;
        }

        form,
        .order-summary {
            padding: 1.25rem;
        }
    }

    @media (max-width: 600px) {
        .confirmation {
            padding: 2rem 1.25rem;
            box-shadow: 5px 5px 0 #40342e;
        }

        .confirmation h1 {
            font-size: 1.9rem;
        }

        .confirmation-summary {
            padding: 1.25rem;
        }
    }
</style>
