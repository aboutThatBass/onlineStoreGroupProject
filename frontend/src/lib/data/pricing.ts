
export const TAX_RATE = 0.0825;

export const DISCOUNT_CODES = {
    LABUB00: { type: 'percent', value: 0.10 },
    '0HIO123': { type: 'percent', value: 0.20 },
    TUNGTUNG22: { type: 'fixed', value: 5.00 }
} as const;

export type PricingResult = {
    subtotal: number;
    discount: number;
    taxableSubtotal: number;
    tax: number;
    total: number;
    discountCode: string;
    validDiscount: boolean;
};

function roundMoney(amount: number): number {
    return Math.round((amount + Number.EPSILON) * 100) / 100;
}

export function normalizeDiscountCode(code: string): string {
    return code.trim().toUpperCase();
}

export function calculatePricing(
    subtotal: number,
    code: string = ''
): PricingResult {
    const normalizedCode = normalizeDiscountCode(code);
    const safeSubtotal = roundMoney(Math.max(0, subtotal));

    const discountRule =
        DISCOUNT_CODES[
            normalizedCode as keyof typeof DISCOUNT_CODES
        ];

    let discount = 0;

    if (discountRule) {
        if (discountRule.type === 'percent') {
            discount = safeSubtotal * discountRule.value;
        } else {
            discount = discountRule.value;
        }
    }

    discount = roundMoney(Math.min(discount, safeSubtotal));

    const taxableSubtotal = roundMoney(safeSubtotal - discount);
    const tax = roundMoney(taxableSubtotal * TAX_RATE);
    const total = roundMoney(taxableSubtotal + tax);

    return {
        subtotal: safeSubtotal,
        discount,
        taxableSubtotal,
        tax,
        total,
        discountCode: discountRule ? normalizedCode : '',
        validDiscount: normalizedCode === '' || Boolean(discountRule)
    };
}
