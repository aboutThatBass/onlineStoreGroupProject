
<script lang="ts">
    import ProductAdmin from '../../lib/components/admin/ProductAdmin.svelte';
    import DiscountAdmin from '../../lib/components/admin/DiscountAdmin.svelte';
    import SaleAdmin from '../../lib/components/admin/SaleAdmin.svelte';
    import UserAdmin from '../../lib/components/admin/UserAdmin.svelte';
    import OrderAdmin from '../../lib/components/admin/OrderAdmin.svelte';

    let activeTab = $state('products');
</script>

<div class="admin-page">
    <h1>Viral Vault Administration</h1>

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
  nav {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin: 16px 0;
  }

  button {
    padding: 8px 12px;
    cursor: pointer;
  }
</style>
