import { error } from '@sveltejs/kit';
import type { Product } from '../../../lib/types/Product';
import type { PageLoad } from './$types';

export const load: PageLoad = async ({ fetch, params }) => {
    if (!/^\d+$/.test(params.id)) {
        error(404, 'Product not found');
    }

    const response = await fetch(`http://localhost:8080/api/products/${params.id}`)
        .catch(() => error(502, 'Unable to load the product. Please try again.'));

    if (response.status === 404 || response.status === 400) {
        error(404, 'Product not found');
    }

    if (!response.ok) {
        error(502, 'Unable to load the product. Please try again.');
    }

    const product: Product = await response.json();
    return { product };
};
