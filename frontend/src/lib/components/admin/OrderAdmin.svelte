
<!--
TODO: ORDER ADMIN

ORDER RETRIEVAL
[ ] Retrieve orders from backend
[ ] Populate current orders table
[ ] Populate order history table
[ ] Confirm order data structure with Pedro's team

ORDER DETAILS
[ ] Display customer information
[ ] Display order date and total
[ ] Display individual items and quantities
[ ] Implement View Details button

ORDER HISTORY
[ ] Determine how current and historical orders are distinguished
[ ] Implement sorting by order date
[ ] Implement sorting by customer
[ ] Implement sorting by total dollar amount
[ ] Verify sorting works correctly

INTEGRATION
[ ] Connect to backend order endpoints
[ ] Add loading and error handling
[ ] Verify newly placed orders appear in admin
[ ] Verify historical orders persist after refresh

NOTE:
- All controls are currently UI placeholders.
- No mock data or API calls are implemented.
-->

<h2>Order Management</h2>

<h3>Current Orders</h3>

<table>
    <thead>
        <tr>
            <th>Order ID</th>
            <th>Customer</th>
            <th>Date</th>
            <th>Total</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
    </thead>

    <tbody>
        <!-- TODO: Populate current orders from API -->
    </tbody>
</table>

<hr />

<h3>Order History</h3>

<p>
    <label for="order-sort">Sort By:</label>
    <select id="order-sort">
        <option value="date">Order Date</option>
        <option value="customer">Customer</option>
        <option value="total">Order Total</option>
    </select>

    <label for="sort-direction">Direction:</label>
    <select id="sort-direction">
        <option value="desc">Descending</option>
        <option value="asc">Ascending</option>
    </select>
</p>

<table>
    <thead>
        <tr>
            <th>Order ID</th>
            <th>Customer</th>
            <th>Date</th>
            <th>Total</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
    </thead>

    <tbody>
        <!-- TODO: Populate order history from API -->
    </tbody>
</table>

<style>
    table {
        width: 100%;
        border-collapse: collapse;
        margin-bottom: 20px;
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
