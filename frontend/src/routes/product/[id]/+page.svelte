
<script lang="ts">
    import { onMount } from 'svelte';
    import type { PageProps } from './$types';
    import type { Product } from '../../../lib/types/Product';
    import {
        ArrowLeft,
        ShoppingCart,
        CheckCircle2,
        XCircle
    } from '@lucide/svelte';
    import '../../../lib/styles/products.css';
    import { cart } from '../../../lib/stores/cart.svelte';

    let { data }: PageProps = $props();

    let product = $derived(data.product);

    let cartStatus = $state<
        'idle' | 'adding' | 'updated' | 'error'
    >('idle');

    let allProducts = $state<Product[]>([]);
    let suggestionsLoading = $state(true);

    // Exclude the current product and prioritize available items.
    let suggestedProducts = $derived.by(() => {
        return allProducts
            .filter((item) => item.id !== product.id)
            .sort((a, b) => {
                const aAvailable = a.quantity > 0 ? 1 : 0;
                const bAvailable = b.quantity > 0 ? 1 : 0;

                return bAvailable - aAvailable;
            })
            .slice(0, 3);
    });

    onMount(async () => {
        try {
            const response = await fetch(
                'http://localhost:8080/api/products'
            );

            if (!response.ok) {
                throw new Error('Unable to load suggestions');
            }

            allProducts = await response.json();
        } catch (error) {
            console.error('Unable to load suggestions:', error);
        } finally {
            suggestionsLoading = false;
        }
    });

    async function handleAddToCart() {
        cartStatus = 'adding';

        cartStatus = (await cart.add(product.id))
            ? 'updated'
            : 'error';
    }
</script>

<svelte:head>
    <title>{product.name} | Viral Vault</title>
</svelte:head>

<main class="product-page">

    <!-- Back to Shop -->
    <a href="/" class="back-button">
        <ArrowLeft size={19} strokeWidth={2} />
        <span>Back to Shop</span>
    </a>

    <!-- Main product details -->
    <article class="product-detail product-card">

        <div class="product-image">
            <img
                src={product.imageUrl}
                alt={product.name}
            />
        </div>

        <div class="product-info">
            <h1>{product.name}</h1>

            <p class="description">
                {product.description}
            </p>

            <div class="purchase-section">
                <p class="price">
                    ${product.price.toFixed(2)}
                </p>

                {#if product.quantity > 0}
                    <div class="stock-status in-stock">
                        <CheckCircle2 size={20} strokeWidth={2.2} />
                        <span>In Stock: {product.quantity}</span>
                    </div>
                {:else}
                    <div class="stock-status out-of-stock">
                        <XCircle size={20} strokeWidth={2.2} />
                        <span>Out of Stock</span>
                    </div>
                {/if}

                <div class="cart-actions">
                    <button
                        type="button"
                        class="add-cart-button"
                        onclick={handleAddToCart}
                        disabled={
                            product.quantity <= 0 ||
                            cartStatus === 'adding'
                        }
                    >
                        {cartStatus === 'adding'
                            ? 'Adding...'
                            : 'Add to Cart'}
                    </button>

                    <a href="/cart" class="view-cart-button">
                        <ShoppingCart size={19} strokeWidth={2} />
                        <span>View Cart</span>
                    </a>
                </div>

                {#if cartStatus === 'updated'}
                    <p class="cart-message" role="status">
                        Cart updated.
                    </p>
                {:else if cartStatus === 'error'}
                    <p class="cart-message error" role="status">
                        {cart.error ?? 'Unable to update your cart.'}
                    </p>
                {/if}
            </div>
        </div>
    </article>

    <!-- Product suggestions -->
    {#if suggestionsLoading}
        <section class="suggestions-section">
            <h2>You Might Also Like</h2>
            <p>Loading suggestions...</p>
        </section>

    {:else if suggestedProducts.length > 0}
        <section class="suggestions-section">
            <h2>You Might Also Like</h2>

            <div class="suggestions-grid">
                {#each suggestedProducts as suggestion (suggestion.id)}
                    <article class="suggestion-card">
                        <a
                            href={`/product/${suggestion.id}`}
                            class="suggestion-image-link"
                            aria-label={`View ${suggestion.name}`}
                        >
                            <img
                                src={suggestion.imageUrl}
                                alt={suggestion.name}
                            />
                        </a>

                        <div class="suggestion-info">
                            <h3>{suggestion.name}</h3>

                            <p class="suggestion-description">
                                {suggestion.description}
                            </p>

                            <p class="suggestion-price">
                                ${suggestion.price.toFixed(2)}
                            </p>

                            {#if suggestion.quantity > 0}
                                <p class="suggestion-stock">
                                    In Stock: {suggestion.quantity}
                                </p>
                            {:else}
                                <p class="suggestion-stock">
                                    Out of Stock
                                </p>
                            {/if}

                            <a
                                href={`/product/${suggestion.id}`}
                                class="suggestion-button"
                            >
                                View Product
                            </a>
                        </div>
                    </article>
                {/each}
            </div>
        </section>
    {/if}
</main>

<style>
    .product-page {
        max-width: 1100px;
        margin: 0 auto;
        padding: clamp(1rem, 4vw, 2rem);
    }

    /* Back to Shop */

    .back-button {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        padding: 0.7rem 1.1rem;
        margin-bottom: 1.25rem;
        background-color: var(--color-accent);
        border: 2px solid var(--color-border);
        border-radius: 8px;
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-weight: 600;
        text-decoration: none;
        transition:
            background-color 0.2s ease,
            transform 0.2s ease;
    }

    .back-button:hover {
        background-color: #d88f83;
        transform: translateY(-2px);
    }

    /* Main product card */

    :global(.product-card).product-detail {
        display: grid;
        grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
        align-items: center;
        gap: clamp(1.5rem, 4vw, 3rem);
        margin-top: 0;
        padding: clamp(1.25rem, 3vw, 2rem);
    }

    .product-image {
        display: flex;
        align-items: center;
        justify-content: center;
        min-width: 0;
        background: #fffdf7;
        border-radius: 12px;
        overflow: hidden;
    }

    .product-image img {
        display: block;
        width: 100%;
        height: auto;
        max-height: 480px;
        object-fit: contain;
    }

    /* Product information */

    .product-info {
        display: flex;
        flex-direction: column;
        align-items: stretch;
        justify-content: center;
        gap: 1.25rem;
        min-width: 0;
    }

    .product-info h1 {
        margin: 0;
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: clamp(2rem, 3.5vw, 2.75rem);
        font-weight: 700;
        line-height: 1.15;
    }

    .product-info .description {
        margin: 0;
        color: #655047;
        font-size: 1.1rem;
        line-height: 1.65;
    }

    /* Price, stock and purchase buttons */

    .purchase-section {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
        margin-top: 0.5rem;
    }

    .purchase-section .price {
        margin: 0;
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 2rem;
        font-weight: 700;
        line-height: 1.2;
    }

    .stock-status {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.45rem 0.75rem;
        border: 1px solid;
        border-radius: 999px;
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 0.95rem;
        font-weight: 600;
    }

    .in-stock {
        background: #e4f3df;
        border-color: #9ac78d;
        color: #27653a;
    }

    .out-of-stock {
        background: #fce2df;
        border-color: #e4a09a;
        color: #a12d28;
    }

    .cart-actions {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 0.75rem;
        width: 100%;
        margin-top: 0.35rem;
    }

    .add-cart-button,
    .view-cart-button {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        min-height: 50px;
        padding: 0.75rem 1rem;
        border: 2px solid var(--color-border);
        border-radius: 9px;
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 1rem;
        font-weight: 600;
        text-align: center;
        text-decoration: none;
        cursor: pointer;
        transition:
            background-color 0.2s ease,
            transform 0.2s ease;
    }

    .add-cart-button {
        background-color: var(--color-accent);
    }

    .add-cart-button:hover:not(:disabled) {
        background-color: #d88f83;
        transform: translateY(-2px);
    }

    .add-cart-button:disabled {
        opacity: 0.55;
        cursor: not-allowed;
    }

    .view-cart-button {
        background-color: #fffdf7;
    }

    .view-cart-button:hover {
        background-color: #fff0e8;
        transform: translateY(-2px);
    }

    .cart-message {
        margin: 0;
        color: #27653a;
        font-size: 0.95rem;
        font-weight: 700;
    }

    .cart-message.error {
        color: #b91c1c;
    }

    /* You Might Also Like */

    .suggestions-section {
        margin-top: 2.5rem;
        margin-bottom: 2rem;
    }

    .suggestions-section h2 {
        margin: 0 0 1.25rem;
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 1.8rem;
    }

    .suggestions-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 1.5rem;
    }

    .suggestion-card {
        display: flex;
        flex-direction: column;
        min-width: 0;
        padding: 1.5rem;
        background-color: #f9dc7a;
        border: 2px solid var(--color-border);
        border-radius: 14px;
        box-shadow: 5px 5px 0 var(--color-border);
    }

    .suggestion-image-link {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 220px;
        background-color: white;
        border-radius: 8px;
        overflow: hidden;
    }

    .suggestion-image-link img {
        display: block;
        width: 100%;
        height: 100%;
        object-fit: contain;
    }

    .suggestion-info {
        display: flex;
        flex-direction: column;
        flex: 1;
        gap: 0.5rem;
        margin-top: 1rem;
    }

    .suggestion-info h3 {
        margin: 0;
        font-size: 1.2rem;
    }

    .suggestion-description {
        margin: 0;
        line-height: 1.4;
    }

    .suggestion-price {
        margin: 0.5rem 0 0;
        font-weight: 600;
    }

    .suggestion-stock {
        margin: 0;
    }

    .suggestion-button {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-top: auto;
        padding: 0.8rem 1rem;
        background-color: var(--color-accent);
        border: 2px solid var(--color-border);
        border-radius: 8px;
        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-weight: 600;
        text-decoration: none;
        transition: background-color 0.2s ease;
    }

    .suggestion-button:hover {
        background-color: #d88f83;
    }

    /* Responsive layout */

    @media (max-width: 900px) {
        .suggestions-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
        }
    }

    @media (max-width: 768px) {
        :global(.product-card).product-detail {
            grid-template-columns: 1fr;
            gap: 1.5rem;
        }

        .product-image img {
            max-height: 380px;
        }

        .product-info h1 {
            font-size: 2rem;
        }
    }

    @media (max-width: 550px) {
        .suggestions-grid {
            grid-template-columns: 1fr;
        }

        .suggestion-image-link {
            height: 240px;
        }

        .cart-actions {
            grid-template-columns: 1fr;
        }

        .product-info {
            gap: 1rem;
        }

        .purchase-section .price {
            font-size: 1.8rem;
        }
    }
</style>
