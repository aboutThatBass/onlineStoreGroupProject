
<script lang="ts">
    import { goto } from '$app/navigation';
    import { LockKeyhole, UserRound, ArrowLeft } from '@lucide/svelte';

    let username = $state('');
    let password = $state('');
    let error = $state('');
    let loading = $state(false);

    async function handleLogin(event: SubmitEvent) {
        event.preventDefault();

        error = '';
        loading = true;

        try {
            const response = await fetch(
                'http://localhost:8080/api/admin/login',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include',
                    body: JSON.stringify({
                        username: username.trim(),
                        password
                    })
                }
            );

            if (!response.ok) {
                if (response.status === 401) {
                    error = 'Invalid administrator username or password.';
                } else if (response.status === 400) {
                    error = 'Please enter your username and password.';
                } else {
                    error = 'Unable to sign in. Please try again.';
                }
                return;
            }

            const data = await response.json();

            if (!data.success || !data.isAdmin) {
                error = 'Administrator access required.';
                return;
            }

            await goto('/admin');

        } catch (err) {
            console.error('Admin login error:', err);
            error = 'Unable to connect to the server.';
        } finally {
            loading = false;
        }
    }
</script>

<svelte:head>
    <title>Admin Login | Viral Vault</title>
</svelte:head>

<div class="admin-login-page">
    <div class="login-card">

        <div class="icon-circle">
            <LockKeyhole size={35} strokeWidth={2} />
        </div>

        <h1>Admin Login</h1>
        <p>Sign in to manage the Viral Vault.</p>

        <form onsubmit={handleLogin}>

            <label for="admin-username">Username</label>

            <div class="input-wrapper">
                <UserRound size={20} />

                <input
                    id="admin-username"
                    type="text"
                    placeholder="Admin username"
                    autocomplete="username"
                    bind:value={username}
                    required
                />
            </div>

            <label for="admin-password">Password</label>

            <div class="input-wrapper">
                <LockKeyhole size={20} />

                <input
                    id="admin-password"
                    type="password"
                    placeholder="Admin password"
                    autocomplete="current-password"
                    bind:value={password}
                    required
                />
            </div>

            {#if error}
                <div class="error-message" role="alert">
                    {error}
                </div>
            {/if}

            <button
                type="submit"
                class="login-button"
                disabled={loading}
            >
                {loading ? 'Signing In...' : 'Sign In'}
            </button>

        </form>

        <a href="/" class="back-link">
            <ArrowLeft size={18} />
            Back to Shop
        </a>

    </div>
</div>

<style>
    .admin-login-page {
        min-height: 75vh;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 3rem 1rem;
        background: #ffedad;
        font-family: var(--font-body, 'Nunito', sans-serif);
        color: #40342e;
    }

    .login-card {
        width: 100%;
        max-width: 440px;
        background: #fffdf7;
        border: 3px solid #40342e;
        border-radius: 18px;
        box-shadow: 7px 7px 0 #40342e;
        padding: 2.5rem;
        text-align: center;
    }

    .icon-circle {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 75px;
        height: 75px;
        margin: 0 auto 1rem;
        background: #f9dc7a;
        border: 2px solid #40342e;
        border-radius: 50%;
    }

    h1 {
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 2.2rem;
        margin: 0.5rem 0;
    }

    p {
        margin-bottom: 2rem;
        color: #73645a;
    }

    form {
        display: flex;
        flex-direction: column;
        text-align: left;
        gap: 0.6rem;
    }

    label {
        font-weight: 800;
        margin-top: 0.5rem;
    }

    .input-wrapper {
        display: flex;
        align-items: center;
        gap: 0.7rem;
        background: white;
        border: 2px solid #40342e;
        border-radius: 10px;
        padding: 0.8rem 1rem;
    }

    input {
        width: 100%;
        min-width: 0;
        border: none;
        outline: none;
        font: inherit;
        background: transparent;
        color: #40342e;
    }

    .input-wrapper:focus-within {
        outline: 2px solid #e8a598;
        outline-offset: 2px;
    }

    .login-button {
        margin-top: 1.5rem;
        padding: 0.9rem;
        border: 2px solid #40342e;
        border-radius: 10px;
        background: #e8a598;
        box-shadow: 3px 3px 0 #40342e;
        color: #40342e;
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 1.2rem;
        font-weight: 700;
        cursor: pointer;
        transition: background 0.2s ease;
    }

    .login-button:hover:not(:disabled) {
        background: #f2b8ad;
    }

    .login-button:disabled {
        opacity: 0.6;
        cursor: wait;
    }

    .error-message {
        color: #b42318;
        background: #fff0ee;
        padding: 0.8rem;
        border-radius: 8px;
        margin-top: 0.5rem;
        font-weight: 700;
    }

    .back-link {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        margin-top: 1.8rem;
        color: #40342e;
        font-weight: 700;
        text-decoration: none;
    }

    .back-link:hover {
        text-decoration: underline;
    }
</style>
