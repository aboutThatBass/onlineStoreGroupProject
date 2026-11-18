
<script lang="ts">
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';

    import ProductAdmin from '../../lib/components/admin/ProductAdmin.svelte';
    import DiscountAdmin from '../../lib/components/admin/DiscountAdmin.svelte';
    import SaleAdmin from '../../lib/components/admin/SaleAdmin.svelte';
    import UserAdmin from '../../lib/components/admin/UserAdmin.svelte';
    import OrderAdmin from '../../lib/components/admin/OrderAdmin.svelte';

    let activeTab = $state('products');

    let checkingSession = $state(true);
    let authorized = $state(false);
    let sessionError = $state('');

    onMount(async () => {
        try {
            const response = await fetch(
                '/api/admin/session',
                {
                    credentials: 'include'
                }
            );

            if (response.status === 401 ||
                response.status === 403) {

                await goto('/admin/login', {
                    replaceState: true
                });

                return;
            }

            if (!response.ok) {
                throw new Error(
                    'Unable to verify administrator session.'
                );
            }

            const session = await response.json();

            if (!session.authenticated || !session.isAdmin) {
                await goto('/admin/login', {
                    replaceState: true
                });

                return;
            }

            authorized = true;

        } catch (err) {
            console.error(err);

            sessionError =
                'Unable to verify your administrator session. ' +
                'Make sure the backend is running.';

        } finally {
            checkingSession = false;
        }
    });
</script>

{#if checkingSession}

    <div class="admin-page">
        <p>Verifying administrator session...</p>
    </div>

{:else if sessionError}

    <div class="admin-page">
        <h1>Viral Vault Administration</h1>

        <p>{sessionError}</p>

        <a href="/admin/login" class="back-to-shop">
            Return to Admin Login
        </a>
    </div>

{:else if authorized}

    <div class="admin-page">

        <div class="admin-header">
            <h1>Viral Vault Administration</h1>

            <a href="/" class="back-to-shop">
                &larr; Back to Shop
            </a>
        </div>

        <nav class="admin-tabs"
             aria-label="Administration sections">

            <button
                class:active={activeTab === 'products'}
                aria-pressed={activeTab === 'products'}
                onclick={() => activeTab = 'products'}
            >
                Products
            </button>

            <button
                class:active={activeTab === 'discounts'}
                aria-pressed={activeTab === 'discounts'}
                onclick={() => activeTab = 'discounts'}
            >
                Discounts
            </button>

            <button
                class:active={activeTab === 'sales'}
                aria-pressed={activeTab === 'sales'}
                onclick={() => activeTab = 'sales'}
            >
                Sales
            </button>

            <button
                class:active={activeTab === 'users'}
                aria-pressed={activeTab === 'users'}
                onclick={() => activeTab = 'users'}
            >
                Users
            </button>

            <button
                class:active={activeTab === 'orders'}
                aria-pressed={activeTab === 'orders'}
                onclick={() => activeTab = 'orders'}
            >
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

{/if}

<style>
    .admin-page {
        padding: 24px;
        min-height: 100vh;
        background-color: white;
        color: #202020;
    }

    .admin-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        margin-bottom: 20px;
    }

    .admin-header h1 {
        margin: 0;
    }

    .back-to-shop {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 12px 16px;
        background-color: white;
        color: #202020;
        border: 1px solid #999;
        border-radius: 4px;
        text-decoration: none;
        white-space: nowrap;
        transition: background-color 0.15s ease;
    }

    .back-to-shop:hover {
        background-color: #f1f1f1;
    }

    .admin-tabs {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 16px;
    }

    .admin-tabs button {
        padding: 10px 14px;
        background-color: #eee;
        color: #202020;
        border: 1px solid #888;
        border-radius: 2px;
        font: inherit;
        cursor: pointer;
        transition:
            background-color 0.15s ease,
            color 0.15s ease;
    }

    .admin-tabs button:hover {
        background-color: #ddd;
    }

    .admin-tabs button.active {
        background-color: #353535;
        color: white;
        border-color: #353535;
    }

    .admin-tabs button.active:hover {
        background-color: #454545;
    }

    hr {
        border: none;
        border-top: 1px solid #aaa;
        margin: 16px 0 20px;
    }

    @media (max-width: 600px) {
        .admin-page {
            padding: 16px;
        }

        .admin-header {
            flex-wrap: wrap;
        }
    }
</style>
