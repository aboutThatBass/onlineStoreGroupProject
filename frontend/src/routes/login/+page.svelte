
<script lang="ts">
    import { goto } from '$app/navigation';
    import { mergeGuestCart } from '../../lib/data/cart';

    let email = $state('');
    let password = $state('');
    let errorMessage = $state('');

    async function handleLogin(event: SubmitEvent) {
        event.preventDefault();
        errorMessage = '';

        try {
            const response = await fetch('http://localhost:8080/api/users/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({ email, password })
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
        }
    }
</script>

<svelte:head>
    <title>Login | Viral Vault</title>
</svelte:head>

<main>
    <h1>Welcome Back!</h1>
    <p>Log in to your Viral Vault account.</p>

    <form onsubmit={handleLogin}>
        <label for="email">Email</label>
        <input
            type="email"
            id="email"
            name="email"
            placeholder="Enter your email"
            bind:value={email}
        />

        <label for="password">Password</label>
        <input
            type="password"
            id="password"
            name="password"
            placeholder="Enter your password"
            bind:value={password}
        />

        {#if errorMessage}
            <p role="alert">{errorMessage}</p>
        {/if}

        <button type="submit">Log In</button>
    </form>

    <p>
        Don't have an account?
        <a href="/register">Register here</a>
    </p>
</main>
