
<script lang="ts">
    import { onMount } from 'svelte';
    import '../app.css';
    import favicon from '#lib/assets/favicon.svg';
    import type { LayoutProps } from './$types';
    import Header from '../lib/components/Header.svelte';
    import { cart } from '../lib/stores/cart.svelte';

    let { children }: LayoutProps = $props();

    onMount(() => {
        cart.load();
    });
</script>

<svelte:head>
    <link rel="icon" href={favicon} />
</svelte:head>

<Header />

{@render children()}
