
<script lang="ts">
    import { onMount } from 'svelte';
    import type { Product } from '../../types/Product';

    type AdminProduct = Product & {
        category?: string;
    };

    type ProductForm = {
        name: string;
        category: string;
        description: string;
        price: number | undefined;
        quantity: number | undefined;
        imageUrl: string;
    };

    const API_URL = '/api/products';

    let products: AdminProduct[] = $state([]);
    let loading = $state(true);
    let saving = $state(false);

    let error = $state('');
    let formError = $state('');
    let success = $state('');

    let searchQuery = $state('');
    let sortBy = $state('id');
    let sortDirection = $state('asc');

    let showAddForm = $state(false);
    let editingID = $state<number | null>(null);

    let newProduct: ProductForm = $state(emptyProduct());

    function emptyProduct(): ProductForm {
        return {
            name: '',
            category: 'Collectibles',
            description: '',
            price: 0,
            quantity: 0,
            imageUrl: ''
        };
    }

    async function getError(response: Response) {

        const data = await response.json().catch(() => null);

        return data?.message ||
            `Request failed (${response.status}).`;
    }

    onMount(() => {
        loadProducts();
    });

    async function loadProducts() {

        loading = true;
        error = '';

        try {

            const response = await fetch(API_URL, {
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(await getError(response));
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

    function getFilteredProducts() {

        const query = searchQuery.trim().toLowerCase();

        const filtered = products.filter(product =>
            String(product.id).includes(query) ||
            product.name.toLowerCase().includes(query) ||
            (product.category || '').toLowerCase()
                .includes(query) ||
            (product.description || '').toLowerCase()
                .includes(query)
        );

        return filtered.sort((a, b) => {

            let result = 0;

            switch (sortBy) {

                case 'id':
                    result = a.id - b.id;
                    break;

                case 'price':
                    result = a.price - b.price;
                    break;

                case 'quantity':
                    result = a.quantity - b.quantity;
                    break;

                case 'category':
                    result = (a.category || '').localeCompare(
                        b.category || ''
                    );
                    break;

                default:
                    result = a.name.localeCompare(
                        b.name,
                        undefined,
                        { sensitivity: 'base' }
                    );
            }

            return sortDirection === 'asc'
                ? result
                : -result;
        });
    }

    let filteredProducts = $derived(getFilteredProducts());

    function startNewProduct() {

        if (saving) return;

        editingID = null;
        newProduct = emptyProduct();

        formError = '';
        error = '';
        success = '';

        showAddForm = true;
    }

    function editProduct(product: AdminProduct) {

        if (saving) return;

        editingID = product.id;

        newProduct = {
            name: product.name,
            category: product.category || 'Collectibles',
            description: product.description || '',
            price: product.price,
            quantity: product.quantity,
            imageUrl: product.imageUrl || ''
        };

        formError = '';
        error = '';
        success = '';

        showAddForm = true;
    }

    function cancelForm() {

        showAddForm = false;
        editingID = null;
        newProduct = emptyProduct();
        formError = '';
    }

    async function saveProduct(event: SubmitEvent) {

        event.preventDefault();

        if (saving) return;

        formError = '';
        success = '';

        if (!newProduct.name.trim()) {
            formError = 'Product name is required.';
            return;
        }

        if (!newProduct.category.trim()) {
            formError = 'Product category is required.';
            return;
        }

        if (
            newProduct.price === undefined ||
            !Number.isFinite(newProduct.price) ||
            newProduct.price < 0 ||
            newProduct.price > 99999999.99
        ) {
            formError = 'Please enter a valid price.';
            return;
        }

        if (
            newProduct.quantity === undefined ||
            !Number.isInteger(newProduct.quantity) ||
            newProduct.quantity < 0 ||
            newProduct.quantity > 2147483647
        ) {
            formError = 'Please enter a valid quantity.';
            return;
        }

        const isNewProduct = editingID === null;

        const url = isNewProduct
            ? API_URL
            : `${API_URL}/${editingID}`;

        saving = true;

        try {

            const response = await fetch(url, {
                method: isNewProduct ? 'POST' : 'PUT',

                headers: {
                    'Content-Type': 'application/json'
                },

                credentials: 'include',

                body: JSON.stringify(newProduct)
            });

            if (!response.ok) {
                throw new Error(await getError(response));
            }

            cancelForm();

            success = isNewProduct
                ? 'Product added successfully!'
                : 'Product updated successfully!';

            await loadProducts();

        } catch (err) {

            formError = err instanceof Error
                ? err.message
                : 'Unable to save product.';

        } finally {
            saving = false;
        }
    }

    async function deleteProduct(product: AdminProduct) {

        if (saving) return;

        const confirmed = window.confirm(
            `Are you sure you want to delete "${product.name}"?`
        );

        if (!confirmed) return;

        success = '';
        error = '';
        saving = true;

        try {

            const response = await fetch(
                `${API_URL}/${product.id}`,
                {
                    method: 'DELETE',
                    credentials: 'include'
                }
            );

            if (!response.ok) {
                throw new Error(await getError(response));
            }

            if (editingID === product.id) {
                cancelForm();
            }

            success = 'Product deleted successfully!';

            await loadProducts();

        } catch (err) {

            error = err instanceof Error
                ? err.message
                : 'Unable to delete product.';

        } finally {
            saving = false;
        }
    }

    function formatPrice(price: number) {

        return Number(price).toLocaleString('en-US', {
            style: 'currency',
            currency: 'USD'
        });
    }
</script>

<div class="page-heading">
    <h2>Product Management</h2>

    <button
        class="primary"
        onclick={startNewProduct}
        disabled={saving}
    >
        + Add New Product
    </button>
</div>

{#if success}
    <p class="message success" role="status">
        {success}
    </p>
{/if}

{#if error}
    <p class="message error" role="alert">
        {error}
    </p>
{/if}

<div class="filters">

    <div class="search-field">

        <label for="product-search">Search Products:</label>

        <div class="search-row">

            <input
                id="product-search"
                type="text"
                bind:value={searchQuery}
                placeholder="Search by ID, name or category..."
            />

            <button onclick={() => searchQuery = ''}>
                Clear
            </button>

        </div>
    </div>

    <div class="filter-field">

        <label for="product-sort">Sort By:</label>

        <select id="product-sort" bind:value={sortBy}>

            <option value="id">Product ID</option>
            <option value="name">Name</option>
            <option value="category">Category</option>
            <option value="price">Price</option>
            <option value="quantity">Quantity</option>

        </select>

    </div>

    <div class="filter-field">

        <label for="product-direction">Direction:</label>

        <select
            id="product-direction"
            bind:value={sortDirection}
        >
            <option value="asc">Ascending</option>
            <option value="desc">Descending</option>
        </select>

    </div>

</div>

{#if showAddForm}

    <section class="edit-panel">

        <h3>
            {editingID === null
                ? 'Add New Product'
                : `Edit Product #${editingID}`}
        </h3>

        {#if formError}

            <p class="message error" role="alert">
                {formError}
            </p>

        {/if}

        <form onsubmit={saveProduct}>

            <div class="form-grid">

                <div class="form-field">

                    <label for="product-name">Name:</label>

                    <input
                        id="product-name"
                        bind:value={newProduct.name}
                        maxlength="100"
                        required
                        disabled={saving}
                    />

                </div>

                <div class="form-field">

                    <label for="product-category">
                        Category:
                    </label>

                    <input
                        id="product-category"
                        bind:value={newProduct.category}
                        maxlength="50"
                        placeholder="Collectibles"
                        required
                        disabled={saving}
                    />

                </div>

                <div class="form-field full-width">

                    <label for="product-description">
                        Description:
                    </label>

                    <textarea
                        id="product-description"
                        bind:value={newProduct.description}
                        rows="4"
                        disabled={saving}
                    ></textarea>

                </div>

                <div class="form-field">

                    <label for="product-price">Price:</label>

                    <input
                        id="product-price"
                        type="number"
                        min="0"
                        max="99999999.99"
                        step="0.01"
                        bind:value={newProduct.price}
                        required
                        disabled={saving}
                    />

                </div>

                <div class="form-field">

                    <label for="product-quantity">
                        Quantity:
                    </label>

                    <input
                        id="product-quantity"
                        type="number"
                        min="0"
                        max="2147483647"
                        step="1"
                        bind:value={newProduct.quantity}
                        required
                        disabled={saving}
                    />

                </div>

                <div class="form-field full-width">

                    <label for="product-image">
                        Image URL:
                    </label>

                    <input
                        id="product-image"
                        bind:value={newProduct.imageUrl}
                        maxlength="255"
                        placeholder="/images/product.png"
                        disabled={saving}
                    />

                    {#if newProduct.imageUrl}

                        <div class="image-preview">

                            <img
                                src={newProduct.imageUrl}
                                alt="Product preview"
                            />

                        </div>

                    {/if}

                </div>

            </div>

            <div class="form-actions">

                <button
                    class="primary"
                    type="submit"
                    disabled={saving}
                >
                    {saving
                        ? 'Saving...'
                        : editingID === null
                            ? 'Save New Product'
                            : 'Save Changes'}
                </button>

                <button
                    type="button"
                    onclick={cancelForm}
                    disabled={saving}
                >
                    Cancel
                </button>

            </div>

        </form>

    </section>

{/if}

<div class="section-heading">

    <h3>Current Products ({filteredProducts.length})</h3>

    <button
        onclick={loadProducts}
        disabled={loading || saving}
    >
        Refresh
    </button>

</div>

<div class="table-container">

    <table>

        <thead>
            <tr>
                <th>ID</th>
                <th>Image</th>
                <th>Name</th>
                <th>Category</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>

        <tbody>

            {#if loading}

                <tr>
                    <td colspan="8">Loading products...</td>
                </tr>

            {:else if filteredProducts.length === 0}

                <tr>
                    <td colspan="8">
                        No matching products found.
                    </td>
                </tr>

            {:else}

                {#each filteredProducts as product (product.id)}

                    <tr class:selected={editingID === product.id}>

                        <td>{product.id}</td>

                        <td>
                            {#if product.imageUrl}

                                <img
                                    class="product-image"
                                    src={product.imageUrl}
                                    alt={product.name}
                                />

                            {:else}
                                —
                            {/if}
                        </td>

                        <td>{product.name}</td>

                        <td>
                            {product.category || 'Uncategorized'}
                        </td>

                        <td>{formatPrice(product.price)}</td>

                        <td>{product.quantity}</td>

                        <td>

                            <span
                                class="stock"
                                class:out-of-stock={product.quantity === 0}
                                class:low-stock={product.quantity > 0 &&
                                    product.quantity <= 5}
                            >

                                {product.quantity === 0
                                    ? 'Out of Stock'
                                    : product.quantity <= 5
                                        ? 'Low Stock'
                                        : 'In Stock'}

                            </span>

                        </td>

                        <td>

                            <div class="action-row">

                                <button
                                    onclick={() =>
                                        editProduct(product)}
                                    disabled={saving}
                                >
                                    Edit
                                </button>

                                <button
                                    class="danger"
                                    onclick={() =>
                                        deleteProduct(product)}
                                    disabled={saving}
                                >
                                    Delete
                                </button>

                            </div>

                        </td>

                    </tr>

                {/each}

            {/if}

        </tbody>

    </table>

</div>

<style>
    h2, h3 {
        margin: 0;
    }

    .page-heading,
    .section-heading {
        display: flex;
        align-items: center;
        justify-content: space-between;
        flex-wrap: wrap;
        gap: 16px;
        margin-bottom: 24px;
    }

    .section-heading {
        margin-top: 28px;
    }

    .filters {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;
        align-items: end;
        margin-bottom: 28px;
    }

    .search-field {
        flex: 3;
        min-width: 250px;
    }

    .filter-field {
        flex: 1;
        min-width: 140px;
    }

    .search-row {
        display: flex;
        gap: 12px;
    }

    label {
        display: block;
        font-weight: 600;
        margin-bottom: 8px;
    }

    input, select, textarea, button {
        font: inherit;
        box-sizing: border-box;
        border: 1px solid #aaa;
        border-radius: 4px;
        padding: 11px 13px;
    }

    input, select, textarea {
        width: 100%;
        min-width: 0;
        background: white;
        color: #222;
    }

    textarea {
        resize: vertical;
    }

    button {
        background: #eee;
        color: #222;
        cursor: pointer;
        white-space: nowrap;
    }

    button:hover:not(:disabled) {
        background: #ddd;
    }

    button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .primary {
        background: #333;
        color: white;
        border-color: #333;
    }

    .primary:hover:not(:disabled) {
        background: #555;
    }

    .danger {
        color: #b91c1c;
        background: #fff;
        border-color: #d99;
    }

    .danger:hover:not(:disabled) {
        background: #fee2e2;
    }

    .edit-panel {
        padding: 22px;
        border: 1px solid #ccc;
        border-radius: 6px;
        background: #fafafa;
        margin-bottom: 28px;
    }

    .edit-panel h3 {
        margin-bottom: 22px;
    }

    .form-grid {
        display: grid;
        grid-template-columns:
            repeat(2, minmax(0, 1fr));
        gap: 20px;
    }

    .full-width {
        grid-column: 1 / -1;
    }

    .form-actions {
        display: flex;
        gap: 12px;
        margin-top: 24px;
    }

    .image-preview {
        margin-top: 14px;
    }

    .image-preview img {
        width: 120px;
        height: 120px;
        object-fit: contain;
        background: white;
        border: 1px solid #ddd;
        border-radius: 4px;
    }

    .table-container {
        overflow-x: auto;
    }

    table {
        width: 100%;
        border-collapse: collapse;
    }

    th, td {
        border: 1px solid #ccc;
        padding: 12px;
        text-align: left;
    }

    th {
        background: #eee;
    }

    tbody tr:hover {
        background: #f8f8f8;
    }

    tr.selected {
        background: #f1f1f1;
    }

    .product-image {
        width: 55px;
        height: 55px;
        object-fit: contain;
    }

    .action-row {
        display: flex;
        gap: 8px;
    }

    .stock {
        display: inline-block;
        padding: 5px 9px;
        border-radius: 4px;
        background: #dcfce7;
        color: #166534;
        white-space: nowrap;
        font-size: 0.85rem;
    }

    .stock.low-stock {
        background: #fef3c7;
        color: #92400e;
    }

    .stock.out-of-stock {
        background: #fee2e2;
        color: #991b1b;
    }

    .message {
        padding: 13px 16px;
        border-radius: 4px;
        border: 1px solid;
        margin-bottom: 20px;
    }

    .success {
        background: #edf7ed;
        border-color: #8bb98b;
        color: #245c24;
    }

    .error {
        background: #fff0f0;
        border-color: #d99;
        color: #942323;
    }

    @media (max-width: 700px) {

        .form-grid {
            grid-template-columns: 1fr;
        }

        .filters {
            flex-direction: column;
            align-items: stretch;
        }

        .search-field {
            min-width: 0;
        }
    }
</style>
