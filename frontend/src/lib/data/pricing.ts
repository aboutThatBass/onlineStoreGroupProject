export const TAX_RATE = 0.0825;

const API = 'http://localhost:8080/api/discounts';

export type DiscountRule = {
    valid: boolean;
    code: string;
    discountType: 'percent' | 'fixed';
    discountPercent: number;
    fixedAmount: number;
};

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

export async function validateDiscountCode(
    code: string
): Promise<DiscountRule | null> {
    const normalized = normalizeDiscountCode(code);

    if (!normalized) {
        return null;
    }

    const response = await fetch(
        `${API}/validate?code=${encodeURIComponent(normalized)}`,
        {
            credentials: 'include',
            cache: 'no-store'
        }
    );

    if (response.status === 400) {
        throw new Error('Invalid or inactive discount code.');
    }

    if (!response.ok) {
        throw new Error('Unable to validate discount code.');
    }

    const rule: DiscountRule = await response.json();

    if (!rule.valid) {
        throw new Error('Invalid or inactive discount code.');
    }

    return rule;
}

export function calculatePricing(
    subtotal: number,
    rule: DiscountRule | string | null = null
): PricingResult {
    const safeSubtotal = roundMoney(Math.max(0, subtotal));

    const discountRule =
        typeof rule === 'object' && rule?.valid ? rule : null;

    let discount = 0;

    if (discountRule) {
        if (discountRule.discountType === 'percent') {
            discount =
                safeSubtotal * discountRule.discountPercent / 100;
        } else {
            discount = discountRule.fixedAmount;
        }
    }

    discount = roundMoney(
        Math.min(Math.max(0, discount), safeSubtotal)
    );

    const taxableSubtotal = roundMoney(
        safeSubtotal - discount
    );

    const tax = roundMoney(taxableSubtotal * TAX_RATE);
    const total = roundMoney(taxableSubtotal + tax);

    return {
        subtotal: safeSubtotal,
        discount,
        taxableSubtotal,
        tax,
        total,
        discountCode: discountRule?.code ?? '',
        validDiscount:
            Boolean(discountRule) ||
            rule === null ||
            rule === ''
    };
}