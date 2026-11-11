
import { getCart } from './cart';
import { calculatePricing } from './pricing';

export type CheckoutDetails = {
    firstName: string;
    lastName: string;
    email: string;
    address: string;
    city: string;
    state: string;
    zipCode: string;
};

export type OrderConfirmation = {
    orderId: string;
    total: number;
    customer: CheckoutDetails;
};

type SavedOrderResponse = {
    orderId: number;
    subtotal: number;
    discount: number;
    tax: number;
    total: number;
    status: string;
};

const API = 'http://localhost:8080/api';

export async function submitOrder(
    customer: CheckoutDetails,
    discountCode: string = ''
): Promise<OrderConfirmation> {
    const sessionResponse = await fetch(
        `${API}/users/session`,
        { credentials: 'include' }
    );

    if (!sessionResponse.ok) {
        throw new Error(
            'Please log in before placing an order.'
        );
    }

    const items = await getCart();

    if (items.length === 0) {
        throw new Error(
            'Cannot checkout with an empty cart.'
        );
    }

    const subtotal = items.reduce(
        (sum, item) =>
            sum + item.product.price * item.quantity,
        0
    );

    const pricing = calculatePricing(
        subtotal,
        discountCode
    );

    if (!pricing.validDiscount) {
        throw new Error('Invalid discount code.');
    }

    const response = await fetch(`${API}/orders`, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            discountCode: pricing.discountCode
        })
    });

    if (!response.ok) {
        if (response.status === 401) {
            throw new Error(
                'Your session has expired. Please log in again.'
            );
        }

        const message = await response.text();

        throw new Error(
            message || 'Unable to place your order.'
        );
    }

    const savedOrder: SavedOrderResponse =
        await response.json();

    return {
        orderId: String(savedOrder.orderId),
        total: Number(savedOrder.total),
        customer
    };
}
