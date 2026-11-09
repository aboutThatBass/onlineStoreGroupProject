<script lang="ts">
    import type { Product } from '../types/Product';
    import { cart } from '../stores/cart.svelte';
    import '../styles/products.css'

    let { product }: { product: Product } = $props();

    let adding = $state(false);

    async function handleAddToCart() {
        adding = true;

        try {
            await cart.add(product.id);
            console.log('Cart contends:', $state.snapshot(cart.items));
        } finally {
            adding = false;
        }
    }
</script>

<article class="product-card">
    <img src={product.imageUrl} alt={product.name} />

    <a href={`/product/${product.id}`} class="product-link">
        <h2>{product.name}</h2>
    </a>
    <p>{product.description}</p>
    <p>${product.price.toFixed(2)}</p>

    {#if product.quantity > 0}
        <p>In Stock: {product.quantity}</p>
    {:else}
        <p>Out of Stock</p>
    {/if}

    <button
        onclick={handleAddToCart}
        disabled={product.quantity === 0 || adding}
    >
        {adding ? 'Adding...' : 'Add to Cart'}
    </button>

    {#if cart.error}
        <p class="error">{cart.error}</p>
    {/if}
</article>

<style>
    .product-card:hover:not(:has(button:hover)){
        background-color: #f3cc58;
    }
</style>
