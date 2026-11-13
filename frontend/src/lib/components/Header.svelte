
<script lang="ts">
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';
    import { ShoppingCart, Menu } from '@lucide/svelte';
    import { cart } from '../stores/cart.svelte';

    let menuOpen = $state(false);
    let loggedIn = $state(false);

    onMount(async () => {
        try {
            const response = await fetch(
                'http://localhost:8080/api/users/session',
                { credentials: 'include' }
            );

            loggedIn = response.ok;
        } catch {
            loggedIn = false;
        }
    });

    function openMenu() {
        menuOpen = true;
    }

    function closeMenu() {
        menuOpen = false;
    }

    function toggleMenu() {
        menuOpen = !menuOpen;
    }

    function handleMenuKeydown(event: KeyboardEvent) {
        if (event.key === 'Escape') {
            closeMenu();
        }
    }

    function handleMenuFocusOut(event: FocusEvent) {
        const container = event.currentTarget as HTMLElement;
        const nextElement = event.relatedTarget as Node | null;

        if (!nextElement || !container.contains(nextElement)) {
            closeMenu();
        }
    }

    async function handleLogout() {
        try {
            const response = await fetch(
                'http://localhost:8080/api/users/logout',
                {
                    method: 'POST',
                    credentials: 'include'
                }
            );

            if (!response.ok) {
                alert('Unable to log out. Please try again.');
                return;
            }

            loggedIn = false;
            closeMenu();

            await cart.load();

            await goto('/');
        } catch {
            alert('Unable to connect to the server.');
        }
    }
</script>

<header>
    <div
        class="menu-container"
        onpointerenter={(event) => {
            if (event.pointerType === 'mouse') {
                openMenu();
            }
        }}
        onpointerleave={(event) => {
            if (event.pointerType === 'mouse') {
                closeMenu();
            }
        }}
        onfocusout={handleMenuFocusOut}
        onkeydown={handleMenuKeydown}
        role="group"
        aria-label="Navigation menu"
    >
        <button
            class="menu-toggle"
            aria-label="Toggle navigation menu"
            aria-expanded={menuOpen}
            aria-controls="main-navigation"
            onclick={toggleMenu}
        >
            <Menu size={28} strokeWidth={2} />
        </button>

        {#if menuOpen}
            <nav
                id="main-navigation"
                aria-label="Main navigation"
            >
                <a
                    href="/"
                    onclick={closeMenu}
                >
                    Shop
                </a>

                {#if loggedIn}
                    <a
                        href="/profile"
                        onclick={closeMenu}
                    >
                        My Profile
                    </a>

                    <a
                        href="/orders"
                        onclick={closeMenu}
                    >
                        My Orders
                    </a>

                    <button
                        class="logout-button"
                        onclick={handleLogout}
                    >
                        Logout
                    </button>
                {:else}
                    <a
                        href="/login"
                        onclick={closeMenu}
                    >
                        Login / Register
                    </a>
                {/if}
            </nav>
        {/if}
    </div>

    <a href="/" class="brand">Viral Vault</a>

    <a
        href="/cart"
        class="cart-link"
        aria-label="Shopping cart"
    >
        <ShoppingCart size={28} strokeWidth={2} />

        {#if cart.hasItems}
            <span class="cart-dot"></span>
        {/if}
    </a>
</header>

<style>
    header {
        display: flex;
        justify-content: flex-start;
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

    .menu-container {
        position: relative;
    }

    nav {
        position: absolute;
        top: 100%;
        left: 0;
        z-index: 100;

        display: flex;
        flex-direction: column;
        gap: 1rem;

        background-color: var(--color-accent);
        border: 2px solid var(--color-border);
        border-radius: 8px;
        padding: 1rem;
        min-width: 180px;

        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
    }

    nav a {
        color: var(--color-text);
        text-decoration: none;
        font-weight: bold;
    }

    nav a:hover {
        text-decoration: underline;
    }

    .logout-button {
        background: transparent;
        border: none;
        padding: 0;
        color: var(--color-text);
        font: inherit;
        font-weight: bold;
        text-align: left;
        cursor: pointer;
    }

    .logout-button:hover {
        text-decoration: underline;
    }

    .menu-toggle,
    .cart-link {
        background: transparent;
        border: none;
        border-radius: 50%;

        width: 44px;
        height: 44px;

        display: flex;
        align-items: center;
        justify-content: center;

        color: var(--color-text);
        cursor: pointer;
        text-decoration: none;

        transition: background-color 0.2s ease;
    }

    .menu-toggle:hover,
    .cart-link:hover {
        background-color: #d88f83;
    }

    .menu-toggle:focus-visible,
    .cart-link:focus-visible {
        outline: 2px solid var(--color-text);
        outline-offset: 3px;
    }

    .cart-link {
        position: relative;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        margin-left: auto;
    }

    .cart-dot {
        position: absolute;
        bottom: 0.5px;
        left: 0.5px;
        width: 12px;
        height: 12px;
        background-color: #dc2626;
        border: 2px solid black;
        border-radius: 50%;
        box-sizing: content-box;
    }
</style>
