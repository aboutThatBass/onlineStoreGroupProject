<!--
TODO: PRODUCT ADMIN
[ ] Create entry point via login page
[ ] Connect Add Product form to POST /api/products
[ ] Connect Edit Product form to PUT /api/products/{id}
[ ] Connect Delete button to DELETE /api/products/{id}
[ ] Refresh product table after successful create/edit/delete
[ ] Add success and error messages for API operations
[ ] Disable Save/Delete buttons while requests are processing
[ ] Validate product inputs before submission
[ ] Verify changes persist after refreshing the page
[ ] Verify changes appear correctly on the storefront
[ ] Verify admin endpoints require administrator authorization

NOTE:
- Product listing already uses the live GET /api/products endpoint.
- Create/Edit currently log to console only.
- Delete currently uses a confirmation dialog and logs to console.
- Backend write endpoints are pending Pedro's team.
- Endpoint paths are proposed and may change.
-->


<script lang="ts">
    import { onMount } from 'svelte';
    import type { Product } from '$lib/types/Product';

    let products: Product[] = $state([]);
    let loading = $state(true);
    let error = $state('');
    let showAddForm = $state(false);
    let newProduct = $state({
        name: '',
        description: '',
        price: 0,
        quantity: 0,
        imageUrl: ''
        });
    let editingID = $state<number | null>(null);

    onMount(async () => {
    try {
        const response = await fetch(
        'http://localhost:8080/api/products'
        );

        if (!response.ok) {
        throw new Error('Failed to fetch products');
        }

        products = await response.json();
    } catch (err) {
        console.error(err);
        error = 'Unable to load products.';
    } finally {
        loading = false;
    }
    });

    
    function editProduct(product: Product) {
        editingID = product.id;

        newProduct = {
            name: product.name,
            description: product.description,
            price: product.price,
            quantity: product.quantity,
            imageUrl: product.imageUrl
        };

        showAddForm = true;
    }

    function startNewProduct() {
        editingID = null;

        newProduct = {
            name: '',
            description: '',
            price: 0,
            quantity: 0,
            imageUrl: ''
        };

        showAddForm = true;
    }

    function deleteProduct(product: Product) {
        const confirmed = window.confirm(
            `Are you sure you want to delete "${product.name}"?`
        );

        if (!confirmed) return;

        console.log('DELETE PRODUCT', product.id);

        // TODO: Connect to DELETE /api/products/{id}
    }
</script>

<h2>Product Management</h2>

<button onclick={startNewProduct}>
    Add New Product
</button>
<!--Logic for unhiding form for adding new product-->
{#if showAddForm}
    <h3>{editingID === null ? 'Add Product' : 'Edit Product'}</h3>

    <form onsubmit={(event) => {
        event.preventDefault();

        console.log(
            editingID === null ? 'CREATE PRODUCT' : 'UPDATE PRODUCT',
            editingID,
            $state.snapshot(newProduct)
        );

        showAddForm = false;
        }}>
    <p>
        <label for="product-name">Name:</label>
        <input
        id="product-name"
        type="text"
        bind:value={newProduct.name}
        required
        />
    </p>

    <p>
        <label for="product-description">Description:</label>
        <textarea
        id="product-description"
        bind:value={newProduct.description}
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
        />
    </p>

    <p>
        <label for="product-image">Image URL:</label>
        <input
        id="product-image"
        type="url"
        bind:value={newProduct.imageUrl}
        />
    </p>

    <button type="submit">
        {editingID === null ? 'Save New Product' : 'Save Changes'}
    </button>

    <button type="button" onclick={() => showAddForm = false}>
        Cancel
        </button>
    </form>
{/if}


<hr />

<h3>Current Products</h3>

{#if loading}
  <p>Loading products...</p>
{:else if error}
  <p>{error}</p>
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
            <button onclick={() => editProduct(product)}>
                Edit
            </button>
            
            <button onclick={() => deleteProduct(product)}>
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
</style>
