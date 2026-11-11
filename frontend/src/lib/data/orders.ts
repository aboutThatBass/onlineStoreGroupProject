
import { clearCart, getCart } from './cart';
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

export async function submitOrder(
    customer: CheckoutDetails,
    discountCode: string = ''
): Promise<OrderConfirmation> {
    const items = await getCart();

    if (items.length === 0) {
        throw new Error('Cannot checkout with an empty cart');
    }

    const subtotal = items.reduce(
        (sum, item) => sum + item.product.price * item.quantity,
        0
    );

    const pricing = calculatePricing(subtotal, discountCode);

    if (!pricing.validDiscount) {
        throw new Error('Invalid discount code.');
    }

    const order: OrderConfirmation = {
        orderId: `VV-${Date.now()}`,
        total: pricing.total,
        customer
    };

    await clearCart();

    return order;
}
