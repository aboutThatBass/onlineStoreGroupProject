<script lang="ts">
    import{ ShoppingCart, Menu } from '@lucide/svelte';
    let menuOpen = $state(false);
</script>

<header>
    <div class="menu-container">
        <button
            class="menu-toggle"
            aria-label="Toggle navigation menu"
            aria-expanded={menuOpen}
            onclick={() => menuOpen = !menuOpen}
        >
            <Menu size={28} strokeWidth={2} />
        </button>

        {#if menuOpen}
            <nav aria-label="Main navigation">
                <a href="/">Shop</a>
                <a href="/login">Login / Register</a>
            </nav>
        {/if}
    </div>

    <a href="/" class="brand">Viral Vault</a>

    <a href="/cart" class="cart-link" aria-label="Shopping cart">
        <ShoppingCart size={28} strokeWidth={2} />
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

    nav {
        position: absolute;
        top: 100%;
        left: 0;
        z-index: 10;

        display: flex;
        flex-direction: column;
        gap: 1rem;

        background-color: var(--color-accent);
        border: 2px solid var(--color-border);
        border-radius: 8px;
        padding: 1rem;
        min-width: 180px;
    }

    nav a {
        color: var(--color-text);
        text-decoration: none;
        font-weight: bold;
    }

    nav a:hover {
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
        margin-left: auto;

        color: var(--color-text);
        cursor: pointer;
        text-decoration: none;

        transition: background-color 0.2s ease;
    }

    .menu-toggle:hover,
    .cart-link:hover {
        background-color: #d88f83;
    }

    .menu-container {
        position: relative;
    }
</style>
