
<script lang="ts">
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';

    onMount(() => {
        void goto('/profile?tab=orders', { replaceState: true });
    });
</script>

<svelte:head>
    <title>My Orders | Viral Vault</title>
</svelte:head>

<p>Opening your order history...</p>
