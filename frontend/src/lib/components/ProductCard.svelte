
<script lang="ts">
    import type { Product } from '../types/Product';

    let { product }: { product: Product } = $props();
</script>

<article class="product-card">
    <img src={product.imageUrl} alt={product.name} />

    <h2>{product.name}</h2>
    <p>{product.description}</p>
    <p>${product.price.toFixed(2)}</p>

    {#if product.quantity > 0}
        <p>In Stock: {product.quantity}</p>
    {:else}
        <p>Out of Stock</p>
    {/if}

    <button disabled={product.quantity === 0}>
        Add to Cart
    </button>
</article>


<style>
    .product-card {
        background-color: #f9d976;
        border: 2px solid #40342e;
        border-radius: 12px;
        padding: 1.25rem;
        box-shadow: 4px 4px 0 #40342e;

        display: flex;
        flex-direction: column;
        gap: 0.75rem;
    }

    .product-card img {
        width: 100%;
        height: 200px;
        object-fit: contain;
    }

    .product-card h2 {
        font-size: 1.25rem;
        margin: 0;
    }

    .product-card p {
        margin: 0;
    }

    button {
        background-color: #e8a598;
        border: 2px solid #40342e;
        border-radius: 8px;
        padding: 0.75rem;
        font-weight: bold;
        cursor: pointer;
        margin-top: auto;
    }

    button:hover:not(:disabled) {
        background-color: #d88f83;
    }

    button:disabled {
        background-color: #d4d0ca;
        cursor: not-allowed;
    }
</style>
