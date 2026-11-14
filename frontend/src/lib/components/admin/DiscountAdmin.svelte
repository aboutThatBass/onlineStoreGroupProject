<!--
TODO: DISCOUNT ADMIN

[ ] Retrieve existing discounts from backend
[ ] Connect Save Discount to API
[ ] Implement editing existing discounts
[ ] Implement deactivation/deletion
[ ] Validate discount codes and values
[ ] Display success/error messages
[ ] Verify discounts are reflected in shopping cart

NOTE: All controls are currently UI placeholders.
-->

<h2>Discount Management</h2>

<h3>Create Discount Code</h3>

<form>
    <p>
        <label for="code">Discount Code:</label>
        <input id="code" type="text" placeholder="SAVE10" />
    </p>

    <p>
        <label for="type">Discount Type:</label>
        <select id="type">
            <option value="percentage">Percentage</option>
            <option value="fixed">Fixed Amount</option>
        </select>
    </p>

    <p>
        <label for="value">Discount Value:</label>
        <input id="value" type="number" min="0" step="0.01" />
    </p>

    <p>
        <label for="active">Active:</label>
        <input id="active" type="checkbox" checked />
    </p>

    <button type="button">Save Discount</button>
</form>

<hr />

<h3>Existing Discounts</h3>

<table>
    <thead>
        <tr>
            <th>Code</th>
            <th>Type</th>
            <th>Value</th>
            <th>Active</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <!-- TODO: Populate from GET /api/discounts -->
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
