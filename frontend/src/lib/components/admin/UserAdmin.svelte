
<script>
    import { onMount } from 'svelte';

    const API = 'http://localhost:8080/api/users';

    let users = $state([]);
    let loading = $state(true);
    let saving = $state(false);

    let searchQuery = $state('');
    let sortBy = $state('id');
    let sortDirection = $state('asc');

    let selectedUser = $state(null);

    let fullName = $state('');
    let email = $state('');
    let password = $state('');
    let confirmPassword = $state('');

    let successMessage = $state('');
    let errorMessage = $state('');

    async function getError(response) {
        const data = await response.json().catch(() => null);

        return data?.message ||
            `Request failed (${response.status}).`;
    }

    async function loadUsers() {
        loading = true;
        errorMessage = '';

        try {
            const response = await fetch(API, {
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(await getError(response));
            }

            users = await response.json();

        } catch (error) {
            errorMessage = error.message;
        } finally {
            loading = false;
        }
    }

    onMount(() => {
        loadUsers();
    });

    function editUser(user) {

        if (user.admin || saving) return;

        selectedUser = user;
        fullName = user.fullName || '';
        email = user.email || '';

        password = '';
        confirmPassword = '';

        successMessage = '';
        errorMessage = '';
    }

    function cancelEdit() {
        selectedUser = null;
        fullName = '';
        email = '';
        password = '';
        confirmPassword = '';
        errorMessage = '';
    }

    function getFilteredUsers() {

        const query = searchQuery.trim().toLowerCase();

        const filtered = users.filter(user =>
            String(user.id).includes(query) ||
            (user.username || '').toLowerCase().includes(query) ||
            (user.fullName || '').toLowerCase().includes(query) ||
            (user.email || '').toLowerCase().includes(query)
        );

        return filtered.sort((a, b) => {

            let result = 0;

            if (sortBy === 'id') {
                result = Number(a.id) - Number(b.id);

            } else if (sortBy === 'role') {
                result = Number(a.admin) - Number(b.admin);

            } else {
                result = String(a[sortBy] || '').localeCompare(
                    String(b[sortBy] || ''),
                    undefined,
                    { sensitivity: 'base' }
                );
            }

            return sortDirection === 'asc' ? result : -result;
        });
    }

    let filteredUsers = $derived(getFilteredUsers());

    async function saveUser(event) {
        event.preventDefault();

        if (!selectedUser || saving) return;

        successMessage = '';
        errorMessage = '';

        if (password !== confirmPassword) {
            errorMessage = 'Passwords do not match.';
            return;
        }

        if (password && password.length < 8) {
            errorMessage = 'Password must be at least 8 characters.';
            return;
        }

        const body = {};

        if (fullName.trim() !== (selectedUser.fullName || '')) {
            body.fullName = fullName.trim();
        }

        if (email.trim() !== (selectedUser.email || '')) {
            body.email = email.trim();
        }

        if (password) {
            body.password = password;
        }

        if (Object.keys(body).length === 0) {
            successMessage = 'No changes were necessary.';
            cancelEdit();
            return;
        }

        saving = true;

        try {

            const response = await fetch(
                `${API}/${selectedUser.id}`,
                {
                    method: 'PATCH',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(body)
                }
            );

            if (!response.ok) {
                throw new Error(await getError(response));
            }

            const updatedUser = await response.json();

            users = users.map(user =>
                user.id === updatedUser.id
                    ? updatedUser
                    : user
            );

            cancelEdit();

            successMessage = 'User updated successfully.';

        } catch (error) {
            errorMessage = error.message;
        } finally {
            saving = false;
        }
    }
</script>

<h2>User Management</h2>

{#if successMessage}
    <div class="message success" role="status">
        {successMessage}
    </div>
{/if}

{#if errorMessage}
    <div class="message error" role="alert">
        {errorMessage}
    </div>
{/if}

<div class="filters">

    <div class="search-field">
        <label for="user-search">Search Users:</label>

        <div class="search-row">
            <input
                id="user-search"
                type="text"
                bind:value={searchQuery}
                placeholder="Search by ID, username, name or email..."
            />

            <button onclick={() => searchQuery = ''}>
                Clear
            </button>
        </div>
    </div>

    <div class="filter-field">
        <label for="user-sort">Sort By:</label>

        <select id="user-sort" bind:value={sortBy}>
            <option value="id">User ID</option>
            <option value="username">Username</option>
            <option value="fullName">Name</option>
            <option value="email">Email</option>
            <option value="role">Role</option>
        </select>
    </div>

    <div class="filter-field">
        <label for="user-direction">Direction:</label>

        <select id="user-direction" bind:value={sortDirection}>
            <option value="asc">Ascending</option>
            <option value="desc">Descending</option>
        </select>
    </div>

</div>

{#if selectedUser}

    <section class="edit-panel">

        <h3>Edit User #{selectedUser.id}</h3>

        <form onsubmit={saveUser}>

            <div class="form-grid">

                <div class="form-field">
                    <label for="edit-id">User ID:</label>

                    <input
                        id="edit-id"
                        value={selectedUser.id}
                        readonly
                    />
                </div>

                <div class="form-field">
                    <label for="edit-username">Username:</label>

                    <input
                        id="edit-username"
                        value={selectedUser.username}
                        readonly
                    />
                </div>

                <div class="form-field">
                    <label for="edit-name">Full Name:</label>

                    <input
                        id="edit-name"
                        type="text"
                        bind:value={fullName}
                        maxlength="100"
                    />
                </div>

                <div class="form-field">
                    <label for="edit-email">Email:</label>

                    <input
                        id="edit-email"
                        type="email"
                        bind:value={email}
                        maxlength="100"
                        required
                    />
                </div>

                <div class="form-field">
                    <label for="edit-password">
                        New Password (Optional):
                    </label>

                    <input
                        id="edit-password"
                        type="password"
                        autocomplete="new-password"
                        bind:value={password}
                        placeholder="Leave blank to keep current password"
                    />
                </div>

                <div class="form-field">
                    <label for="edit-confirm-password">
                        Confirm Password:
                    </label>

                    <input
                        id="edit-confirm-password"
                        type="password"
                        autocomplete="new-password"
                        bind:value={confirmPassword}
                        placeholder="Confirm new password"
                    />
                </div>

            </div>

            <p class="help-text">
                Leave both password fields blank to keep the
                user's existing password.
            </p>

            <div class="form-actions">

                <button
                    type="submit"
                    class="primary"
                    disabled={saving}
                >
                    {saving ? 'Saving...' : 'Save Changes'}
                </button>

                <button
                    type="button"
                    onclick={cancelEdit}
                    disabled={saving}
                >
                    Cancel
                </button>

            </div>

        </form>

    </section>

{/if}

<div class="section-heading">
    <h3>Registered Users ({filteredUsers.length})</h3>

    <button
        onclick={loadUsers}
        disabled={loading || saving}
    >
        Refresh
    </button>
</div>

<div class="table-container">

    <table>

        <thead>
            <tr>
                <th>User ID</th>
                <th>Username</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Actions</th>
            </tr>
        </thead>

        <tbody>

            {#if loading}

                <tr>
                    <td colspan="6">Loading users...</td>
                </tr>

            {:else if filteredUsers.length === 0}

                <tr>
                    <td colspan="6">No matching users found.</td>
                </tr>

            {:else}

                {#each filteredUsers as user (user.id)}

                    <tr class:selected={selectedUser?.id === user.id}>

                        <td>{user.id}</td>

                        <td>{user.username}</td>

                        <td>{user.fullName || 'Not provided'}</td>

                        <td>{user.email}</td>

                        <td>
                            <span
                                class="role"
                                class:admin={user.admin}
                            >
                                {user.admin ? 'Admin' : 'Customer'}
                            </span>
                        </td>

                        <td>

                            {#if user.admin}

                                <span class="protected">
                                    Protected
                                </span>

                            {:else}

                                <button
                                    onclick={() => editUser(user)}
                                    disabled={saving}
                                >
                                    Edit
                                </button>

                            {/if}

                        </td>

                    </tr>

                {/each}

            {/if}

        </tbody>

    </table>

</div>

<style>
    h2 {
        margin-bottom: 24px;
    }

    h3 {
        margin: 0 0 18px;
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

    label {
        display: block;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .search-row {
        display: flex;
        gap: 12px;
    }

    input, select, button {
        font: inherit;
        box-sizing: border-box;
        border: 1px solid #aaa;
        border-radius: 4px;
        padding: 11px 13px;
    }

    input, select {
        width: 100%;
        background: white;
        color: #222;
    }

    input[readonly] {
        background: #eee;
        color: #666;
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

    .section-heading {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
        margin: 28px 0 18px;
    }

    .section-heading h3 {
        margin: 0;
    }

    .table-container {
        overflow-x: auto;
    }

    table {
        width: 100%;
        border-collapse: collapse;
    }

    th, td {
        padding: 12px;
        border: 1px solid #ccc;
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

    .role {
        display: inline-block;
        padding: 5px 10px;
        border-radius: 4px;
        background: #eee;
        color: #333;
        font-size: 0.9rem;
    }

    .role.admin {
        background: #333;
        color: white;
    }

    .protected {
        font-size: 0.9rem;
        color: #666;
    }

    .edit-panel {
        padding: 22px;
        border: 1px solid #ccc;
        border-radius: 6px;
        background: #fafafa;
        margin-bottom: 28px;
    }

    .form-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 20px;
    }

    .form-actions {
        display: flex;
        gap: 12px;
        margin-top: 20px;
    }

    .help-text {
        color: #666;
        font-size: 0.9rem;
        margin-top: 15px;
    }

    .message {
        padding: 13px 16px;
        margin-bottom: 20px;
        border: 1px solid;
        border-radius: 4px;
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
