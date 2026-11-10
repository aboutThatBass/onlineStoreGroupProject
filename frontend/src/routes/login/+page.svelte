
<script lang="ts">
    import { cart } from '../../lib/stores/cart';

    let email = $state('');
    let password = $state('');

    let errorMessage = $state('');
    let successMessage = $state('');
    let isSubmitting = $state(false);

    async function handleLogin(event: SubmitEvent) {
        event.preventDefault();

        errorMessage = '';
        successMessage = '';
        isSubmitting = true;

        try {
            const response = await fetch('http://localhost:8080/api/users/login', {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    email,
                    password
                })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                password = '';

                await cart.loadCart();

                window.location.href = '/';
            } else {
                errorMessage = data.message ?? 'Invalid email or password.';
            }
        } catch (error) {
            errorMessage = 'Unable to connect to the server. Please try again later.';
        } finally {
            isSubmitting = false;
        }
    }
</script>

<svelte:head>
    <title>Login | Viral Vault</title>
</svelte:head>

<main>
    <h1>Welcome Back!</h1>
    <p>Log in to your Viral Vault account.</p>

    {#if successMessage}
        <p role="status" style="color: green;">
            {successMessage}
        </p>
    {/if}

    {#if errorMessage}
        <p role="alert" style="color: red;">
            {errorMessage}
        </p>
    {/if}

    <form onsubmit={handleLogin}>
        <label for="email">Email</label>
        <input
            type="email"
            id="email"
            name="email"
            placeholder="Enter your email"
            bind:value={email}
            required
        />

        <label for="password">Password</label>
        <input
            type="password"
            id="password"
            name="password"
            placeholder="Enter your password"
            bind:value={password}
            required
        />

        <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Logging In...' : 'Log In'}
        </button>
    </form>

    <p>
        Don't have an account?
        <a href="/register">Register here</a>
    </p>
</main>
