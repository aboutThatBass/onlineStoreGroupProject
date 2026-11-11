
package com.codegoats.viralvault.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OrderService {

    private static final BigDecimal TAX_RATE =
            new BigDecimal("0.0825");

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Map<String, Object>> getOrdersForUser(Long userId) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> order :
                orderRepository.findByUserId(userId)) {

            Long orderId =
                    ((Number) order.get("order_id")).longValue();

            Map<String, Object> result = new LinkedHashMap<>();

            result.put("orderId", orderId);
            result.put("totalAmount", order.get("total_amount"));
            result.put("orderDate", order.get("order_date"));
            result.put("status", order.get("status"));

            result.put(
                "items",
                orderRepository.findItemsByOrderId(orderId, userId)
            );

            results.add(result);
        }

        return results;
    }

    private BigDecimal roundMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public Map<String, Object> createOrder(
            Long userId,
            String discountCode) {

        String code = discountCode == null
                ? ""
                : discountCode.trim().toUpperCase(Locale.ROOT);

        if (!code.isEmpty()
                && !code.equals("LABUB00")
                && !code.equals("0HIO123")
                && !code.equals("TUNGTUNG22")) {

            throw new IllegalArgumentException(
                "Invalid discount code."
            );
        }

        List<Map<String, Object>> cartItems =
                orderRepository.findCartItems(userId);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException(
                "Cannot checkout with an empty cart."
            );
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map<String, Object> item : cartItems) {
            Long productId =
                    ((Number) item.get("product_id")).longValue();

            int quantity =
                    ((Number) item.get("quantity")).intValue();

            if (item.get("price") == null
                    || item.get("stock") == null) {
                throw new IllegalArgumentException(
                    "Product " + productId + " is unavailable."
                );
            }

            int stock =
                    ((Number) item.get("stock")).intValue();

            if (quantity <= 0 || quantity > stock) {
                throw new IllegalArgumentException(
                    "Insufficient stock for product " + productId
                );
            }

            BigDecimal price =
                    new BigDecimal(item.get("price").toString());

            if (price.signum() < 0) {
                throw new IllegalArgumentException(
                    "Invalid price for product " + productId
                );
            }

            subtotal = subtotal.add(
                price.multiply(BigDecimal.valueOf(quantity))
            );
        }

        subtotal = roundMoney(subtotal);

        BigDecimal discount = BigDecimal.ZERO;

        switch (code) {
            case "LABUB00":
                discount = subtotal.multiply(
                    new BigDecimal("0.10")
                );
                break;

            case "0HIO123":
                discount = subtotal.multiply(
                    new BigDecimal("0.20")
                );
                break;

            case "TUNGTUNG22":
                discount = new BigDecimal("5.00");
                break;

            default:
                break;
        }

        discount = roundMoney(discount.min(subtotal));

        BigDecimal taxableSubtotal =
                roundMoney(subtotal.subtract(discount));

        BigDecimal tax = roundMoney(
            taxableSubtotal.multiply(TAX_RATE)
        );

        BigDecimal total = roundMoney(
            taxableSubtotal.add(tax)
        );

        Long orderId = orderRepository.createOrder(
            userId,
            total
        );

        for (Map<String, Object> item : cartItems) {
            Long productId =
                    ((Number) item.get("product_id")).longValue();

            int quantity =
                    ((Number) item.get("quantity")).intValue();

            BigDecimal price = new BigDecimal(
                item.get("price").toString()
            );

            orderRepository.reduceInventory(productId, quantity);

            orderRepository.addOrderItem(
                orderId,
                productId,
                quantity,
                price
            );
        }

        orderRepository.clearCart(userId);

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("orderId", orderId);
        result.put("subtotal", subtotal);
        result.put("discount", discount);
        result.put("tax", tax);
        result.put("total", total);
        result.put("status", "Pending");

        return result;
    }
}
