<script lang="ts">
    import { onMount } from 'svelte';

    const API = 'http://localhost:8080/api/products';

    type Product = {
        id: number;
        name: string;
        category: string;
        price: number;
        salePrice: number | null;
        quantity: number;
        onSale: boolean;
        imageUrl: string;
    };

    let products = $state<Product[]>([]);
    let selectedId = $state<number | undefined>(undefined);
    let salePrice = $state<number | undefined>(undefined);
    let search = $state('');
    let loading = $state(true);
    let saving = $state(false);
    let error = $state('');
    let success = $state('');

    let selectedProduct = $derived(
        products.find(p => p.id === selectedId)
    );

    let activeSales = $derived(
        products.filter(p => p.salePrice != null)
    );

    let filteredProducts = $derived(
        products.filter(p =>
            p.name.toLowerCase().includes(
                search.trim().toLowerCase()
            ) ||
            (p.category ?? '').toLowerCase().includes(
                search.trim().toLowerCase()
            )
        )
    );

    function money(value: number): string {
        return '$' + value.toFixed(2);
    }

    function savings(product: Product): number {
        if (product.salePrice == null || product.price <= 0) {
            return 0;
        }

        return Math.round(
            (1 - product.salePrice / product.price) * 100
        );
    }

    async function loadProducts() {
        loading = true;

        try {
            const response = await fetch(API, {
                credentials: 'include',
                cache: 'no-store'
            });

            if (!response.ok) {
                throw new Error('Unable to load products.');
            }

            products = await response.json();
        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Unable to load products.';
        } finally {
            loading = false;
        }
    }

    onMount(() => {
        void loadProducts();
    });

    function selectProduct() {
        salePrice = selectedProduct?.salePrice ?? undefined;
        error = '';
        success = '';
    }

    function editSale(product: Product) {
        selectedId = product.id;
        salePrice = product.salePrice ?? undefined;
        error = '';
        success = '';

        document.getElementById('sale-form')
            ?.scrollIntoView({ behavior: 'smooth' });
    }

    function resetForm() {
        selectedId = undefined;
        salePrice = undefined;
    }

    async function updateSale(
        product: Product,
        price: number | null
    ) {
        if (saving) return;

        saving = true;
        error = '';
        success = '';

        try {
            const response = await fetch(
                `${API}/${product.id}/sale`,
                {
                    method: 'PATCH',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        salePrice: price
                    })
                }
            );

            if (!response.ok) {
                const data = await response.json()
                    .catch(() => null);

                throw new Error(
                    data?.message ?? 'Unable to update sale.'
                );
            }

            const updated: Product = await response.json();

            products = products.map(p =>
                p.id === updated.id ? updated : p
            );

            success = price === null
                ? `${product.name} is no longer on sale.`
                : `${product.name} is now on sale for ${money(price)}.`;

            resetForm();

        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Unable to update sale.';
        } finally {
            saving = false;
        }
    }

    async function saveSale(event: SubmitEvent) {
        event.preventDefault();

        if (!selectedProduct) {
            error = 'Please select a product.';
            return;
        }

        if (
            salePrice === undefined ||
            !Number.isFinite(salePrice) ||
            salePrice < 0 ||
            Math.round(salePrice * 100) >=
                Math.round(selectedProduct.price * 100)
        ) {
            error = 'Sale price must be less than the regular price.';
            return;
        }

        await updateSale(
            selectedProduct,
            Math.round(salePrice * 100) / 100
        );
    }

    async function removeSale(product: Product) {
        if (!confirm(
            `Remove the sale for ${product.name}?`
        )) return;

        await updateSale(product, null);
    }
</script>

<section class="sales-admin">
    <div class="heading">
        <div>
            <h2>Sales Management</h2>
            <p>Create and manage product sale prices.</p>
        </div>

        <button
            class="secondary"
            onclick={loadProducts}
            disabled={loading || saving}
        >
            Refresh
        </button>
    </div>

    <div class="stats">
        <div class="stat">
            <span>Total Products</span>
            <strong>{products.length}</strong>
        </div>

        <div class="stat">
            <span>Active Sales</span>
            <strong>{activeSales.length}</strong>
        </div>

        <div class="stat">
            <span>Regular Price Products</span>
            <strong>
                {products.length - activeSales.length}
            </strong>
        </div>
    </div>

    {#if error}
        <p class="message error" role="alert">{error}</p>
    {/if}

    {#if success}
        <p class="message success" role="status">
            {success}
        </p>
    {/if}

    <form
        id="sale-form"
        class="sale-form"
        onsubmit={saveSale}
    >
        <h3>Set or Update a Sale</h3>

        <div class="form-grid">
            <div class="field">
                <label for="sale-product">Product</label>

                <select
                    id="sale-product"
                    bind:value={selectedId}
                    onchange={selectProduct}
                    disabled={saving}
                    required
                >
                    <option value={undefined}>
                        Select a product
                    </option>

                    {#each products as product}
                        <option value={product.id}>
                            {product.name} - {money(product.price)}
                        </option>
                    {/each}
                </select>
            </div>

            <div class="field">
                <label for="sale-price">Sale Price ($)</label>

                <input
                    id="sale-price"
                    type="number"
                    bind:value={salePrice}
                    min="0"
                    step="0.01"
                    placeholder="0.00"
                    disabled={!selectedProduct || saving}
                    required
                />
            </div>
        </div>

        {#if selectedProduct}
            <div class="price-preview">
                <span>
                    Regular price:
                    <strong>
                        {money(selectedProduct.price)}
                    </strong>
                </span>

                {#if salePrice !== undefined &&
                    salePrice >= 0 &&
                    salePrice < selectedProduct.price}
                    <span class="saving-text">
                        Customer saves
                        {money(selectedProduct.price - salePrice)}
                    </span>
                {/if}
            </div>
        {/if}

        <div class="form-actions">
            <button
                type="submit"
                disabled={saving || !selectedProduct}
            >
                {saving ? 'Saving...' : 'Save Sale'}
            </button>

            <button
                type="button"
                class="secondary"
                onclick={resetForm}
                disabled={saving}
            >
                Cancel
            </button>
        </div>
    </form>

    <div class="table-heading">
        <h3>Product Sales</h3>

        <input
            type="search"
            bind:value={search}
            placeholder="Search products..."
            aria-label="Search products"
        />
    </div>

    {#if loading}
        <p>Loading products...</p>
    {:else}
        <div class="table-wrapper">
            <table>
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Regular Price</th>
                        <th>Sale Price</th>
                        <th>Savings</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>
                    {#each filteredProducts as product (product.id)}
                        <tr>
                            <td>
                                <div class="product-info">
                                    <img
                                        src={product.imageUrl}
                                        alt=""
                                    />
                                    <div>
                                        <strong>
                                            {product.name}
                                        </strong>
                                        <small>
                                            {product.category}
                                        </small>
                                    </div>
                                </div>
                            </td>

                            <td>{money(product.price)}</td>

                            <td>
                                {#if product.salePrice != null}
                                    <strong class="saving-text">
                                        {money(product.salePrice)}
                                    </strong>
                                {:else}
                                    -
                                {/if}
                            </td>

                            <td>
                                {product.salePrice != null
                                    ? `${savings(product)}%`
                                    : '-'}
                            </td>

                            <td>
                                <span
                                    class="status"
                                    class:active={product.salePrice != null}
                                >
                                    {product.salePrice != null
                                        ? 'On Sale'
                                        : 'Regular'}
                                </span>
                            </td>

                            <td>
                                <div class="row-actions">
                                    <button
                                        class="small secondary"
                                        onclick={() => editSale(product)}
                                        disabled={saving}
                                    >
                                        {product.salePrice != null
                                            ? 'Edit'
                                            : 'Set Sale'}
                                    </button>

                                    {#if product.salePrice != null}
                                        <button
                                            class="small danger"
                                            onclick={() => removeSale(product)}
                                            disabled={saving}
                                        >
                                            Remove
                                        </button>
                                    {/if}
                                </div>
                            </td>
                        </tr>
                    {:else}
                        <tr>
                            <td colspan="6">No matching products.</td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    {/if}
</section>

<style>
    .sales-admin {
        color: #292929;
        padding: 1rem 0;
    }

    .heading,
    .table-heading {
        display: flex;
        align-items: center;
        justify-content: space-between;
        flex-wrap: wrap;
        gap: 1rem;
        margin-bottom: 1.25rem;
    }

    h2, h3, p {
        margin: 0;
    }

    .heading p {
        margin-top: 0.4rem;
        color: #666;
    }

    .stats {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 1rem;
        margin-bottom: 1.5rem;
    }

    .stat {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        padding: 1.25rem;
        border: 1px solid #ddd;
        border-radius: 10px;
        background: white;
    }

    .stat span {
        color: #666;
        font-size: 0.9rem;
    }

    .stat strong {
        font-size: 1.8rem;
    }

    .sale-form {
        border: 1px solid #ddd;
        border-radius: 12px;
        background: #fafafa;
        padding: 1.5rem;
        margin-bottom: 2rem;
    }

    .form-grid {
        display: grid;
        grid-template-columns: 2fr 1fr;
        gap: 1rem;
        margin-top: 1.25rem;
    }

    .field {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    label {
        font-weight: 600;
    }

    input, select {
        box-sizing: border-box;
        width: 100%;
        padding: 0.75rem;
        border: 1px solid #ccc;
        border-radius: 7px;
        background: white;
        color: #292929;
        font: inherit;
    }

    .table-heading input {
        max-width: 280px;
    }

    .form-actions,
    .row-actions,
    .price-preview {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 0.75rem;
        margin-top: 1rem;
    }

    button {
        background: #333;
        color: white;
        border: 1px solid #333;
        border-radius: 7px;
        padding: 0.65rem 1rem;
        font: inherit;
        font-weight: 600;
        cursor: pointer;
    }

    button:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    button:hover:not(:disabled) {
        filter: brightness(0.9);
    }

    button.secondary {
        background: white;
        color: #333;
        border-color: #ccc;
    }

    button.danger {
        background: #fff0f0;
        color: #a11f24;
        border-color: #e4aaaa;
    }

    button.small {
        font-size: 0.82rem;
        padding: 0.45rem 0.65rem;
    }

    .table-wrapper {
        overflow-x: auto;
        border: 1px solid #ddd;
        border-radius: 10px;
        background: white;
    }

    table {
        width: 100%;
        border-collapse: collapse;
        text-align: left;
    }

    th, td {
        padding: 1rem;
        border-bottom: 1px solid #e5e5e5;
    }

    th {
        background: #f1f1f1;
        font-size: 0.85rem;
        white-space: nowrap;
    }

    td {
        font-size: 0.9rem;
    }

    .product-info {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        min-width: 180px;
    }

    .product-info img {
        width: 48px;
        height: 48px;
        object-fit: contain;
        border-radius: 6px;
    }

    .product-info small {
        display: block;
        margin-top: 0.25rem;
        color: #777;
    }

    .row-actions {
        margin: 0;
    }

    .status {
        display: inline-block;
        background: #eee;
        color: #555;
        padding: 0.3rem 0.65rem;
        border-radius: 20px;
        white-space: nowrap;
        font-size: 0.8rem;
    }

    .status.active {
        background: #dcf3e3;
        color: #24683c;
    }

    .saving-text {
        color: #24683c;
        font-weight: 600;
    }

    .message {
        padding: 0.85rem 1rem;
        border-radius: 7px;
        margin-bottom: 1rem;
    }

    .message.error {
        background: #fce8e8;
        color: #a11f24;
    }

    .message.success {
        background: #e4f5e8;
        color: #24683c;
    }

    @media (max-width: 700px) {
        .stats, .form-grid {
            grid-template-columns: 1fr;
        }
    }
</style>