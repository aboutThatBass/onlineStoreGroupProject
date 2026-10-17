
import type { Product } from '../types/Product';

export const products: Product[] = [
    {
        id: 1,
        name: "Labubu Exciting Macaron",
        description: "Collectible plush figure from POP MART.",
        price: 24.99,
        quantity: 12,
        imageUrl: "/images/labubu.png"
    },
    {
        id: 2,
        name: "SMISKI Series 1",
        description: "Glow-in-the-dark collectible figure.",
        price: 12.99,
        quantity: 8,
        imageUrl: "/images/smiski.png"
    },
    {
        id: 3,
        name: "Sonny Angel Animal Series",
        description: "Adorable miniature collectible figure.",
        price: 14.99,
        quantity: 0,
        imageUrl: "/images/sonny-angel.png"
    }
];
