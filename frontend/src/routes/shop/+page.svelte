
<script lang="ts">
    import ProductCard from '../../lib/components/ProductCard.svelte';
    import { products } from '../../lib/data/products';
</script>

<svelte:head>
    <title>Shop | Viral Vault</title>
</svelte:head>

<main>
    <h1>Shop Viral Vault</h1>
    <p>Find your next favorite collectible!</p>

    <section class="product-grid">
        {#each products as product (product.id)}
            <ProductCard {product} />
        {/each}
    </section>
</main>



<style>
    main {
        max-width: 1200px;
        margin: 0 auto;
        padding: 2rem;
    }

    .product-grid {
        display: grid;
        grid-template-columns: repeat(
            auto-fit,
            minmax(min(250px, 100%), 1fr)
        );
        gap: 1.5rem;
        margin-top: 2rem;
    }
</style>
