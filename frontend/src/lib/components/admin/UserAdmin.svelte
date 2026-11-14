
<!--
TODO: USER ADMIN

USER RETRIEVAL
[ ] Load users from backend (GET /api/users)
[ ] Populate user table
[ ] Select a user for editing
[ ] Populate form with selected user's information

USER EDITING
[ ] Connect Save Changes to PATCH /api/users/{id}
[ ] Send only fields that have changed
[ ] Omit unchanged fields from PATCH requests
[ ] Add validation and error handling
[ ] Refresh user table after successful updates

PASSWORD MANAGEMENT
[ ] Implement administrator password reset
[ ] Leave password and confirmation fields blank by default
[ ] Blank password fields preserve existing password
[ ] Require matching confirmation when entering a new password
[ ] Omit password property entirely if no new password entered
[ ] Never retrieve or display existing passwords
[ ] Backend hashes new passwords before storing them
[ ] Backend preserves password hash when password is omitted

SECURITY
[ ] Restrict admin page to authorized administrators
[ ] Restrict user modification endpoints to administrators
[ ] Validate all submitted changes on backend
[ ] Never expose password hashes through API responses

INTEGRATION
[ ] Confirm API contracts with Pedro's team
[ ] Verify partial updates preserve unchanged user information
[ ] Verify password resets persist correctly
[ ] Verify editing name/email does not change password
[ ] Display success/error messages after API responses

NOTE:
- All controls are currently UI placeholders.
- PATCH /api/users/{id} is a proposed endpoint.
- Backend must implement partial-update semantics.
- Password fields must never be prepopulated.
- No database changes occur until APIs are integrated.
-->


<h2>User Management</h2>

<h3>Edit User</h3>

<form>
    <p>
        <label for="user-id">User ID:</label>
        <input id="user-id" type="number" readonly />
    </p>

    <p>
        <label for="user-name">Name:</label>
        <input id="user-name" type="text" />
    </p>

    <p>
        <label for="user-email">Email:</label>
        <input id="user-email" type="email" />
    </p>

    <p>
        <label for="user-password">New Password:</label>
        <input
            id="user-password"
            type="password"
            autocomplete="new-password"
            placeholder="Leave blank to keep current password"
        />
    </p>

    <p>
        <label for="user-confirm-password">Confirm Password:</label>
        <input
            id="user-confirm-password"
            type="password"
            autocomplete="new-password"
        />
    </p>

    <button type="button">Save Changes</button>
    <button type="button">Cancel</button>
</form>

<hr />

<h3>Registered Users</h3>

<table>
    <thead>
        <tr>
            <th>User ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Actions</th>
        </tr>
    </thead>

    <tbody>
        <!-- TODO: Populate from GET /api/users -->
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
