
<script lang="ts">
    import { onMount } from 'svelte';

    type OrderItem = {
        product_id: number;
        product_name: string | null;
        image_url: string | null;
        quantity: number;
        price_at_purchase: number;
    };

    type Order = {
        orderId: number;
        totalAmount: number;
        orderDate: string;
        status: string;
        items: OrderItem[];
    };

    let orders = $state<Order[]>([]);
    let loading = $state(true);
    let error = $state('');
    let notLoggedIn = $state(false);

    onMount(async () => {
        try {
            const response = await fetch(
                'http://localhost:8080/api/orders/my',
                { credentials: 'include' }
            );

            if (response.status === 401) {
                notLoggedIn = true;
                return;
            }

            if (!response.ok) {
                throw new Error('Unable to load order history.');
            }

            orders = await response.json();
        } catch {
            error = 'Unable to connect to the server.';
        } finally {
            loading = false;
        }
    });

    function formatDate(value: string): string {
        if (!value) return 'Unknown date';

        const date = new Date(value.replace(' ', 'T'));

        if (Number.isNaN(date.getTime())) {
            return value;
        }

        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    function formatMoney(value: number): string {
        return Number(value).toFixed(2);
    }
</script>

<svelte:head>
    <title>My Orders | Viral Vault</title>
</svelte:head>

<main class="orders-page">
    <h1>My Orders</h1>

    {#if loading}
        <p>Loading your orders...</p>

    {:else if notLoggedIn}
        <section class="message">
            <h2>Please log in</h2>
            <p>You must be logged in to view your order history.</p>
            <a href="/login">Go to Login</a>
        </section>

    {:else if error}
        <section class="message">
            <h2>Something went wrong</h2>
            <p role="alert">{error}</p>
        </section>

    {:else if orders.length === 0}
        <section class="message">
            <h2>No orders yet</h2>
            <p>
                Once you place an order, your purchases
                will appear here.
            </p>
            <a href="/">Continue Shopping</a>
        </section>

    {:else}
        <div class="orders-list">
            {#each orders as order (order.orderId)}
                <section class="order-card">
                    <div class="order-header">
                        <div>
                            <h2>Order #{order.orderId}</h2>
                            <p>{formatDate(order.orderDate)}</p>
                        </div>

                        <span class="status">{order.status}</span>
                    </div>

                    <div class="order-items">
                        {#each order.items as item}
                            <div class="order-item">
                                {#if item.image_url}
                                    <img
                                        src={item.image_url}
                                        alt={item.product_name ?? 'Product'}
                                    />
                                {/if}

                                <div class="item-details">
                                    <strong>
                                        {item.product_name ?? 'Product unavailable'}
                                    </strong>
                                    <p>Quantity: {item.quantity}</p>
                                    <p>
                                        Price each:
                                        ${formatMoney(item.price_at_purchase)}
                                    </p>
                                </div>

                                <strong>
                                    ${formatMoney(
                                        item.price_at_purchase * item.quantity
                                    )}
                                </strong>
                            </div>
                        {/each}
                    </div>

                    <div class="order-footer">
                        <strong>
                            Order Total:
                            ${formatMoney(order.totalAmount)}
                        </strong>
                    </div>
                </section>
            {/each}
        </div>
    {/if}
</main>

<style>
    .orders-page {
        max-width: 1000px;
        margin: 0 auto;
        padding: 3rem 1.5rem;
    }

    h1 {
        font-size: 2rem;
        margin-bottom: 2rem;
    }

    h2 {
        font-size: 1.25rem;
        margin: 0;
    }

    .message {
        text-align: center;
        padding: 4rem 1rem;
        border: 1px solid #ddd;
        border-radius: 8px;
    }

    .message p {
        margin: 1rem 0;
    }

    .message a {
        display: inline-block;
        margin-top: 1rem;
    }

    .orders-list {
        display: flex;
        flex-direction: column;
        gap: 1.5rem;
    }

    .order-card {
        border: 1px solid #ddd;
        border-radius: 8px;
        padding: 1.5rem;
    }

    .order-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 1rem;
        padding-bottom: 1rem;
        border-bottom: 1px solid #ddd;
    }

    .order-header p {
        color: #777;
        margin: 0.5rem 0 0;
    }

    .status {
        border: 1px solid #ddd;
        border-radius: 6px;
        padding: 0.4rem 0.75rem;
        font-size: 0.9rem;
    }

    .order-items {
        padding: 0.5rem 0;
    }

    .order-item {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1rem 0;
        border-bottom: 1px solid #eee;
    }

    .order-item img {
        width: 80px;
        height: 80px;
        object-fit: contain;
        border-radius: 6px;
    }

    .item-details {
        flex: 1;
    }

    .item-details p {
        margin: 0.35rem 0;
        color: #777;
        font-size: 0.9rem;
    }

    .order-footer {
        display: flex;
        justify-content: flex-end;
        padding-top: 1.5rem;
        font-size: 1.1rem;
    }

    @media (max-width: 600px) {
        .order-header {
            flex-direction: column;
        }

        .order-item {
            flex-wrap: wrap;
        }

        .order-card {
            padding: 1rem;
        }
    }
</style>
