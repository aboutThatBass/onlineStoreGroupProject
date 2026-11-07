
<script lang="ts">
    import { onMount } from 'svelte';
    import { cart } from '../stores/cart';

    let isLoggedIn = $state(false);

    onMount(async () => {
        try {
            const response = await fetch('http://localhost:8080/api/users/session', {
                credentials: 'include'
            });

            isLoggedIn = response.ok;
        } catch (error) {
            isLoggedIn = false;
        }
    });

    async function logout() {
        try {
            const response = await fetch('http://localhost:8080/api/users/logout', {
                method: 'POST',
                credentials: 'include'
            });

            if (response.ok) {
                cart.reset();
                isLoggedIn = false;
                window.location.href = '/';
            } else {
                alert('Unable to log out. Please try again.');
            }
        } catch (error) {
            alert('Unable to connect to the server.');
        }
    }
</script>

<header>
    <a href="/" class="brand">Viral Vault</a>

    <nav aria-label="Main navigation">
        <a href="/">Home</a>
        <a href="/shop">Shop</a>
        <a href="/cart">Cart</a>
        {#if isLoggedIn}
            <button type="button" onclick={logout}>Logout</button>
        {:else}
            <a href="/login">Login / Register</a>
        {/if}
    </nav>
</header>

<style>
    header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 1rem;
        padding: 1rem 2rem;

        background-color: var(--color-accent);
        border-bottom: 3px solid var(--color-border);
    }

    .brand {
        font-size: 1.75rem;
        font-weight: bold;
        text-decoration: none;
        color: var(--color-text);
    }

    nav {
        display: flex;
        flex-wrap: wrap;
        gap: 1.5rem;
    }

    nav a,
    nav button {
        color: var(--color-text);
        text-decoration: none;
        font-weight: bold;
    }

    nav a:hover,
    nav button:hover {
        text-decoration: underline;
    }

    nav button {
        background: none;
        border: none;
        padding: 0;
        font: inherit;
        cursor: pointer;
    }
</style>
