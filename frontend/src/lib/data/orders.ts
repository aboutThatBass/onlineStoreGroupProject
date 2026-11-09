import { clearCart, getCart } from './cart';

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

export async function submitOrder(
    customer: CheckoutDetails
): Promise<OrderConfirmation> {
    const items = await getCart();

    if (items.length === 0) {
        throw new Error('Cannot checkout with an empty cart');
    }

    const total = items.reduce(
        (sum, item) => sum + item.product.price * item.quantity,
        0
    );

    const order: OrderConfirmation = {
        orderId: `VV-${Date.now()}`,
        total: Math.round(total * 100) / 100,
        customer
    };

    await clearCart();

    return order;
}
