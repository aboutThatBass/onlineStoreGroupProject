
package com.codegoats.viralvault.cart;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CartRepository {

    private final JdbcTemplate jdbcTemplate;

    public CartRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findByUserId(Long userId) {
        String sql = """
            SELECT product_id, SUM(quantity) AS quantity
            FROM cart_items
            WHERE user_id = ?
            GROUP BY product_id
            ORDER BY product_id
            """;

        return jdbcTemplate.queryForList(sql, userId);
    }

    public void setQuantity(Long userId, Long productId, int quantity) {
        jdbcTemplate.update(
            "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?",
            userId, productId
        );

        if (quantity > 0) {
            jdbcTemplate.update(
                """
                INSERT INTO cart_items (user_id, product_id, quantity)
                VALUES (?, ?, ?)
                """,
                userId, productId, quantity
            );
        }
    }

    public void removeItem(Long userId, Long productId) {
        jdbcTemplate.update(
            "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?",
            userId, productId
        );
    }

    public void clearCart(Long userId) {
        jdbcTemplate.update(
            "DELETE FROM cart_items WHERE user_id = ?",
            userId
        );
    }
}
