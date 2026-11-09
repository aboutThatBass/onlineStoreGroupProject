
<script lang="ts">
    import { cart } from '../../lib/stores/cart.svelte';
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

    async function handleCheckout(event: SubmitEvent) {
        event.preventDefault();

        if (submitting || cart.items.length === 0) return;

        submitting = true;
        error = '';

        try {
            confirmation = await submitOrder(customer);
            await cart.load();
        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Checkout failed. Please try again.';
        } finally {
            submitting = false;
        }
    }
</script>

<svelte:head>
    <title>Checkout | Viral Vault</title>
</svelte:head>

<main class="checkout-page">
    {#if confirmation}
        <section class="confirmation">
            <h1>Order Placed!</h1>
            <p>Thank you, {confirmation.customer.firstName}!</p>
            <p>Your demo order has been created successfully.</p>

            <h2>Order #{confirmation.orderId}</h2>
            <p>Total: ${confirmation.total.toFixed(2)}</p>

            <a href="/">Return to Store</a>
        </section>
    {:else if cart.items.length === 0}
        <section class="empty-checkout">
            <h1>Your cart is empty</h1>
            <p>Add some products before checking out.</p>
            <a href="/">Continue Shopping</a>
        </section>
    {:else}
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
                    <p class="error" role="alert">{error}</p>
                {/if}

                <button
                    type="submit"
                    disabled={submitting || cart.loading}
                >
                    {submitting ? 'Placing Order...' : 'Place Order'}
                </button>

                <p class="demo-note">
                    Demo checkout — no payment will be collected.
                </p>
            </form>

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

                <div class="summary-item">
                    <strong>Subtotal</strong>
                    <strong>${cart.subtotal.toFixed(2)}</strong>
                </div>

                <p class="demo-note">
                    Shipping and taxes are not included in this demo.
                </p>
            </aside>
        </div>
    {/if}
</main>

<style>
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

    form, .order-summary {
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

    .confirmation, .empty-checkout {
        text-align: center;
        padding: 4rem 1rem;
    }

    .confirmation a, .empty-checkout a {
        display: inline-block;
        margin-top: 1.5rem;
    }

    @media (max-width: 768px) {
        .checkout-layout, .name-row {
            grid-template-columns: 1fr;
        }

        form, .order-summary {
            padding: 1.25rem;
        }
    }
</style>
