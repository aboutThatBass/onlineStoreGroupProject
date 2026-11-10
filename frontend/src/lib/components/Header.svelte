
<script lang="ts">
    import { onMount } from 'svelte';
    import { page } from '$app/state';
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
        <a
            href="/"
            class:active={page.url.pathname === '/'}
            aria-current={page.url.pathname === '/' ? 'page' : undefined}
        >
            Home
        </a>

        <a
            href="/shop"
            class:active={page.url.pathname.startsWith('/shop')}
            aria-current={page.url.pathname.startsWith('/shop') ? 'page' : undefined}
        >
            Shop
        </a>

        <a
            href="/cart"
            class:active={page.url.pathname.startsWith('/cart')}
            aria-current={page.url.pathname.startsWith('/cart') ? 'page' : undefined}
        >
            Cart
        </a>

        {#if isLoggedIn}
            <button type="button" onclick={logout}>
                Logout
            </button>
        {:else}
            <a
                href="/login"
                class:active={page.url.pathname === '/login' || page.url.pathname === '/register'}
                aria-current={page.url.pathname === '/login' || page.url.pathname === '/register' ? 'page' : undefined}
            >
                Login / Register
            </a>
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
        align-items: center;
    }

    nav a,
    nav button {
        color: var(--color-text);
        text-decoration: none;
        font-weight: bold;
        font-size: inherit;
        font-family: inherit;
        line-height: inherit;
    }

    nav a:hover,
    nav button:hover {
        text-decoration: underline;
    }

    nav a.active {
        text-decoration: underline;
        text-decoration-thickness: 2px;
        text-underline-offset: 5px;
    }

    nav button {
        background: none;
        border: none;
        padding: 0;
        cursor: pointer;
    }
</style>
