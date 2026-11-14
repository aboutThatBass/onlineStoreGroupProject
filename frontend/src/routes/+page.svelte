
<script lang="ts">
    import { onMount } from 'svelte';
    import {
        Sparkles,
        SlidersHorizontal,
        ChevronDown,
        ChevronUp,
        RotateCcw
    } from '@lucide/svelte';
    import ProductCard from '../lib/components/ProductCard.svelte';
    import type { Product } from '../lib/types/Product';

    let products: Product[] = $state([]);
    let searchQuery = $state('');
    let sortOption = $state('default');
    let availabilityFilter = $state('all');
    let maximumPrice = $state(100);
    let filtersOpen = $state(false);
    let loading = $state(true);
    let error = $state('');

    // Set the price slider's upper limit based on product prices.
    let priceLimit = $derived(
        Math.max(
            100,
            ...products.map(product => Math.ceil(product.price))
        )
    );

    let activeFilterCount = $derived(
        (sortOption !== 'default' ? 1 : 0) +
        (availabilityFilter !== 'all' ? 1 : 0) +
        (maximumPrice < priceLimit ? 1 : 0)
    );

    onMount(() => {
        const handleSearch = (event: Event) => {
            const searchEvent = event as CustomEvent<string>;
            searchQuery = searchEvent.detail ?? '';
        };

        searchQuery =
            new URLSearchParams(window.location.search)
                .get('search') ?? '';

        window.addEventListener(
            'viral-vault-search',
            handleSearch
        );

        async function loadProducts() {
            try {
                const response = await fetch(
                    'http://localhost:8080/api/products'
                );

                if (!response.ok) {
                    throw new Error('Unable to load products');
                }

                products = await response.json();

                maximumPrice = Math.max(
                    100,
                    ...products.map(product => Math.ceil(product.price))
                );
            } catch (err) {
                console.error('Error loading products:', err);
                error = 'Unable to load products.';
            } finally {
                loading = false;
            }
        }

        void loadProducts();

        return () => {
            window.removeEventListener(
                'viral-vault-search',
                handleSearch
            );
        };
    });

    function resetFilters() {
        sortOption = 'default';
        availabilityFilter = 'all';
        maximumPrice = priceLimit;
    }

    let filteredProducts = $derived.by(() => {
        const query = searchQuery.trim().toLowerCase();

        const results = products.filter(product => {
            const matchesSearch =
                product.name.toLowerCase().includes(query) ||
                (product.description ?? '')
                    .toLowerCase()
                    .includes(query);

            const matchesAvailability =
                availabilityFilter === 'all' ||
                (availabilityFilter === 'in' &&
                    product.quantity > 0) ||
                (availabilityFilter === 'out' &&
                    product.quantity <= 0);

            const matchesPrice =
                product.price <= maximumPrice;

            return (
                matchesSearch &&
                matchesAvailability &&
                matchesPrice
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
    <!-- Featured welcome section -->
    <section class="welcome-card" aria-labelledby="welcome-title">
        <div class="welcome-content">
            <div class="welcome-label">
                <Sparkles size={16} strokeWidth={2.5} />
                <span>THE LATEST FINDS</span>
            </div>

            <h1 id="welcome-title">
                What's Trending in the Vault?
            </h1>

            <p class="welcome-description">
                Explore the latest collectible crazes and fan favorites.
            </p>

            <div class="collectible-tags" aria-label="Collection themes">
                <span class="collectible-tag yellow-tag">
                    Collectible Favorites
                </span>

                <span class="collectible-tag pink-tag">
                    Viral Finds
                </span>

                <span class="collectible-tag yellow-tag">
                    Cute &amp; Quirky
                </span>
            </div>
        </div>

        <div class="welcome-decoration" aria-hidden="true">
            <Sparkles size={42} strokeWidth={1.8} />
        </div>
    </section>

    <!-- Collection -->
    <section class="collection-section" id="products">
        <div class="collection-heading">
            <div>
                <h2>Explore the Collection</h2>
                <p>Find something you'll love.</p>
            </div>

            <button
                type="button"
                class="filter-toggle"
                class:filter-active={filtersOpen || activeFilterCount > 0}
                aria-expanded={filtersOpen}
                aria-controls="product-filter-panel"
                onclick={() => filtersOpen = !filtersOpen}
            >
                <SlidersHorizontal size={22} strokeWidth={2.2} />
                <span>Filters</span>

                {#if activeFilterCount > 0}
                    <span class="filter-count">
                        {activeFilterCount}
                    </span>
                {/if}

                {#if filtersOpen}
                    <ChevronUp size={18} />
                {:else}
                    <ChevronDown size={18} />
                {/if}
            </button>
        </div>

        <!-- Collapsible filters -->
        {#if filtersOpen}
            <div
                id="product-filter-panel"
                class="filter-panel"
            >
                <div class="filter-panel-header">
                    <h3>Filter &amp; Sort</h3>

                    <button
                        type="button"
                        class="reset-button"
                        onclick={resetFilters}
                    >
                        <RotateCcw size={16} />
                        Reset All
                    </button>
                </div>

                <div class="filter-grid">
                    <!-- Sorting -->
                    <div class="filter-group">
                        <label for="product-sort">
                            Sort Products
                        </label>

                        <select
                            id="product-sort"
                            bind:value={sortOption}
                        >
                            <option value="default">
                                Default Order
                            </option>

                            <option value="price-low">
                                Price: Low to High
                            </option>

                            <option value="price-high">
                                Price: High to Low
                            </option>

                            <option value="available">
                                Availability: In Stock First
                            </option>
                        </select>
                    </div>

                    <!-- Availability -->
                    <fieldset class="filter-group availability-group">
                        <legend>Availability</legend>

                        <label class="radio-option">
                            <input
                                type="radio"
                                name="availability"
                                value="all"
                                bind:group={availabilityFilter}
                            />
                            All Products
                        </label>

                        <label class="radio-option">
                            <input
                                type="radio"
                                name="availability"
                                value="in"
                                bind:group={availabilityFilter}
                            />
                            In Stock Only
                        </label>

                        <label class="radio-option">
                            <input
                                type="radio"
                                name="availability"
                                value="out"
                                bind:group={availabilityFilter}
                            />
                            Out of Stock Only
                        </label>
                    </fieldset>

                    <!-- Price -->
                    <div class="filter-group price-group">
                        <div class="price-heading">
                            <label for="maximum-price">
                                Maximum Price
                            </label>

                            <span class="price-value">
                                ${maximumPrice.toFixed(0)}
                            </span>
                        </div>

                        <input
                            id="maximum-price"
                            type="range"
                            min="0"
                            max={priceLimit}
                            step="1"
                            bind:value={maximumPrice}
                        />

                        <div class="price-range-labels">
                            <span>$0</span>
                            <span>${priceLimit}</span>
                        </div>
                    </div>
                </div>

                <div class="filter-footer">
                    <span class="filter-results">
                        {filteredProducts.length}
                        {filteredProducts.length === 1
                            ? ' product matches'
                            : ' products match'}
                    </span>

                    <button
                        type="button"
                        class="done-button"
                        onclick={() => filtersOpen = false}
                    >
                        Done
                    </button>
                </div>
            </div>
        {/if}

        <!-- Products -->
        {#if loading}
            <p class="status-message">
                Loading products...
            </p>
        {:else if error}
            <p class="status-message error-message">
                {error}
            </p>
        {:else}
            {#if filteredProducts.length > 0}
                <div class="product-grid" aria-label="Products">
                    {#each filteredProducts as product (product.id)}
                        <ProductCard {product} />
                    {/each}
                </div>
            {:else}
                <div class="no-results">
                    <h3>No products found</h3>
                    <p>
                        Try adjusting your search or filters.
                    </p>

                    <button
                        type="button"
                        class="reset-button reset-empty"
                        onclick={resetFilters}
                    >
                        <RotateCcw size={16} />
                        Reset Filters
                    </button>
                </div>
            {/if}

            <p class="results-count">
                Showing {filteredProducts.length} of {products.length}
                {products.length === 1 ? 'product' : 'products'}
            </p>
        {/if}
    </section>
</main>

<style>
    main {
        max-width: 1200px;
        margin: 0 auto;
        padding: 2rem;
    }

    /* Welcome card */

    .welcome-card {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1.5rem;
        overflow: hidden;
        margin-bottom: 2.25rem;
        padding: 2rem 2.25rem;
        background: #fff9e9;
        border: 2px solid var(--color-border);
        border-radius: 16px;
        box-shadow: 4px 4px 0 var(--color-border);
    }

    .welcome-content {
        position: relative;
        z-index: 1;
        min-width: 0;
    }

    .welcome-label {
        display: inline-flex;
        align-items: center;
        gap: 0.45rem;
        padding: 0.4rem 0.85rem;
        background: #e8a598;
        border: 1.5px solid var(--color-border);
        border-radius: 999px;
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 0.78rem;
        font-weight: 600;
        letter-spacing: 0.045em;
    }

    .welcome-card h1 {
        margin: 1rem 0 0.65rem;
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: clamp(1.9rem, 3.4vw, 2.8rem);
        font-weight: 600;
        line-height: 1.15;
        letter-spacing: -0.02em;
    }

    .welcome-description {
        margin: 0;
        color: #655047;
        font-size: 1.08rem;
        line-height: 1.6;
    }

    .collectible-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 0.6rem;
        margin-top: 1.5rem;
    }

    .collectible-tag {
        display: inline-flex;
        align-items: center;
        padding: 0.42rem 0.85rem;
        border: 1.5px solid var(--color-border);
        border-radius: 999px;
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 0.85rem;
        font-weight: 500;
        white-space: nowrap;
    }

    .yellow-tag {
        background: #ffdf78;
    }

    .pink-tag {
        background: #f6d3c9;
    }

    .welcome-decoration {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 110px;
        height: 110px;
        flex-shrink: 0;
        border: 2px dashed #d9ad8c;
        border-radius: 50%;
        background: #ffedad;
        color: #a86d53;
        transform: rotate(-12deg);
    }

    /* Collection heading */

    .collection-section {
        scroll-margin-top: 1.5rem;
    }

    .collection-heading {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        margin-bottom: 1.25rem;
    }

    .collection-heading h2 {
        margin: 0;
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: clamp(1.5rem, 2.4vw, 2rem);
        font-weight: 600;
    }

    .collection-heading p {
        margin: 0.35rem 0 0;
        color: #655047;
        font-size: 0.98rem;
    }

    /* Filter toggle */

    .filter-toggle {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.6rem;
        flex-shrink: 0;
        padding: 0.7rem 1rem;
        background: #fffdf7;
        border: 2px solid var(--color-border);
        border-radius: 10px;
        box-shadow: 3px 3px 0 var(--color-border);
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
        transition:
            background-color 0.2s ease,
            transform 0.2s ease;
    }

    .filter-toggle:hover {
        background: #fff0e8;
        transform: translateY(-2px);
    }

    .filter-toggle.filter-active {
        background: #f6d3c9;
    }

    .filter-toggle:focus-visible,
    .reset-button:focus-visible,
    .done-button:focus-visible {
        outline: 3px solid #40342e;
        outline-offset: 3px;
    }

    .filter-count {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-width: 23px;
        height: 23px;
        padding: 0 0.3rem;
        border-radius: 50%;
        background: #ffdf78;
        border: 1px solid var(--color-border);
        font-size: 0.8rem;
    }

    /* Filter panel */

    .filter-panel {
        margin-bottom: 1.5rem;
        padding: 1.5rem;
        background: #fff9e9;
        border: 2px solid var(--color-border);
        border-radius: 14px;
        box-shadow: 4px 4px 0 var(--color-border);
    }

    .filter-panel-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        margin-bottom: 1.5rem;
    }

    .filter-panel-header h3 {
        margin: 0;
        font-size: 1.35rem;
    }

    .reset-button {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.4rem;
        padding: 0.5rem 0.8rem;
        background: transparent;
        border: 1.5px solid var(--color-border);
        border-radius: 8px;
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 0.9rem;
        font-weight: 600;
        cursor: pointer;
    }

    .reset-button:hover {
        background: #f6d3c9;
    }

    .filter-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 1.75rem;
    }

    .filter-group {
        display: flex;
        flex-direction: column;
        gap: 0.7rem;
        min-width: 0;
    }

    .filter-group > label,
    .availability-group legend,
    .price-heading label {
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-weight: 600;
    }

    .filter-group select {
        width: 100%;
        padding: 0.75rem;
        border: 2px solid var(--color-border);
        border-radius: 8px;
        background: white;
        color: #333;
        font: inherit;
        cursor: pointer;
    }

    .filter-group select:focus-visible {
        outline: 2px solid #e78a9c;
        outline-offset: 2px;
    }

    .availability-group {
        margin: 0;
        padding: 0;
        border: none;
    }

    .availability-group legend {
        margin-bottom: 0.7rem;
        padding: 0;
    }

    .radio-option {
        display: flex;
        align-items: center;
        gap: 0.6rem;
        font-family: var(--font-body, 'Nunito', sans-serif);
        font-size: 0.95rem;
        cursor: pointer;
    }

    .radio-option input {
        width: 17px;
        height: 17px;
        margin: 0;
        accent-color: #d88f83;
        cursor: pointer;
    }

    .price-heading {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 0.5rem;
    }

    .price-value {
        padding: 0.3rem 0.6rem;
        background: #ffdf78;
        border: 1px solid var(--color-border);
        border-radius: 7px;
        font-weight: 700;
    }

    .price-group input[type='range'] {
        width: 100%;
        margin: 0.5rem 0 0;
        accent-color: #d88f83;
        cursor: pointer;
    }

    .price-range-labels {
        display: flex;
        justify-content: space-between;
        color: #655047;
        font-size: 0.85rem;
    }

    .filter-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        margin-top: 1.5rem;
        padding-top: 1rem;
        border-top: 1px solid #d9c5b4;
    }

    .filter-results {
        color: #655047;
        font-size: 0.95rem;
    }

    .done-button {
        min-width: 110px;
        padding: 0.65rem 1.2rem;
        background: #e8a598;
        border: 2px solid var(--color-border);
        border-radius: 9px;
        box-shadow: 2px 2px 0 var(--color-border);
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
    }

    .done-button:hover {
        background: #d88f83;
    }

    /* Product grid */

    .product-grid {
        display: grid;
        grid-template-columns: repeat(
            auto-fit,
            minmax(min(250px, 100%), 1fr)
        );
        gap: 1.5rem;
        margin-top: 1.5rem;
    }

    .results-count {
        margin-top: 2rem;
        margin-bottom: 1rem;
        color: #777;
        font-size: 0.9rem;
        text-align: center;
    }

    .status-message {
        margin-top: 2rem;
    }

    .error-message {
        color: #b91c1c;
    }

    .no-results {
        margin-top: 2rem;
        padding: 3rem 1rem;
        text-align: center;
        border: 1px dashed #ccc;
        border-radius: 12px;
    }

    .no-results h3 {
        margin-top: 0;
    }

    .reset-empty {
        margin-top: 0.75rem;
    }

    /* Responsive layout */

    @media (max-width: 800px) {
        .filter-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
        }

        .price-group {
            grid-column: 1 / -1;
        }
    }

    @media (max-width: 700px) {
        .welcome-card {
            padding: 1.5rem;
        }

        .welcome-decoration {
            display: none;
        }

        .welcome-card h1 {
            font-size: clamp(1.75rem, 5vw, 2.3rem);
        }
    }

    @media (max-width: 600px) {
        main {
            padding: 1.25rem;
        }

        .welcome-card {
            padding: 1.35rem;
            margin-bottom: 1.75rem;
        }

        .welcome-description {
            font-size: 1rem;
        }

        .collectible-tags {
            gap: 0.45rem;
            margin-top: 1.2rem;
        }

        .collectible-tag {
            font-size: 0.75rem;
            padding: 0.35rem 0.65rem;
        }

        .collection-heading {
            align-items: flex-start;
        }

        .collection-heading h2 {
            font-size: 1.45rem;
        }

        .filter-toggle {
            padding: 0.65rem;
            gap: 0.35rem;
            font-size: 0.85rem;
        }

        .filter-panel {
            padding: 1rem;
        }

        .filter-grid {
            grid-template-columns: 1fr;
            gap: 1.5rem;
        }

        .price-group {
            grid-column: auto;
        }
    }
</style>
