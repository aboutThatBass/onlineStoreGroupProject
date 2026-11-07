
<script lang="ts">
    import { onMount } from 'svelte';
    import ProductCard from '../../lib/components/ProductCard.svelte';
    import type { Product } from '../../lib/types/Product';

    let products: Product[] = $state([]);
    let searchQuery = $state('');
    let sortOption = $state('default');
    let loading = $state(true);
    let loadError = $state('');

    onMount(async () => {
        try {
            const response = await fetch('http://localhost:8080/api/products');

            if (!response.ok) {
                throw new Error('Failed to load products');
            }

            products = await response.json();
        } catch (error) {
            console.error('Error loading products:', error);
            loadError = 'Unable to load products. Please try again later.';
        } finally {
            loading = false;
        }
    });

    let filteredProducts = $derived.by(() => {
        const query = searchQuery.trim().toLowerCase();

        const results = products.filter((product) => {
            return (
                product.name.toLowerCase().includes(query) ||
                (product.description ?? '').toLowerCase().includes(query)
            );
        });

        switch (sortOption) {
            case 'price-low':
                return results.sort((a, b) => a.price - b.price);

            case 'price-high':
                return results.sort((a, b) => b.price - a.price);

            case 'available':
                return results.sort((a, b) => {
                    const aAvailable = a.quantity > 0 ? 1 : 0;
                    const bAvailable = b.quantity > 0 ? 1 : 0;

                    return bAvailable - aAvailable;
                });

            default:
                return results;
        }
    });
</script>

<svelte:head>
    <title>Shop | Viral Vault</title>
</svelte:head>

<main>
    <h1>Shop Viral Vault</h1>
    <p>Find your next favorite collectible!</p>

    <section class="shop-controls">
        <div class="control">
            <label for="product-search">Search Products</label>
            <input
                id="product-search"
                type="search"
                placeholder="Search by name or description..."
                bind:value={searchQuery}
            />
        </div>

        <div class="control">
            <label for="product-sort">Sort Products</label>
            <select id="product-sort" bind:value={sortOption}>
                <option value="default">Default Order</option>
                <option value="price-low">Price: Low to High</option>
                <option value="price-high">Price: High to Low</option>
                <option value="available">Availability: In Stock First</option>
            </select>
        </div>
    </section>

    <p class="results-count">
        Showing {filteredProducts.length} of {products.length} products
    </p>

    {#if loading}
        <p>Loading products...</p>
    {:else if loadError}
        <p>{loadError}</p>
    {:else if filteredProducts.length > 0}
        <section class="product-grid">
            {#each filteredProducts as product (product.id)}
                <ProductCard {product} />
            {/each}
        </section>
    {:else}
        <div class="no-results">
            <h2>No products found</h2>
            <p>Try searching for a different product or description.</p>
        </div>
    {/if}
</main>

<style>
    main {
        max-width: 1200px;
        margin: 0 auto;
        padding: 2rem;
    }

    .shop-controls {
        display: flex;
        flex-wrap: wrap;
        gap: 1.5rem;
        margin-top: 2rem;
    }

    .control {
        display: flex;
        flex: 1;
        flex-direction: column;
        gap: 0.5rem;
        min-width: 240px;
    }

    .control label {
        font-weight: 600;
    }

    .control input,
    .control select {
        width: 100%;
        padding: 0.75rem;
        border: 1px solid #ccc;
        border-radius: 8px;
        background: white;
        color: #333;
        font: inherit;
    }

    .control input:focus,
    .control select:focus {
        outline: 2px solid #e78a9c;
        outline-offset: 2px;
    }

    .results-count {
        margin-top: 1.5rem;
        color: #555;
    }

    .product-grid {
        display: grid;
        grid-template-columns: repeat(
            auto-fit,
            minmax(min(250px, 100%), 1fr)
        );
        gap: 1.5rem;
        margin-top: 1rem;
    }

    .no-results {
        margin-top: 2rem;
        padding: 3rem 1rem;
        text-align: center;
        border: 1px dashed #ccc;
        border-radius: 12px;
    }
</style>
