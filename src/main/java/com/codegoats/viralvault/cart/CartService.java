
package com.codegoats.viralvault.cart;

import com.codegoats.viralvault.product.Product;
import com.codegoats.viralvault.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(
        CartRepository cartRepository,
        ProductRepository productRepository
    ) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public List<Map<String, Object>> getCart(Long userId) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> row : cartRepository.findByUserId(userId)) {
            Long productId = ((Number) row.get("product_id")).longValue();
            int quantity = ((Number) row.get("quantity")).intValue();

            Product product = productRepository.findById(productId);

            if (product != null) {
                results.add(Map.of(
                    "product", product,
                    "quantity", quantity
                ));
            }
        }

        return results;
    }

    @Transactional
    public List<Map<String, Object>> setQuantity(
        Long userId,
        Long productId,
        int quantity
    ) {
        Product product = productRepository.findById(productId);

        if (product == null) {
            throw new IllegalArgumentException("Product not found.");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException(
                "Quantity cannot be negative."
            );
        }

        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException(
                "Requested quantity exceeds available stock."
            );
        }

        cartRepository.setQuantity(userId, productId, quantity);

        return getCart(userId);
    }

    @Transactional
    public List<Map<String, Object>> removeItem(
        Long userId,
        Long productId
    ) {
        cartRepository.removeItem(userId, productId);
        return getCart(userId);
    }

    @Transactional
    public List<Map<String, Object>> clearCart(Long userId) {
        cartRepository.clearCart(userId);
        return getCart(userId);
    }
}
