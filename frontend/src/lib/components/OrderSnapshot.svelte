<script lang="ts">
    import type { OrderSnapshot } from '../types/OrderSnapshot';
    let { snapshot }: { snapshot: OrderSnapshot | null | undefined } = $props();
    const money = (value: number) => Number(value).toFixed(2);
</script>

{#if snapshot}
    <section class="snapshot" aria-label="Order receipt and shipping details">
        <div>
            <h4>Ship to</h4>
            <p>{snapshot.firstName} {snapshot.lastName}</p>
            <p>{snapshot.email}</p>
            <p>{snapshot.address}</p>
            <p>{snapshot.city}, {snapshot.state} {snapshot.zipCode}</p>
        </div>
        <dl>
            <div><dt>Subtotal</dt><dd>${money(snapshot.subtotal)}</dd></div>
            <div>
                <dt>Discount{snapshot.discountCode ? ` (${snapshot.discountCode})` : ' (none)'}</dt>
                <dd>-${money(snapshot.discountAmount)}</dd>
            </div>
            <div>
                <dt>Sales tax ({Number((Number(snapshot.taxRate) * 100).toFixed(4))}%)</dt>
                <dd>${money(snapshot.taxAmount)}</dd>
            </div>
            <div class="total"><dt>Order total</dt><dd>${money(snapshot.total)}</dd></div>
        </dl>
    </section>
{:else}
    <p class="legacy">Shipping details and the tax/discount breakdown were not recorded for this older order.</p>
{/if}

<style>
    .snapshot { display: grid; grid-template-columns: repeat(auto-fit, minmax(min(240px, 100%), 1fr)); gap: 1.5rem; margin: 1rem 0; padding: 1rem; border: 1px solid #d8d8d8; border-radius: 8px; }
    h4 { margin: 0 0 0.5rem; }
    p { margin: 0.25rem 0; overflow-wrap: anywhere; }
    dl { margin: 0; min-width: 0; }
    dl div { display: flex; justify-content: space-between; gap: 1rem; margin: 0.4rem 0; }
    dt { overflow-wrap: anywhere; }
    dd { margin: 0; white-space: nowrap; }
    .total { border-top: 1px solid #d8d8d8; padding-top: 0.5rem; font-weight: 700; }
    .legacy { color: #666; font-size: 0.9rem; margin: 1rem 0; }
</style>
