
package com.codegoats.viralvault.order;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findByUserId(Long userId) {
        String sql = """
            SELECT order_id, total_amount, order_date, status
            FROM orders
            WHERE user_id = ?
            ORDER BY order_date DESC, order_id DESC
            """;

        return jdbcTemplate.queryForList(sql, userId);
    }

    public List<Map<String, Object>> findItemsByOrderId(
            Long orderId, Long userId) {

        String sql = """
            SELECT
                oi.product_id,
                p.name AS product_name,
                p.image_url,
                oi.quantity,
                oi.price_at_purchase
            FROM order_items oi
            JOIN orders o ON oi.order_id = o.order_id
            LEFT JOIN products p ON oi.product_id = p.product_id
            WHERE oi.order_id = ? AND o.user_id = ?
            ORDER BY oi.order_item_id
            """;

        return jdbcTemplate.queryForList(sql, orderId, userId);
    }

    public List<Map<String, Object>> findCartItems(Long userId) {
        String sql = """
            SELECT
                c.product_id,
                SUM(c.quantity) AS quantity,
                p.price,
                p.quantity AS stock
            FROM cart_items c
            LEFT JOIN products p ON c.product_id = p.product_id
            WHERE c.user_id = ?
            GROUP BY c.product_id
            ORDER BY c.product_id
            """;

        return jdbcTemplate.queryForList(sql, userId);
    }

    public Long createOrder(Long userId, BigDecimal total) {
        String sql = """
            INSERT INTO orders (user_id, total_amount, status)
            VALUES (?, ?, 'Pending')
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            );

            statement.setLong(1, userId);
            statement.setBigDecimal(2, total);

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                "Unable to generate an order ID."
            );
        }

        return key.longValue();
    }

    public void addOrderItem(
            Long orderId,
            Long productId,
            int quantity,
            BigDecimal price) {

        jdbcTemplate.update(
            """
            INSERT INTO order_items
                (order_id, product_id, quantity, price_at_purchase)
            VALUES (?, ?, ?, ?)
            """,
            orderId,
            productId,
            quantity,
            price
        );
    }

    public void reduceInventory(Long productId, int quantity) {
        int updated = jdbcTemplate.update(
            """
            UPDATE products
            SET quantity = quantity - ?
            WHERE product_id = ?
              AND quantity >= ?
            """,
            quantity,
            productId,
            quantity
        );

        if (updated != 1) {
            throw new IllegalArgumentException(
                "Insufficient inventory for product " + productId
            );
        }
    }

    public void clearCart(Long userId) {
        jdbcTemplate.update(
            "DELETE FROM cart_items WHERE user_id = ?",
            userId
        );
    }
}
