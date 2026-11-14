
<!--
TODO: SALES ADMIN

[ ] Load products from backend
[ ] Populate product selection dropdown
[ ] Connect Save Sale to backend API
[ ] Implement editing existing sales
[ ] Implement removing sales
[ ] Verify sale prices appear on storefront
[ ] Verify sale prices appear in shopping cart
[ ] Add validation and error handling

NOTE: All controls are currently UI placeholders.
-->

<h2>Sales Management</h2>

<h3>Create or Modify Sale</h3>

<form>
    <p>
        <label for="sale-product">Product:</label>
        <select id="sale-product">
            <option value="">Select a product</option>
            <!-- TODO: Populate with products from API -->
        </select>
    </p>

    <p>
        <label for="sale-price">Sale Price ($):</label>
        <input
            id="sale-price"
            type="number"
            min="0"
            step="0.01"
        />
    </p>

    <p>
        <label for="sale-active">Sale Active:</label>
        <input
            id="sale-active"
            type="checkbox"
            checked
        />
    </p>

    <button type="button">Save Sale</button>
</form>

<hr />

<h3>Current Sales</h3>

<table>
    <thead>
        <tr>
            <th>Product ID</th>
            <th>Product Name</th>
            <th>Original Price</th>
            <th>Sale Price</th>
            <th>Active</th>
            <th>Actions</th>
        </tr>
    </thead>

    <tbody>
        <!-- TODO: Populate from backend -->
    </tbody>
</table>

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
</style>
