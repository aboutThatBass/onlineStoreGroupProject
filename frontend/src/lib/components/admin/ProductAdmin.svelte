
<script lang="ts">
    import { onMount } from 'svelte';
    import type { Product } from '$lib/types/Product';

    const API_URL = 'http://localhost:8080/api/products';

    let products: Product[] = $state([]);
    let loading = $state(true);
    let saving = $state(false);
    let error = $state('');
    let formError = $state('');
    let success = $state('');

    let showAddForm = $state(false);
    let editingID = $state<number | null>(null);

    let newProduct = $state({
        name: '',
        description: '',
        price: 0,
        quantity: 0,
        imageUrl: ''
    });

    onMount(async () => {
        await loadProducts();
    });

    async function loadProducts() {
        loading = true;
        error = '';

        try {
            const response = await fetch(API_URL);

            if (!response.ok) {
                throw new Error('Failed to fetch products.');
            }

            products = await response.json();
        } catch (err) {
            console.error(err);
            error = 'Unable to load products.';
        } finally {
            loading = false;
        }
    }

    function editProduct(product: Product) {
        if (saving) return;

        editingID = product.id;
        formError = '';
        success = '';

        newProduct = {
            name: product.name,
            description: product.description ?? '',
            price: product.price,
            quantity: product.quantity,
            imageUrl: product.imageUrl ?? ''
        };

        showAddForm = true;
    }

    function startNewProduct() {
        if (saving) return;

        editingID = null;
        formError = '';
        success = '';

        newProduct = {
            name: '',
            description: '',
            price: 0,
            quantity: 0,
            imageUrl: ''
        };

        showAddForm = true;
    }

    function cancelForm() {
        showAddForm = false;
        editingID = null;
        formError = '';
    }

    async function saveProduct(event: SubmitEvent) {
        event.preventDefault();

        if (saving) return;

        formError = '';
        success = '';

        if (editingID === null) {
            formError = 'Adding products is not implemented yet.';
            return;
        }

        if (!newProduct.name.trim()) {
            formError = 'Product name is required.';
            return;
        }

        if (
            !Number.isFinite(newProduct.price) ||
            newProduct.price < 0
        ) {
            formError = 'Please enter a valid product price.';
            return;
        }

        if (
            !Number.isInteger(newProduct.quantity) ||
            newProduct.quantity < 0
        ) {
            formError = 'Please enter a valid quantity.';
            return;
        }

        saving = true;

        try {
            const response = await fetch(
                `${API_URL}/${editingID}`,
                {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include',
                    body: JSON.stringify(newProduct)
                }
            );

            if (!response.ok) {
                throw new Error(
                    `Unable to update product (${response.status}).`
                );
            }

            success = 'Product updated successfully!';
            showAddForm = false;
            editingID = null;

            await loadProducts();

        } catch (err) {
            console.error(err);

            formError = err instanceof Error
                ? err.message
                : 'Unable to save product changes.';
        } finally {
            saving = false;
        }
    }

    function deleteProduct(product: Product) {
        const confirmed = window.confirm(
            `Are you sure you want to delete "${product.name}"?`
        );

        if (!confirmed) return;

        console.log('DELETE PRODUCT', product.id);

        success = '';
        error = 'Deleting products is not implemented yet.';
    }
</script>

<h2>Product Management</h2>

<button onclick={startNewProduct} disabled={saving}>
    Add New Product
</button>

{#if success}
    <p class="success-message">{success}</p>
{/if}

{#if showAddForm}
    <h3>
        {editingID === null ? 'Add Product' : 'Edit Product'}
    </h3>

    {#if formError}
        <p class="error-message">{formError}</p>
    {/if}

    <form onsubmit={saveProduct}>
        <p>
            <label for="product-name">Name:</label>
            <input
                id="product-name"
                type="text"
                bind:value={newProduct.name}
                required
                disabled={saving}
            />
        </p>

        <p>
            <label for="product-description">Description:</label>
            <textarea
                id="product-description"
                bind:value={newProduct.description}
                disabled={saving}
            ></textarea>
        </p>

        <p>
            <label for="product-price">Price:</label>
            <input
                id="product-price"
                type="number"
                min="0"
                step="0.01"
                bind:value={newProduct.price}
                required
                disabled={saving}
            />
        </p>

        <p>
            <label for="product-quantity">Quantity:</label>
            <input
                id="product-quantity"
                type="number"
                min="0"
                step="1"
                bind:value={newProduct.quantity}
                required
                disabled={saving}
            />
        </p>

        <p>
            <label for="product-image">Image URL:</label>
            <input
                id="product-image"
                type="text"
                bind:value={newProduct.imageUrl}
                disabled={saving}
            />
        </p>

        <button type="submit" disabled={saving}>
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
    </form>
{/if}

<hr />

<h3>Current Products</h3>

{#if error}
    <p class="error-message">{error}</p>
{/if}

{#if loading}
    <p>Loading products...</p>
{:else if products.length === 0}
    <p>No products found.</p>
{:else}
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Actions</th>
            </tr>
        </thead>

        <tbody>
            {#each products as product (product.id)}
                <tr>
                    <td>{product.id}</td>
                    <td>{product.name}</td>
                    <td>${product.price.toFixed(2)}</td>
                    <td>{product.quantity}</td>
                    <td>
                        <button
                            onclick={() => editProduct(product)}
                            disabled={saving}
                        >
                            Edit
                        </button>

                        <button
                            onclick={() => deleteProduct(product)}
                            disabled={saving}
                        >
                            Delete
                        </button>
                    </td>
                </tr>
            {/each}
        </tbody>
    </table>
{/if}

<style>
    table {
        width: 100%;
        border-collapse: collapse;
    }

    th, td {
        padding: 10px;
        border: 1px solid #ccc;
        text-align: left;
    }

    th {
        background: #eee;
    }

    button {
        margin-right: 5px;
        cursor: pointer;
    }

    button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .success-message {
        color: #166534;
        background: #f0fdf4;
        border: 1px solid #bbf7d0;
        padding: 10px;
        border-radius: 4px;
    }

    .error-message {
        color: #b91c1c;
        background: #fef2f2;
        border: 1px solid #fecaca;
        padding: 10px;
        border-radius: 4px;
    }
</style>
