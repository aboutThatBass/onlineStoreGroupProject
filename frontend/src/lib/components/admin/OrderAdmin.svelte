
<script lang="ts">
    import { onMount } from 'svelte';
    import OrderReceipt from '../OrderSnapshot.svelte';
    import type { OrderSnapshot } from '../../types/OrderSnapshot';

    const API_URL = 'http://localhost:8080/api/orders';

    interface Order {
        orderId: number;
        userId: number;
        customer: string;
        email: string;
        orderDate: string;
        totalAmount: number;
        status: string;
        fulfilled: boolean;
        snapshot: OrderSnapshot | null;
    }

    interface OrderItem {
        product_id: number;
        product_name: string | null;
        image_url: string | null;
        quantity: number;
        price_at_purchase: number;
    }

    interface OrderDetails extends Order {
        items: OrderItem[];
    }

    let orders = $state<Order[]>([]);
    let selectedOrder = $state<OrderDetails | null>(null);

    let loading = $state(true);
    let loadingDetails = $state(false);
    let updating = $state(false);

    let error = $state('');
    let success = $state('');

    let sortBy = $state('date');
    let sortDirection = $state('desc');
    let searchQuery = $state('');

    // Search orders by order ID or customer name.
    let filteredOrders = $derived.by(() => {
        const query = searchQuery.trim().toLowerCase();

        if (!query) return orders;

        const idQuery = query.replace(/^#/, '');

        return orders.filter(order =>
            (
                idQuery !== '' &&
                String(order.orderId).includes(idQuery)
            ) ||
            (order.customer ?? '')
                .toLowerCase()
                .includes(query)
        );
    });

    // Sort both current and past orders.
    let sortedOrders = $derived.by(() => {
        return [...filteredOrders].sort((a, b) => {
            let comparison = 0;

            switch (sortBy) {
                case 'id':
                    comparison = a.orderId - b.orderId;
                    break;

                case 'customer':
                    comparison = (a.customer ?? '').localeCompare(
                        b.customer ?? ''
                    );
                    break;

                case 'total':
                    comparison =
                        Number(a.totalAmount) -
                        Number(b.totalAmount);
                    break;

                default:
                    comparison = String(a.orderDate)
                        .localeCompare(String(b.orderDate));
                    break;
            }

            if (comparison === 0) {
                comparison = a.orderId - b.orderId;
            }

            return sortDirection === 'asc'
                ? comparison
                : -comparison;
        });
    });

    // Display unfulfilled orders.
    let currentOrders = $derived(
        sortedOrders.filter(order => !order.fulfilled)
    );

    // Display fulfilled orders.
    let orderHistory = $derived(
        sortedOrders.filter(order => order.fulfilled)
    );

    onMount(() => {
        void loadOrders();
    });

    function formatMoney(value: number) {
        return Number(value).toFixed(2);
    }

    function formatDate(value: string) {
        if (!value) return 'Unknown';

        const normalized = String(value).replace(' ', 'T');

        const timestamp =
            /(?:Z|[+-]\d{2}:\d{2})$/i.test(normalized)
                ? normalized
                : normalized + 'Z';

        const date = new Date(timestamp);

        if (Number.isNaN(date.getTime())) {
            return String(value);
        }

        return date.toLocaleString();
    }

    async function loadOrders() {
        loading = true;
        error = '';

        try {
            const response = await fetch(API_URL, {
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(
                    `Unable to load orders (${response.status}).`
                );
            }

            orders = await response.json();

        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Unable to load orders.';

        } finally {
            loading = false;
        }
    }

    async function viewDetails(orderId: number) {
        if (loadingDetails || updating) return;

        loadingDetails = true;
        selectedOrder = null;
        error = '';

        try {
            const response = await fetch(
                `${API_URL}/${orderId}`,
                {
                    credentials: 'include'
                }
            );

            if (!response.ok) {
                throw new Error(
                    `Unable to retrieve order (${response.status}).`
                );
            }

            selectedOrder = await response.json();

        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Unable to retrieve order details.';

        } finally {
            loadingDetails = false;
        }
    }

    async function updateFulfillment(
        order: Order,
        fulfilled: boolean
    ) {
        if (updating || loadingDetails) return;

        const action = fulfilled
            ? 'mark as fulfilled'
            : 'mark as pending';

        const confirmed = window.confirm(
            `Are you sure you want to ${action} order #${order.orderId}?`
        );

        if (!confirmed) return;

        updating = true;
        error = '';
        success = '';

        try {
            const response = await fetch(
                `${API_URL}/${order.orderId}`,
                {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include',
                    body: JSON.stringify({ fulfilled })
                }
            );

            if (!response.ok) {
                throw new Error(
                    `Unable to update order (${response.status}).`
                );
            }

            const updatedOrder: OrderDetails =
                await response.json();

            orders = orders.map(existing =>
                existing.orderId === updatedOrder.orderId
                    ? updatedOrder
                    : existing
            );

            if (
                selectedOrder?.orderId ===
                updatedOrder.orderId
            ) {
                selectedOrder = updatedOrder;
            }

            success = fulfilled
                ? `Order #${order.orderId} marked as fulfilled.`
                : `Order #${order.orderId} returned to Current Orders.`;

        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Unable to update order status.';

        } finally {
            updating = false;
        }
    }
</script>

<h2>Order Management</h2>

<!-- Search and sort current and past orders -->
<div class="search-section">

    <div class="order-controls">

        <div class="search-group">

            <label for="order-search">
                Search Orders:
            </label>

            <div class="search-controls">

                <input
                    id="order-search"
                    type="search"
                    placeholder="Search by order ID or customer name..."
                    bind:value={searchQuery}
                />

                <button onclick={() => searchQuery = ''}>
                    Clear
                </button>

            </div>

        </div>

        <div class="sort-group">

            <label for="order-sort">
                Sort By:
            </label>

            <select id="order-sort" bind:value={sortBy}>
                <option value="date">Order Date</option>
                <option value="id">Order ID</option>
                <option value="customer">Customer</option>
                <option value="total">Order Total</option>
            </select>

        </div>

        <div class="sort-group">

            <label for="sort-direction">
                Direction:
            </label>

            <select
                id="sort-direction"
                bind:value={sortDirection}
            >
                <option value="desc">Descending</option>
                <option value="asc">Ascending</option>
            </select>

        </div>

    </div>

    {#if searchQuery.trim() && !loading && !error}
        <p class="search-results">
            Found {filteredOrders.length} matching
            {filteredOrders.length === 1 ? 'order' : 'orders'}.
        </p>
    {/if}

</div>

{#if success}
    <p class="success-message">{success}</p>
{/if}

{#if error}
    <p class="error-message">{error}</p>
{/if}

{#if loadingDetails}
    <p>Loading order details...</p>
{/if}

<!-- Selected order details -->
{#if selectedOrder}
    <section class="order-details">

        <div class="details-header">

            <h3>Order #{selectedOrder.orderId} Details</h3>

            <button onclick={() => selectedOrder = null}>
                Close
            </button>

        </div>

        <p>
            <strong>Customer:</strong>
            {selectedOrder.customer}
        </p>

        <p>
            <strong>Email:</strong>
            {selectedOrder.email}
        </p>

        <p>
            <strong>Order Date:</strong>
            {formatDate(selectedOrder.orderDate)}
        </p>

        <p>
            <strong>Status:</strong>
            {selectedOrder.status}
        </p>

        <h4>Purchased Items</h4>

        <table>
            <thead>
                <tr>
                    <th>Product</th>
                    <th>Quantity</th>
                    <th>Unit Price</th>
                    <th>Subtotal</th>
                </tr>
            </thead>

            <tbody>
                {#each selectedOrder.items as item}
                    <tr>
                        <td>
                            {item.product_name ??
                                `Product #${item.product_id}`}
                        </td>

                        <td>{item.quantity}</td>

                        <td>
                            ${formatMoney(item.price_at_purchase)}
                        </td>

                        <td>
                            ${formatMoney(
                                item.price_at_purchase *
                                item.quantity
                            )}
                        </td>
                    </tr>
                {/each}
            </tbody>
        </table>

        <p class="order-total">
            Order Total:
            ${formatMoney(selectedOrder.totalAmount)}
        </p>

        <button
            disabled={updating || loadingDetails}
            onclick={() => {
                if (selectedOrder) {
                    updateFulfillment(
                        selectedOrder,
                        !selectedOrder.fulfilled
                    );
                }
            }}
        >
            {selectedOrder.fulfilled
                ? 'Mark as Pending'
                : 'Mark as Fulfilled'}
        </button>

        <OrderReceipt snapshot={selectedOrder.snapshot} />

    </section>
{/if}

<!-- Current Orders -->
<h3>Current Orders ({currentOrders.length})</h3>

<table>
    <thead>
        <tr>
            <th>Order ID</th>
            <th>Customer</th>
            <th>Date</th>
            <th>Total</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
    </thead>

    <tbody>
        {#if loading}
            <tr>
                <td colspan="6">Loading orders...</td>
            </tr>

        {:else if error && orders.length === 0}
            <tr>
                <td colspan="6">Orders unavailable.</td>
            </tr>

        {:else if currentOrders.length === 0}
            <tr>
                <td colspan="6">
                    {searchQuery.trim()
                        ? 'No matching current orders.'
                        : 'No current orders.'}
                </td>
            </tr>

        {:else}
            {#each currentOrders as order (order.orderId)}
                <tr>
                    <td>{order.orderId}</td>
                    <td>{order.customer}</td>
                    <td>{formatDate(order.orderDate)}</td>
                    <td>${formatMoney(order.totalAmount)}</td>

                    <td>
                        <span class="pending">
                            {order.status}
                        </span>
                    </td>

                    <td>
                        <button
                            disabled={loadingDetails || updating}
                            onclick={() =>
                                viewDetails(order.orderId)}
                        >
                            View Details
                        </button>

                        <button
                            disabled={updating || loadingDetails}
                            onclick={() =>
                                updateFulfillment(order, true)}
                        >
                            Fulfill
                        </button>
                    </td>
                </tr>
            {/each}
        {/if}
    </tbody>
</table>

<hr />

<!-- Order History -->
<h3>Order History ({orderHistory.length})</h3>

<table>
    <thead>
        <tr>
            <th>Order ID</th>
            <th>Customer</th>
            <th>Date</th>
            <th>Total</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
    </thead>

    <tbody>
        {#if loading}
            <tr>
                <td colspan="6">Loading order history...</td>
            </tr>

        {:else if error && orders.length === 0}
            <tr>
                <td colspan="6">Order history unavailable.</td>
            </tr>

        {:else if orderHistory.length === 0}
            <tr>
                <td colspan="6">
                    {searchQuery.trim()
                        ? 'No matching past orders.'
                        : 'No fulfilled orders.'}
                </td>
            </tr>

        {:else}
            {#each orderHistory as order (order.orderId)}
                <tr>
                    <td>{order.orderId}</td>
                    <td>{order.customer}</td>
                    <td>{formatDate(order.orderDate)}</td>
                    <td>${formatMoney(order.totalAmount)}</td>

                    <td>
                        <span class="fulfilled">
                            {order.status}
                        </span>
                    </td>

                    <td>
                        <button
                            disabled={loadingDetails || updating}
                            onclick={() =>
                                viewDetails(order.orderId)}
                        >
                            View Details
                        </button>

                        <button
                            disabled={updating || loadingDetails}
                            onclick={() =>
                                updateFulfillment(order, false)}
                        >
                            Mark Pending
                        </button>
                    </td>
                </tr>
            {/each}
        {/if}
    </tbody>
</table>

<style>
    table {
        width: 100%;
        border-collapse: collapse;
        margin-bottom: 20px;
    }

    th, td {
        padding: 10px;
        border: 1px solid #ccc;
        text-align: left;
    }

    th {
        background: #eee;
    }

    button {
        margin-right: 5px;
        cursor: pointer;
    }

    button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .pending {
        color: #946200;
        font-weight: 600;
    }

    .fulfilled {
        color: #166534;
        font-weight: 600;
    }

    .order-details {
        border: 1px solid #ccc;
        background-color: #fafafa;
        padding: 20px;
        margin-bottom: 24px;
        border-radius: 4px;
    }

    .details-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    .details-header h3 {
        margin: 0;
    }

    .order-total {
        font-weight: bold;
        text-align: right;
    }

    .success-message {
        background: #f0fdf4;
        color: #166534;
        border: 1px solid #bbf7d0;
        padding: 10px;
        border-radius: 4px;
    }

    .error-message {
        background: #fef2f2;
        color: #b91c1c;
        border: 1px solid #fecaca;
        padding: 10px;
        border-radius: 4px;
    }

    .search-section {
        margin: 20px 0 25px;
    }

    .search-section label {
        display: block;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .order-controls {
        display: flex;
        align-items: flex-end;
        flex-wrap: wrap;
        gap: 20px;
    }

    .search-group {
        flex: 1;
        max-width: 500px;
        min-width: min(100%, 320px);
    }

    .search-controls {
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .search-controls input {
        flex: 1;
        width: 100%;
        min-width: 0;
        height: 42px;
        padding: 10px 12px;
        border: 1px solid #999;
        border-radius: 4px;
        font-family: inherit;
        font-size: 15px;
        box-sizing: border-box;
    }

    .search-controls input:focus {
        outline: 2px solid #333;
        outline-offset: 1px;
    }

    .search-controls button {
        flex-shrink: 0;
        height: 42px;
        padding: 10px 16px;
        margin-right: 0;
        border: 1px solid #999;
        border-radius: 4px;
        background: #eee;
        font-family: inherit;
        font-size: 15px;
        cursor: pointer;
        box-sizing: border-box;
    }

    .search-controls button:hover {
        background: #ddd;
    }

    .sort-group {
        display: flex;
        flex-direction: column;
    }

    .sort-group select {
        height: 42px;
        min-width: 145px;
        padding: 8px 12px;
        border: 1px solid #999;
        border-radius: 4px;
        background: white;
        font-family: inherit;
        font-size: 15px;
        cursor: pointer;
        box-sizing: border-box;
    }

    .sort-group select:focus {
        outline: 2px solid #333;
        outline-offset: 1px;
    }

    .search-results {
        color: #666;
        font-size: 14px;
        margin-top: 8px;
    }

    @media (max-width: 700px) {
        .search-group {
            flex-basis: 100%;
            max-width: 100%;
            min-width: 0;
        }

        .order-controls {
            gap: 15px;
        }

        .sort-group {
            flex: 1;
        }

        .sort-group select {
            width: 100%;
            min-width: 0;
        }
    }
</style>
