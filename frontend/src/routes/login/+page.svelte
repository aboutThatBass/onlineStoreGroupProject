
<script lang="ts">
    import { mergeGuestCart } from '../../lib/data/cart';
    import { Mail, LockKeyhole, LogIn, ArrowRight } from '@lucide/svelte';

    let email = $state('');
    let password = $state('');
    let errorMessage = $state('');
    let loading = $state(false);

    async function handleLogin(event: SubmitEvent) {
        event.preventDefault();

        errorMessage = '';
        loading = true;

        try {
            const response = await fetch('http://localhost:8080/api/users/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    email: email.trim(),
                    password
                })
            });

            const data = await response.json();

            if (!response.ok || !data.success) {
                errorMessage = data.message || 'Login failed.';
                return;
            }

            try {
                await mergeGuestCart();
            } catch (error) {
                console.error('Unable to merge guest cart:', error);
            }

            window.location.href = '/';
        } catch {
            errorMessage = 'Unable to connect to the server.';
        } finally {
            loading = false;
        }
    }
</script>

<svelte:head>
    <title>Login | Viral Vault</title>
</svelte:head>

<main class="login-page">
    <section class="login-card" aria-labelledby="login-title">

        <div class="icon-circle">
            <LogIn size={34} strokeWidth={2} />
        </div>

        <h1 id="login-title">Welcome Back!</h1>
        <p class="subtitle">
            Log in to your Viral Vault account.
        </p>

        <form onsubmit={handleLogin}>

            <div class="form-group">
                <label for="email">Email</label>

                <div class="input-wrapper">
                    <Mail size={20} />

                    <input
                        type="email"
                        id="email"
                        name="email"
                        placeholder="Enter your email"
                        autocomplete="email"
                        bind:value={email}
                        required
                    />
                </div>
            </div>

            <div class="form-group">
                <label for="password">Password</label>

                <div class="input-wrapper">
                    <LockKeyhole size={20} />

                    <input
                        type="password"
                        id="password"
                        name="password"
                        placeholder="Enter your password"
                        autocomplete="current-password"
                        bind:value={password}
                        required
                    />
                </div>
            </div>

            {#if errorMessage}
                <div class="error-message" role="alert">
                    {errorMessage}
                </div>
            {/if}

            <button
                type="submit"
                class="login-button"
                disabled={loading}
            >
                {loading ? 'Logging In...' : 'Log In'}

                {#if !loading}
                    <ArrowRight size={20} />
                {/if}
            </button>
        </form>

        <div class="card-footer">
            <p>
                Don't have an account?
                <a href="/register">Register here</a>
            </p>
        </div>

    </section>
</main>

<style>
    .login-page {
        min-height: 75vh;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 3.5rem 1rem;
        background: #ffedad;
        color: #40342e;
        font-family: var(--font-body, 'Nunito', sans-serif);
    }

    .login-card {
        width: 100%;
        max-width: 460px;
        padding: 2.5rem;
        background: #fffdf7;
        border: 3px solid #40342e;
        border-radius: 18px;
        box-shadow: 7px 7px 0 #40342e;
        text-align: center;
    }

    .icon-circle {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 76px;
        height: 76px;
        margin: 0 auto 1.25rem;
        background: #f9dc7a;
        border: 2px solid #40342e;
        border-radius: 50%;
    }

    h1 {
        margin: 0.5rem 0;
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 2.3rem;
        font-weight: 700;
        line-height: 1.2;
    }

    .subtitle {
        margin: 0.75rem 0 2rem;
        color: #73645a;
        font-size: 1rem;
    }

    form {
        display: flex;
        flex-direction: column;
        gap: 1.25rem;
        text-align: left;
    }

    .form-group {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    label {
        font-weight: 800;
        font-size: 1rem;
    }

    .input-wrapper {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 0.9rem 1rem;
        background: white;
        border: 2px solid #40342e;
        border-radius: 10px;
        transition: outline 0.2s ease;
    }

    .input-wrapper :global(svg) {
        flex-shrink: 0;
        color: #73645a;
    }

    input {
        width: 100%;
        min-width: 0;
        padding: 0;
        border: none;
        outline: none;
        background: transparent;
        color: #40342e;
        font: inherit;
        font-size: 1rem;
    }

    input::placeholder {
        color: #94867c;
    }

    .input-wrapper:focus-within {
        outline: 2px solid #e8a598;
        outline-offset: 2px;
    }

    .login-button {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.6rem;
        width: 100%;
        margin-top: 0.5rem;
        padding: 0.9rem 1rem;
        background: #e8a598;
        color: #40342e;
        border: 2px solid #40342e;
        border-radius: 10px;
        box-shadow: 3px 3px 0 #40342e;
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 1.2rem;
        font-weight: 700;
        cursor: pointer;
        transition: background 0.2s ease, transform 0.2s ease;
    }

    .login-button:hover:not(:disabled) {
        background: #f2b8ad;
        transform: translateY(-2px);
    }

    .login-button:active:not(:disabled) {
        transform: translateY(1px);
        box-shadow: 1px 1px 0 #40342e;
    }

    .login-button:disabled {
        opacity: 0.65;
        cursor: wait;
    }

    .error-message {
        padding: 0.8rem 1rem;
        background: #fff0ee;
        color: #b42318;
        border: 1px solid #e8a598;
        border-radius: 8px;
        font-size: 0.95rem;
        font-weight: 700;
    }

    .card-footer {
        margin-top: 2rem;
        padding-top: 1.5rem;
        border-top: 1px solid #e9ded2;
    }

    .card-footer p {
        margin: 0;
        color: #73645a;
        font-size: 0.95rem;
    }

    .card-footer a {
        color: #40342e;
        font-weight: 800;
        text-decoration: underline;
        text-decoration-color: #e8a598;
        text-decoration-thickness: 2px;
        text-underline-offset: 3px;
    }

    .card-footer a:hover {
        color: #b36f63;
    }

    @media (max-width: 480px) {
        .login-page {
            padding: 2rem 1rem;
        }

        .login-card {
            padding: 2rem 1.4rem;
            box-shadow: 5px 5px 0 #40342e;
        }

        h1 {
            font-size: 1.9rem;
        }
    }
</style>
