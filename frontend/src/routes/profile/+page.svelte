
<script lang="ts">
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';
    import { page } from '$app/state';
    import { UserRound, Package, Search, Pencil } from '@lucide/svelte';

    type Profile = {
        username: string;
        fullName: string;
        email: string;
        phoneNumber: string;
        address: string;
        city: string;
        state: string;
        zipCode: string;
    };

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

    type SortOption = 'newest' | 'oldest' | 'highest' | 'lowest';
    type AccountTab = 'profile' | 'orders';

    const PROFILE_API = 'http://localhost:8080/api/users/profile';
    const ORDERS_API = 'http://localhost:8080/api/orders/my';

    function emptyProfile(): Profile {
        return {
            username: '',
            fullName: '',
            email: '',
            phoneNumber: '',
            address: '',
            city: '',
            state: '',
            zipCode: ''
        };
    }

    let profile = $state<Profile>(emptyProfile());
    let originalProfile = $state<Profile>(emptyProfile());

    let activeTab = $state<AccountTab>('profile');

    let loading = $state(true);
    let saving = $state(false);
    let editing = $state(false);
    let error = $state('');
    let success = $state('');

    let orders = $state<Order[]>([]);
    let ordersLoading = $state(false);
    let ordersLoaded = $state(false);
    let ordersError = $state('');

    let searchQuery = $state('');
    let sortBy = $state<SortOption>('newest');

    // Support links such as /profile?tab=orders
    $effect(() => {
        activeTab =
            page.url.searchParams.get('tab') === 'orders'
                ? 'orders'
                : 'profile';
    });

    onMount(() => {
        void loadProfile();
    });

    async function loadProfile() {
        loading = true;
        error = '';

        try {
            const response = await fetch(PROFILE_API, {
                credentials: 'include'
            });

            if (response.status === 401) {
                await goto('/login');
                return;
            }

            if (!response.ok) {
                throw new Error('Unable to load your profile.');
            }

            const data: Profile = await response.json();
            profile = { ...data };
            originalProfile = { ...data };
        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Something went wrong.';
        } finally {
            loading = false;
        }
    }

    async function selectTab(tab: AccountTab) {
        activeTab = tab;

        if (tab === 'orders' && !ordersLoaded && !ordersLoading) {
            void loadOrders();
        }

        await goto(
            tab === 'orders' ? '/profile?tab=orders' : '/profile',
            { noScroll: true, keepFocus: true }
        );
    }

    // Also load orders when opening the tab directly by URL.
    $effect(() => {
        if (activeTab === 'orders' && !ordersLoaded && !ordersLoading && !loading && !error) {
            void loadOrders();
        }
    });

    function startEditing() {
        profile = { ...originalProfile };
        editing = true;
        error = '';
        success = '';
    }

    function cancelEditing() {
        profile = { ...originalProfile };
        editing = false;
        error = '';
    }

    async function saveProfile(event: SubmitEvent) {
        event.preventDefault();

        saving = true;
        error = '';
        success = '';

        try {
            const response = await fetch(PROFILE_API, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    fullName: profile.fullName,
                    email: profile.email,
                    phoneNumber: profile.phoneNumber,
                    address: profile.address,
                    city: profile.city,
                    state: profile.state,
                    zipCode: profile.zipCode
                })
            });

            if (response.status === 401) {
                await goto('/login');
                return;
            }

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Unable to save profile.');
            }

            profile = { ...data };
            originalProfile = { ...data };
            editing = false;
            success = 'Your profile has been updated successfully!';
        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Something went wrong.';
        } finally {
            saving = false;
        }
    }

    async function loadOrders() {
        if (ordersLoading) return;

        ordersLoading = true;
        ordersError = '';

        try {
            const response = await fetch(ORDERS_API, {
                credentials: 'include'
            });

            if (response.status === 401) {
                await goto('/login');
                return;
            }

            if (!response.ok) {
                throw new Error('Unable to load order history.');
            }

            orders = await response.json();
            ordersLoaded = true;
        } catch (err) {
            ordersError = err instanceof Error
                ? err.message
                : 'Unable to connect to the server.';
        } finally {
            ordersLoading = false;
        }
    }

    function getDateTimestamp(value: string): number {
        if (!value) return 0;
        const timestamp = new Date(value.replace(' ', 'T')).getTime();
        return Number.isNaN(timestamp) ? 0 : timestamp;
    }

    let filteredOrders = $derived.by(() => {
        const query = searchQuery.trim().toLowerCase();

        const matching = orders.filter((order) => {
            if (!query) return true;

            const orderNumberQuery = query
                .replace(/^order\s*#?\s*/, '')
                .replace(/^#/, '');

            const matchesNumber =
                orderNumberQuery.length > 0 &&
                String(order.orderId).includes(orderNumberQuery);

            const matchesStatus =
                order.status.toLowerCase().includes(query);

            const matchesProduct = order.items.some((item) =>
                (item.product_name ?? '').toLowerCase().includes(query)
            );

            return matchesNumber || matchesStatus || matchesProduct;
        });

        return [...matching].sort((a, b) => {
            switch (sortBy) {
                case 'oldest':
                    return getDateTimestamp(a.orderDate) -
                        getDateTimestamp(b.orderDate);
                case 'highest':
                    return b.totalAmount - a.totalAmount;
                case 'lowest':
                    return a.totalAmount - b.totalAmount;
                default:
                    return getDateTimestamp(b.orderDate) -
                        getDateTimestamp(a.orderDate);
            }
        });
    });

    function formatDate(value: string): string {
        if (!value) return 'Unknown date';

        const date = new Date(value.replace(' ', 'T'));
        if (Number.isNaN(date.getTime())) return value;

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
    <title>My Account | Viral Vault</title>
</svelte:head>

<main class="account-page">
    <header class="account-banner">
        <div class="banner-icon">
            <UserRound size={35} strokeWidth={2} />
        </div>
        <div>
            <h1>My Account</h1>
            <p>Manage your profile and purchases</p>
        </div>
    </header>

    <div class="account-tabs" role="tablist" aria-label="Account sections">
        <button
            type="button"
            role="tab"
            id="profile-tab"
            aria-selected={activeTab === 'profile'}
            aria-controls="profile-panel"
            class:active={activeTab === 'profile'}
            onclick={() => selectTab('profile')}
        >
            <UserRound size={23} />
            <span>Profile Details</span>
        </button>

        <button
            type="button"
            role="tab"
            id="orders-tab"
            aria-selected={activeTab === 'orders'}
            aria-controls="orders-panel"
            class:active={activeTab === 'orders'}
            onclick={() => selectTab('orders')}
        >
            <Package size={23} />
            <span>My Orders</span>
        </button>
    </div>

    {#if activeTab === 'profile'}
        <section
            class="account-panel"
            id="profile-panel"
            role="tabpanel"
            aria-labelledby="profile-tab"
        >
            <div class="panel-heading">
                <div>
                    <h2>Personal Information</h2>
                    <p>Keep your account details up to date.</p>
                </div>
                <Pencil size={23} />
            </div>

            {#if loading}
                <p>Loading your profile...</p>
            {:else}
                {#if error}
                    <p class="message error" role="alert">{error}</p>
                {/if}

                {#if success}
                    <p class="message success" role="status">{success}</p>
                {/if}

                <form onsubmit={saveProfile}>
                    <div class="field">
                        <label for="username">Username</label>
                        <input
                            id="username"
                            value={profile.username}
                            disabled
                        />
                        <small>Username cannot be changed.</small>
                    </div>

                    <div class="field">
                        <label for="fullName">Full Name</label>
                        <input
                            id="fullName"
                            bind:value={profile.fullName}
                            disabled={!editing}
                            maxlength="100"
                        />
                    </div>

                    <div class="field">
                        <label for="email">Email Address</label>
                        <input
                            id="email"
                            type="email"
                            bind:value={profile.email}
                            disabled={!editing}
                            maxlength="100"
                            required
                        />
                    </div>

                    <div class="field">
                        <label for="phone">Phone Number</label>
                        <input
                            id="phone"
                            type="tel"
                            bind:value={profile.phoneNumber}
                            disabled={!editing}
                            required
                        />
                    </div>

                    <div class="field">
                        <label for="address">Street Address</label>
                        <input
                            id="address"
                            bind:value={profile.address}
                            disabled={!editing}
                            maxlength="255"
                        />
                    </div>

                    <div class="field-row">
                        <div class="field">
                            <label for="city">City</label>
                            <input
                                id="city"
                                bind:value={profile.city}
                                disabled={!editing}
                                maxlength="100"
                            />
                        </div>

                        <div class="field">
                            <label for="state">State</label>
                            <input
                                id="state"
                                bind:value={profile.state}
                                disabled={!editing}
                                maxlength="50"
                            />
                        </div>
                    </div>

                    <div class="field">
                        <label for="zip">ZIP Code</label>
                        <input
                            id="zip"
                            bind:value={profile.zipCode}
                            disabled={!editing}
                            maxlength="20"
                        />
                    </div>

                    <div class="actions">
                        {#if editing}
                            <button
                                type="submit"
                                class="primary-button"
                                disabled={saving}
                            >
                                {saving ? 'Saving...' : 'Save Changes'}
                            </button>

                            <button
                                type="button"
                                class="secondary-button"
                                onclick={cancelEditing}
                                disabled={saving}
                            >
                                Cancel
                            </button>
                        {:else}
                            <button
                                type="button"
                                class="primary-button"
                                onclick={startEditing}
                            >
                                Edit Profile
                            </button>
                        {/if}
                    </div>
                </form>
            {/if}
        </section>
    {:else}
        <section
            class="account-panel"
            id="orders-panel"
            role="tabpanel"
            aria-labelledby="orders-tab"
        >
            <div class="panel-heading">
                <div>
                    <h2>Order History</h2>
                    <p>Review your past purchases.</p>
                </div>
                <Package size={25} />
            </div>

            {#if ordersLoading}
                <p>Loading your orders...</p>
            {:else if ordersError}
                <div class="empty-state">
                    <h3>Something went wrong</h3>
                    <p role="alert">{ordersError}</p>
                    <button
                        class="primary-button"
                        onclick={() => loadOrders()}
                    >
                        Try Again
                    </button>
                </div>
            {:else if orders.length === 0}
                <div class="empty-state">
                    <Package size={40} />
                    <h3>No orders yet</h3>
                    <p>
                        Once you place an order, your purchases
                        will appear here.
                    </p>
                    <a href="/" class="primary-button">
                        Continue Shopping
                    </a>
                </div>
            {:else}
                <div class="order-controls">
                    <div class="field">
                        <label for="order-search">Search Orders</label>
                        <div class="search-input">
                            <Search size={19} />
                            <input
                                id="order-search"
                                type="search"
                                placeholder="Order #, product, or status..."
                                bind:value={searchQuery}
                            />
                            {#if searchQuery}
                                <button
                                    type="button"
                                    class="clear-button"
                                    onclick={() => searchQuery = ''}
                                >
                                    Clear
                                </button>
                            {/if}
                        </div>
                    </div>

                    <div class="field sort-field">
                        <label for="order-sort">Sort By</label>
                        <select id="order-sort" bind:value={sortBy}>
                            <option value="newest">Date: Newest First</option>
                            <option value="oldest">Date: Oldest First</option>
                            <option value="highest">Cost: Highest First</option>
                            <option value="lowest">Cost: Lowest First</option>
                        </select>
                    </div>
                </div>

                {#if filteredOrders.length === 0}
                    <div class="empty-state">
                        <h3>No matching orders</h3>
                        <p>Try a different search.</p>
                        <button
                            class="primary-button"
                            onclick={() => searchQuery = ''}
                        >
                            Clear Search
                        </button>
                    </div>
                {:else}
                    <div class="orders-list">
                        {#each filteredOrders as order (order.orderId)}
                            <article class="order-card">
                                <div class="order-header">
                                    <div>
                                        <h3>Order #{order.orderId}</h3>
                                        <p>{formatDate(order.orderDate)}</p>
                                    </div>
                                    <span class="order-status">
                                        {order.status}
                                    </span>
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

                                            <strong class="item-total">
                                                ${formatMoney(
                                                    item.price_at_purchase *
                                                    item.quantity
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
                            </article>
                        {/each}
                    </div>
                {/if}

                <p class="results-count">
                    Showing {filteredOrders.length} of {orders.length}
                    {orders.length === 1 ? 'order' : 'orders'}
                </p>
            {/if}
        </section>
    {/if}
</main>

<style>
    .account-page {
        max-width: 1050px;
        margin: 0 auto;
        padding: 2.5rem 1.5rem 4rem;
        min-height: 75vh;
    }

    .account-banner {
        display: flex;
        align-items: center;
        gap: 1.2rem;
        padding: 1.5rem 2rem;
        background: var(--color-accent);
        border: 2px solid var(--color-border);
        border-radius: 14px;
        margin-bottom: 1.5rem;
    }

    .banner-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 65px;
        height: 65px;
        background: #fff5d6;
        border: 2px solid var(--color-border);
        border-radius: 50%;
        flex-shrink: 0;
    }

    h1 {
        margin: 0;
        font-size: 2rem;
    }

    .account-banner p {
        margin: 0.35rem 0 0;
    }

    .account-tabs {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 1rem;
        margin-bottom: 1.5rem;
    }

    .account-tabs button {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.7rem;
        padding: 1.15rem;
        background: #fff9e9;
        border: 2px solid var(--color-border);
        border-radius: 12px;
        color: var(--color-text);
        font: inherit;
        font-weight: 700;
        cursor: pointer;
        transition: background 0.15s ease;
    }

    .account-tabs button:hover,
    .account-tabs button.active {
        background: var(--color-accent);
    }

    .account-tabs button.active {
        box-shadow: 3px 3px 0 var(--color-border);
    }

    .account-panel {
        background: #fff9e9;
        border: 2px solid var(--color-border);
        border-radius: 14px;
        padding: 2rem;
    }

    .panel-heading {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        margin-bottom: 1.5rem;
    }

    .panel-heading h2 {
        margin: 0;
        font-size: 1.65rem;
    }

    .panel-heading p {
        margin: 0.35rem 0 0;
        color: #77655b;
    }

    form {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .field {
        display: flex;
        flex-direction: column;
        gap: 0.45rem;
        flex: 1;
        min-width: 0;
    }

    .field-row {
        display: flex;
        gap: 1rem;
    }

    label {
        font-weight: 700;
    }

    .field > input,
    .field > select {
        width: 100%;
        padding: 0.85rem;
        background: white;
        border: 2px solid var(--color-border);
        border-radius: 8px;
        color: var(--color-text);
        font: inherit;
    }

    .field > input:disabled {
        background: #f1f0eb;
        color: #555;
    }

    small {
        color: #77655b;
    }

    .actions {
        display: flex;
        gap: 1rem;
        margin-top: 0.75rem;
    }

    .primary-button,
    .secondary-button {
        display: inline-flex;
        justify-content: center;
        align-items: center;
        padding: 0.85rem 1.5rem;
        border: 2px solid var(--color-border);
        border-radius: 8px;
        color: var(--color-text);
        font: inherit;
        font-weight: 700;
        text-decoration: none;
        cursor: pointer;
    }

    .primary-button {
        background: var(--color-accent);
    }

    .secondary-button {
        background: white;
    }

    .primary-button:hover {
        background: var(--color-accent-hover);
    }

    button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .message {
        padding: 0.9rem;
        border-radius: 8px;
        margin-bottom: 1rem;
    }

    .error {
        background: #fee2e2;
        color: #991b1b;
    }

    .success {
        background: #dcfce7;
        color: #166534;
    }

    .order-controls {
        display: flex;
        align-items: flex-end;
        gap: 1rem;
        margin-bottom: 1.5rem;
    }

    .sort-field {
        flex: 0 0 240px;
    }

    .search-input {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.7rem;
        background: white;
        border: 2px solid var(--color-border);
        border-radius: 8px;
    }

    .search-input input {
        width: 100%;
        min-width: 0;
        flex: 1;
        border: none;
        outline: none;
        background: transparent;
        color: var(--color-text);
        font: inherit;
    }

    .clear-button {
        border: none;
        background: transparent;
        color: var(--color-text);
        cursor: pointer;
        font-weight: 700;
    }

    .orders-list {
        display: flex;
        flex-direction: column;
        gap: 1.25rem;
    }

    .order-card {
        padding: 1.5rem;
        background: white;
        border: 2px solid var(--color-border);
        border-radius: 12px;
    }

    .order-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 1rem;
        padding-bottom: 1rem;
        border-bottom: 1px solid #ddd;
    }

    .order-header h3 {
        margin: 0;
        font-size: 1.25rem;
    }

    .order-header p {
        margin: 0.4rem 0 0;
        color: #77655b;
    }

    .order-status {
        padding: 0.45rem 0.8rem;
        background: #ffedad;
        border: 1px solid var(--color-border);
        border-radius: 7px;
        font-weight: 700;
        font-size: 0.85rem;
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
        border-radius: 8px;
    }

    .item-details {
        flex: 1;
        min-width: 0;
    }

    .item-details p {
        margin: 0.3rem 0;
        color: #77655b;
        font-size: 0.9rem;
    }

    .item-total {
        white-space: nowrap;
    }

    .order-footer {
        display: flex;
        justify-content: flex-end;
        padding-top: 1.2rem;
        font-size: 1.1rem;
    }

    .results-count {
        text-align: center;
        margin-top: 1.5rem;
        color: #77655b;
        font-size: 0.9rem;
    }

    .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 0.75rem;
        padding: 3rem 1rem;
        text-align: center;
    }

    .empty-state h3,
    .empty-state p {
        margin: 0;
    }

    .empty-state .primary-button {
        margin-top: 0.75rem;
    }

    @media (max-width: 650px) {
        .account-page {
            padding: 1.5rem 1rem;
        }

        .account-banner {
            padding: 1rem;
        }

        .banner-icon {
            width: 50px;
            height: 50px;
        }

        h1 {
            font-size: 1.65rem;
        }

        .account-panel {
            padding: 1.2rem;
        }

        .account-tabs button {
            padding: 0.85rem 0.4rem;
            font-size: 0.9rem;
        }

        .field-row,
        .order-controls,
        .actions {
            flex-direction: column;
            align-items: stretch;
        }

        .sort-field {
            flex: 1;
        }

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
