
<script lang="ts">
    import '../app.css';
    import favicon from '#lib/assets/favicon.svg';
    import type { LayoutProps } from './$types';
    import Header from '../lib/components/Header.svelte';
    import { onMount } from 'svelte';
    import { cart } from '../lib/stores/cart';

    let { children }: LayoutProps = $props();

    onMount(() => {
        cart.loadCart();
    });
</script>

<svelte:head>
    <link rel="icon" href={favicon} />
</svelte:head>

<Header />

{@render children()}
