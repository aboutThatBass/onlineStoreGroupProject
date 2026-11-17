<script lang="ts">
    import { effectivePrice, isOnSale, type Product } from '../types/Product';
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
    <a href={`/product/${product.id}`} class="product-link">
        <img src={product.imageUrl} alt="" />
        <h2>{product.name}</h2>
    </a>
    <p>{product.description}</p>
        <p class="product-price">
        {#if isOnSale(product)}
            <del class="original-price">
                ${product.price.toFixed(2)}
            </del>

            <strong class="sale-price">
                ${effectivePrice(product).toFixed(2)}
            </strong>

            <span class="sale-badge">
                {Math.round(
                    (1 - effectivePrice(product) / product.price) * 100
                )}% OFF
            </span>
        {:else}
            ${product.price.toFixed(2)}
        {/if}
    </p>

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
    .product-price {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 0.5rem;
    }

    .original-price {
        color: #777;
        font-size: 0.9rem;
    }

    .sale-price {
        color: #a52626;
        font-size: 1.2rem;
    }

    .sale-badge {
        background: #a52626;
        color: white;
        padding: 0.25rem 0.5rem;
        border-radius: 6px;
        font-size: 0.75rem;
        font-weight: 700;
    }

    .product-link {
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
    }

    .product-card:hover:not(:has(button:hover)){
        background-color: #f3cc58;
    }
</style>
