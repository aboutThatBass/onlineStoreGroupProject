<script lang="ts">
    import { onMount } from 'svelte';

    type Discount = {
        id: number;
        code: string;
        discountPercent: number;
        discountType: 'percent' | 'fixed';
        fixedAmount: number;
        active: boolean;
    };

    const API = '/api/discounts';

    let discounts = $state<Discount[]>([]);
    let code = $state('');
    let discountType = $state<'percent' | 'fixed'>('percent');
    let value = $state<number | undefined>(10);
    let active = $state(true);
    let editingId = $state<number | null>(null);
    let search = $state('');
    let loading = $state(false);
    let saving = $state(false);
    let error = $state('');
    let success = $state('');

    let activeCount = $derived(
        discounts.filter(discount => discount.active).length
    );

    let inactiveCount = $derived(
        discounts.length - activeCount
    );

    let filteredDiscounts = $derived(
        discounts.filter(discount =>
            discount.code.toLowerCase().includes(
                search.trim().toLowerCase()
            )
        )
    );

    onMount(() => {
        loadDiscounts();
    });

    async function readError(response: Response) {
        try {
            const data = await response.json();

            return data.message || 'Request failed.';
        } catch {
            return 'Request failed.';
        }
    }

    async function loadDiscounts() {
        loading = true;
        error = '';

        try {
            const response = await fetch(API, {
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(await readError(response));
            }

            discounts = await response.json();

        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Unable to load discounts.';
        } finally {
            loading = false;
        }
    }

    function resetForm() {
        code = '';
        discountType = 'percent';
        value = 10;
        active = true;
        editingId = null;
    }

    function cancelEdit() {
        resetForm();
        error = '';
        success = '';
    }

    function editDiscount(discount: Discount) {
        editingId = discount.id;
        code = discount.code;
        discountType = discount.discountType;

        value = discount.discountType === 'fixed'
            ? discount.fixedAmount
            : discount.discountPercent;

        active = discount.active;

        error = '';
        success = '';
    }

    async function saveDiscount(event: SubmitEvent) {
        event.preventDefault();

        if (saving) return;

        error = '';
        success = '';

        const normalizedCode = code.trim().toUpperCase();
        const amount = Number(value);

        if (!normalizedCode) {
            error = 'Please enter a discount code.';
            return;
        }

        if (normalizedCode.length > 50) {
            error = 'Discount codes cannot exceed 50 characters.';
            return;
        }

        if (!Number.isFinite(amount) || amount < 0.01) {
            error = 'Please enter a valid discount amount.';
            return;
        }

        if (discountType === 'percent' && amount > 100) {
            error = 'Percentage discounts cannot exceed 100%.';
            return;
        }

        if (discountType === 'fixed' && amount > 99999999.99) {
            error = 'Fixed discount amount is too large.';
            return;
        }

        const isEditing = editingId !== null;

        const payload = {
            code: normalizedCode,
            discountType,
            discountPercent:
                discountType === 'percent' ? amount : 0,
            fixedAmount:
                discountType === 'fixed' ? amount : 0,
            isActive: active
        };

        saving = true;

        try {
            const response = await fetch(
                isEditing ? `${API}/${editingId}` : API,
                {
                    method: isEditing ? 'PATCH' : 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(payload)
                }
            );

            if (!response.ok) {
                throw new Error(await readError(response));
            }

            resetForm();
            await loadDiscounts();

            success = isEditing
                ? 'Discount updated successfully.'
                : 'Discount created successfully.';

        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Unable to save discount.';
        } finally {
            saving = false;
        }
    }

    async function toggleDiscount(discount: Discount) {
        if (saving) return;

        error = '';
        success = '';
        saving = true;

        try {
            const response = await fetch(
                `${API}/${discount.id}`,
                {
                    method: 'PATCH',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        isActive: !discount.active
                    })
                }
            );

            if (!response.ok) {
                throw new Error(await readError(response));
            }

            if (editingId === discount.id) {
                active = !discount.active;
            }

            await loadDiscounts();

            success = discount.active
                ? 'Discount deactivated successfully.'
                : 'Discount activated successfully.';

        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Unable to update discount.';
        } finally {
            saving = false;
        }
    }

    function formatDiscount(discount: Discount) {
        return discount.discountType === 'fixed'
            ? `$${discount.fixedAmount.toFixed(2)}`
            : `${discount.discountPercent}%`;
    }
</script>

<section class="discount-admin">

    <h2>Discount Management</h2>

    <p class="description">
        Create, edit, activate, and deactivate discount codes.
    </p>

    <div class="stats">

        <div class="stat-card">
            <span>Total Discounts</span>
            <strong>{discounts.length}</strong>
        </div>

        <div class="stat-card">
            <span>Active Discounts</span>
            <strong>{activeCount}</strong>
        </div>

        <div class="stat-card">
            <span>Inactive Discounts</span>
            <strong>{inactiveCount}</strong>
        </div>

    </div>

    {#if error}
        <div class="message error" role="alert">
            {error}
        </div>
    {/if}

    {#if success}
        <div class="message success" role="status">
            {success}
        </div>
    {/if}

    <form class="discount-form" onsubmit={saveDiscount}>

        <h3>
            {editingId !== null
                ? 'Edit Discount Code'
                : 'Create Discount Code'}
        </h3>

        <div class="form-grid">

            <label>
                Discount Code

                <input
                    type="text"
                    bind:value={code}
                    placeholder="SAVE10"
                    maxlength="50"
                    required
                />
            </label>

            <label>
                Discount Type

                <select bind:value={discountType}>
                    <option value="percent">
                        Percentage (%)
                    </option>

                    <option value="fixed">
                        Fixed Amount ($)
                    </option>
                </select>
            </label>

            <label>
                {discountType === 'percent'
                    ? 'Discount Percentage (%)'
                    : 'Discount Amount ($)'}

                <input
                    type="number"
                    bind:value={value}
                    min="0.01"
                    max={discountType === 'percent'
                        ? 100
                        : 99999999.99}
                    step="0.01"
                    required
                />
            </label>

        </div>

        <label class="checkbox-label">
            <input type="checkbox" bind:checked={active} />
            Active Discount
        </label>

        <div class="form-actions">

            <button
                type="submit"
                class="primary-button"
                disabled={saving}
            >
                {saving
                    ? 'Saving...'
                    : editingId !== null
                        ? 'Save Changes'
                        : 'Create Discount'}
            </button>

            {#if editingId !== null}
                <button
                    type="button"
                    class="secondary-button"
                    onclick={cancelEdit}
                    disabled={saving}
                >
                    Cancel
                </button>
            {/if}

        </div>

    </form>

    <div class="table-header">

        <h3>Existing Discounts</h3>

        <input
            type="search"
            bind:value={search}
            placeholder="Search discount codes..."
            aria-label="Search discount codes"
        />

    </div>

    {#if loading}

        <p>Loading discounts...</p>

    {:else}

        <div class="table-wrapper">

            <table>

                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Discount Code</th>
                        <th>Type</th>
                        <th>Value</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>

                    {#each filteredDiscounts as discount (discount.id)}

                        <tr>

                            <td>{discount.id}</td>

                            <td>
                                <strong>{discount.code}</strong>
                            </td>

                            <td>
                                {discount.discountType === 'fixed'
                                    ? 'Fixed Amount'
                                    : 'Percentage'}
                            </td>

                            <td>
                                {formatDiscount(discount)}
                            </td>

                            <td>
                                <span
                                    class="status"
                                    class:active-status={discount.active}
                                    class:inactive-status={!discount.active}
                                >
                                    {discount.active
                                        ? 'Active'
                                        : 'Inactive'}
                                </span>
                            </td>

                            <td>

                                <div class="row-actions">

                                    <button
                                        type="button"
                                        class="secondary-button"
                                        onclick={() => editDiscount(discount)}
                                        disabled={saving}
                                    >
                                        Edit
                                    </button>

                                    <button
                                        type="button"
                                        class="toggle-button"
                                        class:deactivate={discount.active}
                                        onclick={() => toggleDiscount(discount)}
                                        disabled={saving}
                                    >
                                        {discount.active
                                            ? 'Deactivate'
                                            : 'Activate'}
                                    </button>

                                </div>

                            </td>

                        </tr>

                    {:else}

                        <tr>
                            <td colspan="6" class="empty">
                                No discount codes found.
                            </td>
                        </tr>

                    {/each}

                </tbody>

            </table>

        </div>

    {/if}

</section>

<style>
    .discount-admin {
        padding: 0;
        color: #171717;
    }

    h2 {
        margin: 0 0 12px;
        font-size: 1.6rem;
    }

    h3 {
        margin: 0 0 16px;
        font-size: 1.2rem;
    }

    .description {
        color: #666;
        margin-bottom: 24px;
    }

    .stats {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 16px;
        margin-bottom: 24px;
    }

    .stat-card {
        background: #f7f7f7;
        border: 1px solid #ddd;
        border-radius: 6px;
        padding: 20px;
    }

    .stat-card span {
        display: block;
        color: #666;
        margin-bottom: 10px;
        font-size: 0.9rem;
    }

    .stat-card strong {
        font-size: 1.8rem;
    }

    .discount-form {
        background: white;
        border: 1px solid #ccc;
        border-radius: 6px;
        padding: 24px;
        margin-bottom: 32px;
    }

    .form-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 18px;
        margin-bottom: 18px;
    }

    label {
        display: flex;
        flex-direction: column;
        gap: 8px;
        font-weight: 600;
    }

    input,
    select {
        box-sizing: border-box;
        width: 100%;
        padding: 11px 12px;
        border: 1px solid #aaa;
        border-radius: 4px;
        background: white;
        color: #171717;
        font: inherit;
        font-weight: normal;
    }

    .checkbox-label {
        display: flex;
        flex-direction: row;
        align-items: center;
        margin-bottom: 20px;
    }

    .checkbox-label input {
        width: auto;
    }

    .form-actions,
    .row-actions {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
    }

    button {
        padding: 10px 15px;
        border-radius: 4px;
        cursor: pointer;
        font: inherit;
        white-space: nowrap;
    }

    button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .primary-button {
        background: #333;
        color: white;
        border: 1px solid #333;
    }

    .secondary-button {
        background: white;
        color: #222;
        border: 1px solid #aaa;
    }

    .toggle-button {
        background: white;
        color: #26723c;
        border: 1px solid #9ac8a6;
    }

    .toggle-button.deactivate {
        color: #b32626;
        border-color: #e0aaaa;
    }

    .table-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        margin-bottom: 20px;
    }

    .table-header h3 {
        margin: 0;
    }

    .table-header input {
        max-width: 330px;
    }

    .table-wrapper {
        overflow-x: auto;
    }

    table {
        width: 100%;
        border-collapse: collapse;
        background: white;
    }

    th,
    td {
        border: 1px solid #ddd;
        padding: 13px;
        text-align: left;
    }

    th {
        background: #ededed;
        font-weight: 600;
    }

    .status {
        display: inline-block;
        padding: 6px 10px;
        border-radius: 4px;
        font-size: 0.85rem;
    }

    .active-status {
        background: #e1f2e5;
        color: #26723c;
    }

    .inactive-status {
        background: #ededed;
        color: #666;
    }

    .message {
        padding: 12px 16px;
        border-radius: 4px;
        margin-bottom: 16px;
    }

    .error {
        background: #fce8e8;
        color: #a52222;
    }

    .success {
        background: #e1f2e5;
        color: #26723c;
    }

    .empty {
        text-align: center;
        color: #666;
        padding: 24px;
    }

    @media (max-width: 750px) {
        .stats,
        .form-grid {
            grid-template-columns: 1fr;
        }

        .table-header {
            align-items: stretch;
            flex-direction: column;
        }

        .table-header input {
            max-width: none;
        }
    }
</style>