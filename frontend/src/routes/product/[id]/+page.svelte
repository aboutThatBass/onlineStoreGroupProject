
<script lang="ts">
    import { onMount } from 'svelte';
    import { page } from '$app/state';
    import type { Product } from '../../../lib/types/Product';
    import '../../../lib/styles/products.css';
    import { cart } from '../../../lib/stores/cart.svelte';

    let product = $state<Product | null>(null);
    let loading = $state(true);
    let error = $state('');
    let adding = $state(false);

    async function loadProduct(id: string) {
        loading = true;
        error = '';
        product = null;

        try {
            const response = await fetch(
                `http://localhost:8080/api/products/${id}`
            );

            if (response.status === 404) {
                error = 'Product not found';
                return;
            }

            if (!response.ok) {
                throw new Error('Unable to load product');
            }

            product = await response.json();
        } catch {
            error = 'Unable to load product';
        } finally {
            loading = false;
        }
    }

    onMount(() => {
        if (page.params.id) {
            loadProduct(page.params.id);
        } else {
            loading = false;
            error = 'Product not found';
        }
    });

    async function handleAddToCart() {
        if (!product || adding) return;

        adding = true;

        try {
            await cart.add(product.id);
        } finally {
            adding = false;
        }
    }
</script>

{#if loading}
    <p>Loading product...</p>
{:else if product}
    <main class="product-detail product-card">
        <div class="product-image">
            <img src={product.imageUrl} alt={product.name} />
        </div>

        <div class="product-info">
            <h1>{product.name}</h1>
            <p class="description">{product.description}</p>

            <div class="purchase-info">
                <p class="price">${product.price.toFixed(2)}</p>

                {#if product.quantity > 0}
                    <p>In Stock: {product.quantity}</p>
                {:else}
                    <p>Out of Stock</p>
                {/if}
            </div>

            <button
                onclick={handleAddToCart}
                disabled={product.quantity === 0 || adding}
            >
                {adding ? 'Adding...' : 'Add to Cart'}
            </button>

            {#if cart.error}
                <p class="error" role="alert">{cart.error}</p>
            {/if}
        </div>
    </main>
{:else}
    <h1>{error || 'Product not found'}</h1>
{/if}

<style>
    :global(.product-card).product-detail {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 2rem;

        max-width: 1100px;
        margin: 2rem auto;
        padding: 2rem;
    }

    .product-detail .product-image img {
        width: 100%;
        height: auto;
        max-height: 600px;
        object-fit: contain;
    }

    .product-info {
        display: flex;
        flex-direction: column;
        gap: 0;
    }

    .product-info h1 {
        font-size: 40px;
    }

    .product-info .description {
        margin-top: 1rem;
        font-size: larger;
    }

    .purchase-info {
        margin-top: auto;
        margin-bottom: 0.25rem;
        display: flex;
        justify-content: space-between;
        gap: 1rem;
        font-size: large;
    }

    .product-info button {
        margin-top: .2rem;
    }

    .error {
        color: #dc2626;
    }

    @media (max-width: 768px) {
        :global(.product-card).product-detail {
            grid-template-columns: 1fr;
            margin: 1rem;
            padding: 1.25rem;
        }
    }
</style>
