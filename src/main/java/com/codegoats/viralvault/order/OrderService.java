
package com.codegoats.viralvault.order;

import org.springframework.stereotype.Service;
import com.codegoats.viralvault.discount.DiscountService;
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
    private final DiscountService discountService;

    public OrderService(
            OrderRepository orderRepository,
            DiscountService discountService) {
        this.orderRepository = orderRepository;
        this.discountService = discountService;
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
            result.put("snapshot", orderRepository.findSnapshot(orderId));

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
            String discountCode, CheckoutCustomer customer) {

        if (customer == null) {
            throw new IllegalArgumentException("Checkout contact and shipping details are required.");
        }
        customer = customer.validated();

        String code = discountCode == null
                ? ""
                : discountCode.trim().toUpperCase(Locale.ROOT);



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

        BigDecimal discount = roundMoney(
            discountService.calculateDiscount(code, subtotal)
        );

        BigDecimal taxableSubtotal =
                roundMoney(subtotal.subtract(discount));

        BigDecimal tax = roundMoney(
            taxableSubtotal.multiply(TAX_RATE)
        );

        BigDecimal total = roundMoney(
            taxableSubtotal.add(tax)
        );

        Long orderId = orderRepository.createOrder(userId, total);
        // Saved in the same transaction as inventory, items, and cart updates.
        // Monetary values always come from the server, never the request body.
        orderRepository.saveSnapshot(orderId, customer, subtotal, code, discount,
                TAX_RATE, tax, total);

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
        result.put("snapshot", orderRepository.findSnapshot(orderId));

        return result;
    }

    // ADMIN ORDER MANAGEMENT

    // Convert database results into the frontend format.
    private Map<String, Object> formatAdminOrder(
            Map<String, Object> order) {

        Map<String, Object> result = new LinkedHashMap<>();

        String customer = order.get("full_name") == null
                ? ""
                : order.get("full_name").toString();

        if (customer.isBlank()) {
            customer = order.get("username").toString();
        }

        String status = order.get("status") == null
                ? "Pending"
                : order.get("status").toString();

        result.put("orderId", order.get("order_id"));
        result.put("userId", order.get("user_id"));
        Map<String, Object> snapshot = orderRepository.findSnapshot(
                ((Number) order.get("order_id")).longValue());
        result.put("snapshot", snapshot);
        result.put("customer", snapshot == null ? customer
                : snapshot.get("firstName") + " " + snapshot.get("lastName"));
        result.put("email", snapshot == null ? order.get("email") : snapshot.get("email"));
        result.put("orderDate", order.get("order_date"));
        result.put("totalAmount", order.get("total_amount"));
        result.put("status", status);

        result.put(
            "fulfilled",
            status.equalsIgnoreCase("Fulfilled")
        );

        return result;
    }

    // Retrieve all customer orders.
    public List<Map<String, Object>> getAllOrders() {

        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> order :
                orderRepository.findAllOrders()) {

            results.add(formatAdminOrder(order));
        }

        return results;
    }

    // Retrieve details and purchased items for an order.
    public Map<String, Object> getOrderDetails(Long orderId) {

        Map<String, Object> order =
                orderRepository.findOrderById(orderId);

        if (order == null) {
            return null;
        }

        Map<String, Object> result =
                formatAdminOrder(order);

        result.put(
            "items",
            orderRepository.findAdminOrderItems(orderId)
        );

        return result;
    }

    // Update the fulfillment status of an order.
    @Transactional
    public Map<String, Object> updateFulfillment(
            Long orderId, boolean fulfilled) {

        String status = fulfilled
                ? "Fulfilled"
                : "Pending";

        boolean updated =
                orderRepository.updateOrderStatus(
                    orderId, status
                );

        if (!updated) {
            return null;
        }

        return getOrderDetails(orderId);
    }
}
