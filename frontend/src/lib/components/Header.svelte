
<header>
    <a href="/" class="brand">Viral Vault</a>

    <nav aria-label="Main navigation">
        <a href="/">Home</a>
        <a href="/shop">Shop</a>
        <a href="/cart">Cart</a>
        <a href="/login">Login / Register</a>
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

    nav a {
        color: var(--color-text);
        text-decoration: none;
        font-weight: bold;
    }

    nav a:hover {
        text-decoration: underline;
    }
</style>
