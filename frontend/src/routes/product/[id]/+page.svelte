<script lang="ts">
    import { page } from '$app/state';
    import { products } from '../../../lib/data/products';
    import '../../../lib/styles/products.css';

    let product = $derived(
        products.find(p => p.id === Number(page.params.id))
    );
</script>

{#if product}
    <main class="product-detail product-card">
        <div class="product-image">
            <img src={product.imageUrl} alt={product.name} />
        </div>

        <div class="product-info">
            <h1>{product.name}</h1>
            <p class="description">{product.description}</p>

            <div class='purchase-info'>
                <p class="price">${product.price.toFixed(2)}</p>

                {#if product.quantity > 0}
                    <p>In Stock: {product.quantity}</p>
                {:else}
                    <p>Out of Stock</p>
                {/if}
            </div>
            <button disabled={product.quantity === 0}>
                Add to Cart
            </button>
        </div>
    </main>
{:else}
    <h1>Product not found</h1>
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

    .product-info h1{
        font-size: 40px;
    }

    .product-info .description{
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

    @media (max-width: 768px) {
    :global(.product-card).product-detail {
        grid-template-columns: 1fr;
        margin: 1rem;
        padding: 1.25rem;
    }
}
</style>