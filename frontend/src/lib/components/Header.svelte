
<script lang="ts">
    import { onMount, tick } from 'svelte';
    import { goto } from '$app/navigation';
    import { page } from '$app/state';
    import {
        ShoppingCart,
        Menu,
        UserRound,
        Search,
        ArrowLeft,
        X
    } from '@lucide/svelte';
    import { cart } from '../stores/cart.svelte';

    let menuOpen = $state(false);
    let profileOpen = $state(false);
    let searchOpen = $state(false);
    let headerSearch = $state('');
    let loggedIn = $state(false);
    let searchInput = $state<HTMLInputElement | undefined>(undefined);

    let showBackToShop = $derived(
        page.url.pathname === '/cart' ||
        page.url.pathname === '/profile' ||
        page.url.pathname.startsWith('/profile/') ||
        page.url.pathname === '/orders'
    );

    onMount(() => {
        async function checkSession() {
            try {
                const response = await fetch(
                    'http://localhost:8080/api/users/session',
                    { credentials: 'include' }
                );

                loggedIn = response.ok;
            } catch {
                loggedIn = false;
            }
        }

        void checkSession();

        headerSearch =
            new URLSearchParams(window.location.search)
                .get('search') ?? '';
    });

    function closeMenus() {
        menuOpen = false;
        profileOpen = false;
    }

    function closeSearch() {
        searchOpen = false;
    }

    function handleKeydown(event: KeyboardEvent) {
        if (event.key === 'Escape') {
            closeMenus();
            closeSearch();
        }
    }

    function handleMenuFocusOut(event: FocusEvent) {
        const container = event.currentTarget as HTMLElement;
        const next = event.relatedTarget as Node | null;

        if (!next || !container.contains(next)) {
            menuOpen = false;
        }
    }

    function handleProfileFocusOut(event: FocusEvent) {
        const container = event.currentTarget as HTMLElement;
        const next = event.relatedTarget as Node | null;

        if (!next || !container.contains(next)) {
            profileOpen = false;
        }
    }

    function handleSearchFocusOut(event: FocusEvent) {
        const container = event.currentTarget as HTMLElement;
        const next = event.relatedTarget as Node | null;

        if (!next || !container.contains(next)) {
            searchOpen = false;
        }
    }

    function updateSearch(value: string) {
        headerSearch = value;

        if (page.url.pathname !== '/') {
            return;
        }

        const url = new URL(window.location.href);

        if (value.trim()) {
            url.searchParams.set('search', value);
        } else {
            url.searchParams.delete('search');
        }

        window.history.replaceState(
            window.history.state,
            '',
            url
        );

        window.dispatchEvent(
            new CustomEvent('viral-vault-search', {
                detail: value
            })
        );
    }

    async function openSearch() {
        closeMenus();

        if (page.url.pathname !== '/') {
            await goto(
                headerSearch.trim()
                    ? `/?search=${encodeURIComponent(headerSearch)}`
                    : '/'
            );
        }

        searchOpen = true;

        await tick();
        searchInput?.focus();
    }

    function toggleSearch() {
        if (searchOpen) {
            closeSearch();
        } else {
            void openSearch();
        }
    }

    async function backToShop() {
        closeMenus();
        closeSearch();
        headerSearch = '';

        await goto('/');
    }

    function handleCenterButtonClick() {
        if (showBackToShop) {
            void backToShop();
        } else {
            toggleSearch();
        }
    }

    function handleProfilePointerEnter(event: PointerEvent) {
        if (event.pointerType === 'mouse') {
            profileOpen = true;
        }
    }

    function handleProfilePointerLeave(event: PointerEvent) {
        if (event.pointerType === 'mouse') {
            profileOpen = false;
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
            closeMenus();
            closeSearch();

            await cart.load();
            await goto('/');
        } catch {
            alert('Unable to connect to the server.');
        }
    }
</script>

<svelte:window onkeydown={handleKeydown} />

<header class="site-header">
    <div class="header-inner">

        <!-- Mobile navigation -->
        <div
            class="mobile-menu-container"
            onfocusout={handleMenuFocusOut}
        >
            <button
                type="button"
                class="icon-button mobile-toggle"
                aria-label="Toggle navigation"
                aria-expanded={menuOpen}
                aria-controls="mobile-navigation"
                onclick={() => menuOpen = !menuOpen}
            >
                {#if menuOpen}
                    <X size={27} />
                {:else}
                    <Menu size={27} />
                {/if}
            </button>

            {#if menuOpen}
                <nav
                    id="mobile-navigation"
                    class="dropdown mobile-dropdown"
                    aria-label="Mobile navigation"
                >
                    <a href="/" onclick={closeMenus}>
                        Home
                    </a>

                    <a href="/#products" onclick={closeMenus}>
                        Shop
                    </a>

                    <button
                        type="button"
                        onclick={() => {
                            closeMenus();
                            void openSearch();
                        }}
                    >
                        Search Products
                    </button>

                    {#if loggedIn}
                        <a href="/profile" onclick={closeMenus}>
                            My Profile
                        </a>
                    {:else}
                        <a href="/login" onclick={closeMenus}>
                            Login / Register
                        </a>
                    {/if}

                    <!-- Always visible -->
                    <a href="/admin/login" onclick={closeMenus}>
                        Admin Dashboard
                    </a>

                    {#if loggedIn}
                        <button type="button" onclick={handleLogout}>
                            Logout
                        </button>
                    {/if}
                </nav>
            {/if}
        </div>

        <!-- Branding -->
        <a href="/" class="brand" onclick={closeMenus}>
            <img
                src="/images/viral-vault-logo.png"
                alt=""
                class="brand-image"
            />

            <span class="brand-name">Viral Vault</span>
        </a>

        <!-- Center search / back button -->
        <nav
            class="desktop-navigation"
            aria-label="Main navigation"
        >
            <div
                class="search-container"
                onfocusout={handleSearchFocusOut}
            >
                <button
                    type="button"
                    class="center-button"
                    class:back-button={showBackToShop}
                    aria-expanded={!showBackToShop ? searchOpen : undefined}
                    aria-controls={!showBackToShop ? 'header-search-panel' : undefined}
                    onclick={handleCenterButtonClick}
                >
                    {#if showBackToShop}
                        <ArrowLeft size={23} strokeWidth={2.5} />
                        <span>Back to Shop</span>
                    {:else}
                        <Search size={23} strokeWidth={2.5} />
                        <span>Search Products</span>
                    {/if}
                </button>

                {#if searchOpen && !showBackToShop}
                    <div
                        id="header-search-panel"
                        class="search-dropdown"
                    >
                        <div class="search-field">
                            <Search size={21} strokeWidth={2} />

                            <input
                                bind:this={searchInput}
                                type="search"
                                placeholder="Search products..."
                                aria-label="Search products"
                                value={headerSearch}
                                oninput={(event) =>
                                    updateSearch(
                                        event.currentTarget.value
                                    )
                                }
                            />

                            <button
                                type="button"
                                class="close-search"
                                aria-label="Close search"
                                onclick={closeSearch}
                            >
                                <X size={20} />
                            </button>
                        </div>
                    </div>
                {/if}
            </div>
        </nav>

        <!-- Cart and profile -->
        <div class="header-actions">
            <a
                href="/cart"
                class="icon-button cart-link"
                aria-label={`Shopping cart, ${cart.itemCount} items`}
            >
                <ShoppingCart size={40} strokeWidth={2} />

                {#if cart.itemCount > 0}
                    <span class="cart-dot">
                        {cart.itemCount}
                    </span>
                {/if}
            </a>

            <div
                class="profile-container"
                onpointerenter={handleProfilePointerEnter}
                onpointerleave={handleProfilePointerLeave}
                onfocusout={handleProfileFocusOut}
            >
                <button
                    type="button"
                    class="icon-button profile-toggle"
                    aria-label="Account menu"
                    aria-expanded={profileOpen}
                    aria-controls="profile-navigation"
                    onclick={() => profileOpen = !profileOpen}
                >
                    <UserRound size={40} strokeWidth={2} />
                </button>

                {#if profileOpen}
                    <nav
                        id="profile-navigation"
                        class="dropdown profile-dropdown"
                        aria-label="Account navigation"
                    >
                        {#if loggedIn}
                            <a
                                href="/profile"
                                onclick={closeMenus}
                            >
                                My Profile
                            </a>
                        {:else}
                            <a
                                href="/login"
                                onclick={closeMenus}
                            >
                                Login / Register
                            </a>
                        {/if}

                        <!-- Always visible -->
                        <a
                            href="/admin/login"
                            onclick={closeMenus}
                        >
                            Admin Dashboard
                        </a>

                        {#if loggedIn}
                            <button
                                type="button"
                                onclick={handleLogout}
                            >
                                Logout
                            </button>
                        {/if}
                    </nav>
                {/if}
            </div>
        </div>
    </div>

    <!-- Mobile search field -->
    {#if searchOpen && !showBackToShop}
        <div class="mobile-search-panel">
            <div class="search-field">
                <Search size={21} strokeWidth={2} />

                <input
                    type="search"
                    placeholder="Search products..."
                    aria-label="Search products"
                    value={headerSearch}
                    oninput={(event) =>
                        updateSearch(event.currentTarget.value)
                    }
                />

                <button
                    type="button"
                    class="close-search"
                    aria-label="Close search"
                    onclick={closeSearch}
                >
                    <X size={20} />
                </button>
            </div>
        </div>
    {/if}
</header>

<style>
    .site-header {
        width: 100%;
        background-color: var(--color-accent);
        border-bottom: 3px solid var(--color-border);
        position: relative;
        z-index: 100;
    }

    /* Responsive desktop header */

    .header-inner {
        display: grid;
        grid-template-columns:
            minmax(0, 1fr)
            auto
            minmax(0, 1fr);
        align-items: center;

        gap: clamp(1rem, 1.5vw, 2rem);

        width: 100%;
        max-width: 1600px;
        margin: 0 auto;

        padding:
            clamp(0.5rem, 0.7vw, 0.85rem)
            clamp(1.25rem, 2vw, 2.5rem);
    }

    /* Branding */

    .brand {
        display: flex;
        align-items: center;
        gap: clamp(0.65rem, 0.8vw, 1rem);
        justify-self: start;
        min-width: 0;
        text-decoration: none;
    }

    /*
     * Logo grows smoothly on wide screens.
     * Windowed: approximately 100px
     * Large desktop: up to 145px
     */
    .brand-image {
        width: clamp(100px, 7.55vw, 145px);
        height: clamp(100px, 7.55vw, 145px);
        object-fit: contain;
        flex-shrink: 0;
        transition: transform 0.2s ease;
    }

    /*
     * Brand text scales independently of the image.
     */
    .brand-name {
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        color: #ffdf78;

        font-size: clamp(2.4rem, 2.9vw, 3.5rem);
        font-weight: 700;
        letter-spacing: -0.03em;
        white-space: nowrap;

        -webkit-text-stroke: 1.4px #40241e;
        text-shadow: 3px 4px 0 #40241e;
    }

    .brand:hover .brand-image {
        transform: scale(1.04);
    }

    /* Center search / back button */

    .desktop-navigation {
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .search-container {
        position: relative;
        display: flex;
        align-items: center;
    }

    .center-button {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 0.75rem;

        min-width: 260px;
        padding: 0.9rem 1.8rem;

        background: #fffdf7;
        border: 2px solid var(--color-border);
        border-radius: 12px;

        color: var(--color-text);
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 1.2rem;
        font-weight: 600;
        white-space: nowrap;

        cursor: pointer;
        box-shadow: 3px 3px 0 var(--color-border);

        transition:
            background-color 0.2s ease,
            transform 0.2s ease,
            box-shadow 0.2s ease;
    }

    .center-button:hover {
        background: #fff0e8;
        transform: translateY(-2px);
        box-shadow: 3px 5px 0 var(--color-border);
    }

    .center-button:active {
        transform: translateY(2px);
        box-shadow: 1px 1px 0 var(--color-border);
    }

    .center-button.back-button {
        background: #fffdf7;
    }

    .center-button.back-button:hover {
        background: #fff0e8;
    }

    .center-button:focus-visible {
        outline: 3px solid #40342e;
        outline-offset: 5px;
    }

    /* Search dropdown */

    .search-dropdown {
        position: absolute;
        top: 100%;
        left: 50%;
        transform: translateX(-50%);
        z-index: 200;
        width: 370px;
        padding-top: 15px;
    }

    .search-field {
        display: flex;
        align-items: center;
        gap: 0.6rem;
        width: 100%;
        padding: 0.65rem 0.8rem;

        background: white;
        border: 2px solid var(--color-border);
        border-radius: 10px;

        color: #40241e;
        box-shadow: 0 5px 14px rgba(0, 0, 0, 0.15);
    }

    .search-field input {
        flex: 1;
        min-width: 0;
        padding: 0.2rem;
        border: none;
        outline: none;
        background: transparent;
        color: #333;
        font: inherit;
    }

    .close-search {
        display: flex;
        align-items: center;
        justify-content: center;
        background: transparent;
        border: none;
        color: #40241e;
        cursor: pointer;
    }

    .mobile-search-panel {
        display: none;
    }

    /* Cart and profile */

    .header-actions {
        display: flex;
        align-items: center;
        justify-self: end;

        gap: clamp(0.75rem, 1.2vw, 1.5rem);
    }

    /*
     * Clickable areas scale along with the icons.
     */
    .icon-button {
        position: relative;
        display: inline-flex;
        align-items: center;
        justify-content: center;

        width: clamp(52px, 3.55vw, 68px);
        height: clamp(52px, 3.55vw, 68px);

        padding: 0;
        background: transparent;
        border: none;
        border-radius: 50%;

        color: var(--color-text);
        cursor: pointer;
        text-decoration: none;

        transition: background-color 0.2s ease;
    }

    /*
     * Lucide icons grow from 34px to 46px.
     * This affects only the cart and profile icons.
     */
    .cart-link :global(svg),
    .profile-toggle :global(svg) {
        width: clamp(34px, 2.4vw, 46px);
        height: clamp(34px, 2.4vw, 46px);
    }

    .icon-button:hover {
        background-color: #d88f83;
    }

    .icon-button:focus-visible,
    .brand:focus-visible {
        outline: 2px solid var(--color-text);
        outline-offset: 3px;
    }

    /*
     * Cart badge stays in the same bottom-left position.
     * The live quantity counter is unchanged.
     */
    .cart-dot {
        position: absolute;
        bottom: 5px;
        left: 5px;

        width: 22px;
        height: 22px;

        background-color: #dc2626;
        border: 2px solid black;
        border-radius: 50%;
        box-sizing: border-box;

        display: flex;
        align-items: center;
        justify-content: center;

        color: white;
        font-family: var(--font-heading, 'Fredoka', sans-serif);
        font-size: 12px;
        font-weight: 700;
        line-height: 1;
        font-variant-numeric: tabular-nums;
    }

    /* Dropdown menus */

    .profile-container,
    .mobile-menu-container {
        position: relative;
    }

    .dropdown {
        position: absolute;
        top: 100%;
        z-index: 200;

        display: flex;
        flex-direction: column;
        gap: 0.3rem;

        min-width: 190px;
        padding: 0.65rem;

        background-color: var(--color-accent);
        border: 2px solid var(--color-border);
        border-radius: 9px;

        box-shadow: 0 5px 14px rgba(0, 0, 0, 0.15);
    }

    .profile-dropdown {
        right: 0;
    }

    .dropdown a,
    .dropdown button {
        display: block;
        width: 100%;
        padding: 0.65rem 0.8rem;

        background: transparent;
        border: none;
        border-radius: 5px;

        color: var(--color-text);
        font: inherit;
        font-weight: 650;
        text-align: left;
        text-decoration: none;
        cursor: pointer;
    }

    .dropdown a:hover,
    .dropdown button:hover {
        background-color: #d88f83;
    }

    /* Mobile navigation */

    .mobile-menu-container {
        display: none;
    }

    /*
     * Medium desktop / windowed mode.
     * Keeps your existing windowed proportions.
     */
    @media (max-width: 1200px) {
        .brand-image {
            width: 100px;
            height: 100px;
        }

        .brand-name {
            font-size: 2.4rem;
        }

        .header-inner {
            gap: 1rem;
            padding: 0.5rem 1.25rem;
        }

        .icon-button {
            width: 52px;
            height: 52px;
        }

        .cart-link :global(svg),
        .profile-toggle :global(svg) {
            width: 34px;
            height: 34px;
        }
    }

    /*
     * Smaller windows.
     */
    @media (max-width: 1000px) {
        .brand-image {
            width: 80px;
            height: 80px;
        }

        .brand-name {
            font-size: 1.9rem;
        }

        .center-button {
            min-width: 220px;
            padding: 0.75rem 1.1rem;
            font-size: 1.05rem;
        }

        .header-actions {
            gap: 0.5rem;
        }

        .icon-button {
            width: 52px;
            height: 52px;
        }

        .cart-link :global(svg),
        .profile-toggle :global(svg) {
            width: 34px;
            height: 34px;
        }
    }

    /*
     * Mobile layout.
     */
    @media (max-width: 760px) {
        .header-inner {
            display: flex;
            gap: 0.5rem;
            padding: 0.5rem 0.75rem;
        }

        .mobile-menu-container {
            display: block;
        }

        .mobile-dropdown {
            left: 0;
        }

        .desktop-navigation {
            display: flex;
            margin-left: auto;
        }

        .center-button {
            min-width: 0;
            padding: 0.7rem 0.85rem;
            gap: 0.4rem;
            font-size: 0.9rem;
        }

        .center-button :global(svg) {
            width: 19px;
            height: 19px;
        }

        .brand {
            gap: 0.35rem;
        }

        .brand-image {
            width: 58px;
            height: 58px;
        }

        .brand-name {
            font-size: 1.4rem;
        }

        .header-actions {
            margin-left: 0;
            gap: 0.1rem;
        }

        .profile-container {
            display: none;
        }

        .search-dropdown {
            display: none;
        }

        .mobile-search-panel {
            display: block;
            width: 100%;
            padding: 0.65rem 0.75rem;
            border-top: 1px solid rgba(64, 36, 30, 0.2);
        }
    }

    @media (max-width: 500px) {
        .brand-name {
            display: none;
        }

        .center-button {
            font-size: 0.8rem;
            padding: 0.65rem;
        }

        .center-button :global(svg) {
            width: 17px;
            height: 17px;
        }
    }

    @media (max-width: 400px) {
        .brand-image {
            width: 46px;
            height: 46px;
        }

        .icon-button {
            width: 42px;
            height: 42px;
        }

        .cart-link :global(svg) {
            width: 28px;
            height: 28px;
        }
    }
</style>
