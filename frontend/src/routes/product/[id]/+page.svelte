<script lang="ts">
    import type { PageProps } from './$types';
    import '../../../lib/styles/products.css';
    import { cart } from '../../../lib/stores/cart.svelte';

    let { data }: PageProps = $props();
    let product = $derived(data.product);
    let cartStatus = $state<'idle' | 'adding' | 'updated' | 'error'>('idle');

    async function handleAddToCart() {
        cartStatus = 'adding';
        cartStatus = (await cart.add(product.id)) ? 'updated' : 'error';
    }
</script>

<svelte:head>
    <title>{product.name} | Viral Vault</title>
</svelte:head>

<main class="product-page">
    <a href="/">Back to Shop</a>

    <article class="product-detail product-card">
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

            <div class="cart-actions">
                <button
                    onclick={handleAddToCart}
                    disabled={product.quantity <= 0 || cartStatus === 'adding'}
                >
                    {cartStatus === 'adding' ? 'Adding...' : 'Add to Cart'}
                </button>
                <a href="/cart">View Cart</a>
            </div>

            <p role="status" class:error={cartStatus === 'error'}>
                {#if cartStatus === 'updated'}
                    Cart updated.
                {:else if cartStatus === 'error'}
                    {cart.error ?? 'Unable to update your cart.'}
                {/if}
            </p>
        </div>
    </article>
</main>

<style>
    .product-page {
        max-width: 1100px;
        margin: 0 auto;
        padding: clamp(1rem, 4vw, 2rem);
    }

    :global(.product-card).product-detail {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 2rem;
        margin-top: 1rem;
        padding: clamp(1rem, 4vw, 2rem);
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

    .cart-actions {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 1rem;
        margin-top: 0.25rem;
    }

    .error {
        color: #dc2626;
    }

    @media (max-width: 768px) {
        :global(.product-card).product-detail {
            grid-template-columns: 1fr;
        }
    }
</style>
