export type Product = {
    id: number;
    name: string;
    description: string;
    price: number;
    salePrice?: number | null;
    onSale?: boolean;
    quantity: number;
    imageUrl: string;
}

export function effectivePrice(product: Product): number {
    return product.salePrice ?? product.price;
}

export function isOnSale(product: Product): boolean {
    return product.salePrice != null &&
        product.salePrice < product.price;
}