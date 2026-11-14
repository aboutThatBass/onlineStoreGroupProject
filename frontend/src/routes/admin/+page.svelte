
<script lang="ts">
    import ProductAdmin from '../../lib/components/admin/ProductAdmin.svelte';
    import DiscountAdmin from '../../lib/components/admin/DiscountAdmin.svelte';
    import SaleAdmin from '../../lib/components/admin/SaleAdmin.svelte';
    import UserAdmin from '../../lib/components/admin/UserAdmin.svelte';
    import OrderAdmin from '../../lib/components/admin/OrderAdmin.svelte';

    let activeTab = $state('products');
</script>

<div class="admin-page">

    <div class="admin-header">
        <h1>Viral Vault Administration</h1>

        <a href="/" class="back-to-shop">
            ← Back to Shop
        </a>
    </div>

    <nav>
        <button onclick={() => activeTab = 'products'}>
            Products
        </button>

        <button onclick={() => activeTab = 'discounts'}>
            Discounts
        </button>

        <button onclick={() => activeTab = 'sales'}>
            Sales
        </button>

        <button onclick={() => activeTab = 'users'}>
            Users
        </button>

        <button onclick={() => activeTab = 'orders'}>
            Orders
        </button>
    </nav>

    <hr />

    {#if activeTab === 'products'}
        <ProductAdmin />
    {:else if activeTab === 'discounts'}
        <DiscountAdmin />
    {:else if activeTab === 'sales'}
        <SaleAdmin />
    {:else if activeTab === 'users'}
        <UserAdmin />
    {:else if activeTab === 'orders'}
        <OrderAdmin />
    {/if}

</div>

<style>
    :global(body:has(.admin-page)) {
        background: #fff;
        color: #222;
    }

    .admin-page {
        min-height: 100vh;
        padding: 24px;
        background: #fff;
        color: #222;
        font-family: Arial, sans-serif;
    }

    .admin-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        flex-wrap: wrap;
        gap: 16px;
        margin-bottom: 20px;
    }

    .admin-header h1 {
        margin: 0;
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 2.2rem;
        color: #222;
    }

    .back-to-shop {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 10px 16px;
        background: #fff;
        color: #222;
        border: 1px solid #888;
        border-radius: 4px;
        font-family: Arial, sans-serif;
        font-size: 16px;
        font-weight: 500;
        text-decoration: none;
        white-space: nowrap;
        cursor: pointer;
        transition: background 0.2s ease, border-color 0.2s ease;
    }

    .back-to-shop:hover {
        background: #f0f0f0;
        border-color: #555;
    }

    .back-to-shop:focus-visible {
        outline: 2px solid #555;
        outline-offset: 3px;
    }

    nav {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin: 16px 0;
    }

    nav button {
        padding: 8px 12px;
        cursor: pointer;
    }

    @media (max-width: 600px) {
        .admin-header {
            align-items: flex-start;
            flex-direction: column;
        }

        .admin-header h1 {
            font-size: 1.8rem;
        }
    }
</style>
