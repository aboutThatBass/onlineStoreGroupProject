<script lang="ts">
    import type { Product } from '../types/Product';
    import '../styles/products.css'

    let { product }: { product: Product } = $props();
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

    <button disabled={product.quantity === 0}>
        Add to Cart
    </button>
</article>

<style>
    .product-card:hover:not(:has(button:hover)){
        background-color: #f3cc58;
    }
</style>
