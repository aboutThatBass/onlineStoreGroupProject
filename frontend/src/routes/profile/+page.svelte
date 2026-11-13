
<script lang="ts">
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';

    type Profile = {
        username: string;
        fullName: string;
        email: string;
        phoneNumber: string;
        address: string;
        city: string;
        state: string;
        zipCode: string;
    };

    const API_URL = 'http://localhost:8080/api/users/profile';

    let profile = $state<Profile>({
        username: '',
        fullName: '',
        email: '',
        phoneNumber: '',
        address: '',
        city: '',
        state: '',
        zipCode: ''
    });

    let originalProfile = $state<Profile>({
        username: '',
        fullName: '',
        email: '',
        phoneNumber: '',
        address: '',
        city: '',
        state: '',
        zipCode: ''
    });

    let loading = $state(true);
    let saving = $state(false);
    let editing = $state(false);
    let error = $state('');
    let success = $state('');

    onMount(async () => {
        await loadProfile();
    });

    async function loadProfile() {
        loading = true;
        error = '';

        try {
            const response = await fetch(API_URL, {
                credentials: 'include'
            });

            if (response.status === 401) {
                await goto('/login');
                return;
            }

            if (!response.ok) {
                throw new Error('Unable to load your profile.');
            }

            const data: Profile = await response.json();

            profile = { ...data };
            originalProfile = { ...data };
        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Something went wrong.';
        } finally {
            loading = false;
        }
    }

    function startEditing() {
        profile = { ...originalProfile };
        editing = true;
        error = '';
        success = '';
    }

    function cancelEditing() {
        profile = { ...originalProfile };
        editing = false;
        error = '';
    }

    async function saveProfile(event: SubmitEvent) {
        event.preventDefault();

        saving = true;
        error = '';
        success = '';

        try {
            const response = await fetch(API_URL, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    fullName: profile.fullName,
                    email: profile.email,
                    phoneNumber: profile.phoneNumber,
                    address: profile.address,
                    city: profile.city,
                    state: profile.state,
                    zipCode: profile.zipCode
                })
            });

            const data = await response.json();

            if (response.status === 401) {
                await goto('/login');
                return;
            }

            if (!response.ok) {
                throw new Error(
                    data.message || 'Unable to save profile.'
                );
            }

            profile = { ...data };
            originalProfile = { ...data };
            editing = false;
            success = 'Your profile has been updated successfully!';
        } catch (err) {
            error = err instanceof Error
                ? err.message
                : 'Something went wrong.';
        } finally {
            saving = false;
        }
    }
</script>

<svelte:head>
    <title>My Profile | Viral Vault</title>
</svelte:head>

<main class="profile-page">
    <div class="profile-card">
        <h1>My Profile</h1>
        <p class="subtitle">
            View and update your Viral Vault account information.
        </p>

        {#if loading}
            <p>Loading your profile...</p>
        {:else}
            {#if error}
                <p class="message error" role="alert">{error}</p>
            {/if}

            {#if success}
                <p class="message success" role="status">{success}</p>
            {/if}

            <form onsubmit={saveProfile}>
                <div class="field">
                    <label for="username">Username</label>
                    <input
                        id="username"
                        value={profile.username}
                        disabled
                    />
                    <small>Username cannot be changed.</small>
                </div>

                <div class="field">
                    <label for="fullName">Full Name</label>
                    <input
                        id="fullName"
                        bind:value={profile.fullName}
                        disabled={!editing}
                        maxlength="100"
                    />
                </div>

                <div class="field">
                    <label for="email">Email Address</label>
                    <input
                        id="email"
                        type="email"
                        bind:value={profile.email}
                        disabled={!editing}
                        maxlength="100"
                        required
                    />
                </div>

                <div class="field">
                    <label for="phone">Phone Number</label>
                    <input
                        id="phone"
                        type="tel"
                        bind:value={profile.phoneNumber}
                        disabled={!editing}
                        required
                    />
                </div>

                <div class="field">
                    <label for="address">Street Address</label>
                    <input
                        id="address"
                        bind:value={profile.address}
                        disabled={!editing}
                        maxlength="255"
                    />
                </div>

                <div class="field-row">
                    <div class="field">
                        <label for="city">City</label>
                        <input
                            id="city"
                            bind:value={profile.city}
                            disabled={!editing}
                            maxlength="100"
                        />
                    </div>

                    <div class="field">
                        <label for="state">State</label>
                        <input
                            id="state"
                            bind:value={profile.state}
                            disabled={!editing}
                            maxlength="50"
                        />
                    </div>
                </div>

                <div class="field">
                    <label for="zip">ZIP Code</label>
                    <input
                        id="zip"
                        bind:value={profile.zipCode}
                        disabled={!editing}
                        maxlength="20"
                    />
                </div>

                <div class="actions">
                    {#if editing}
                        <button
                            type="submit"
                            class="primary-button"
                            disabled={saving}
                        >
                            {saving ? 'Saving...' : 'Save Changes'}
                        </button>

                        <button
                            type="button"
                            class="secondary-button"
                            onclick={cancelEditing}
                            disabled={saving}
                        >
                            Cancel
                        </button>
                    {:else}
                        <button
                            type="button"
                            class="primary-button"
                            onclick={startEditing}
                        >
                            Edit Profile
                        </button>
                    {/if}
                </div>
            </form>
        {/if}
    </div>
</main>

<style>
    .profile-page {
        min-height: 80vh;
        padding: 3rem 1rem;
        display: flex;
        justify-content: center;
        align-items: flex-start;
    }

    .profile-card {
        width: 100%;
        max-width: 650px;
        background: #fffdf7;
        border: 2px solid var(--color-border);
        border-radius: 12px;
        padding: 2rem;
        box-shadow: 4px 4px 0 rgba(0, 0, 0, 0.08);
    }

    h1 {
        margin-top: 0;
        color: var(--color-text);
        font-size: 2rem;
    }

    .subtitle {
        margin-bottom: 2rem;
        color: #666;
    }

    form {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .field {
        display: flex;
        flex-direction: column;
        gap: 0.4rem;
        flex: 1;
    }

    .field-row {
        display: flex;
        gap: 1rem;
    }

    label {
        font-weight: 600;
        color: var(--color-text);
    }

    input {
        padding: 0.8rem;
        border: 2px solid var(--color-border);
        border-radius: 7px;
        font: inherit;
        background: white;
        color: var(--color-text);
        width: 100%;
        box-sizing: border-box;
    }

    input:disabled {
        background: #f1f0eb;
        color: #555;
        cursor: default;
    }

    small {
        color: #777;
    }

    .actions {
        display: flex;
        gap: 1rem;
        margin-top: 1rem;
    }

    button {
        padding: 0.85rem 1.5rem;
        border: 2px solid var(--color-border);
        border-radius: 8px;
        font: inherit;
        font-weight: bold;
        cursor: pointer;
    }

    .primary-button {
        background: var(--color-accent);
        color: var(--color-text);
    }

    .secondary-button {
        background: white;
        color: var(--color-text);
    }

    button:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .message {
        padding: 0.8rem;
        border-radius: 6px;
        margin-bottom: 1rem;
    }

    .error {
        background: #fee2e2;
        color: #991b1b;
    }

    .success {
        background: #dcfce7;
        color: #166534;
    }

    @media (max-width: 550px) {
        .profile-card {
            padding: 1.25rem;
        }

        .field-row {
            flex-direction: column;
        }

        .actions {
            flex-direction: column;
        }
    }
</style>
