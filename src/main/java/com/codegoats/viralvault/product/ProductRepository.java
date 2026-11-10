
package com.codegoats.viralvault.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Product> findAll() {
        String sql = """
            SELECT product_id, name, description, price,
                   quantity, image_url
            FROM products
            ORDER BY product_id
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new Product(
                rs.getLong("product_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getInt("quantity"),
                rs.getString("image_url")
            )
        );
    }

    public Product findById(Long id) {
        String sql = """
            SELECT product_id, name, description, price,
                   quantity, image_url
            FROM products
            WHERE product_id = ?
            """;

        List<Product> results = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new Product(
                rs.getLong("product_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getInt("quantity"),
                rs.getString("image_url")
            ),
            id
        );

        return results.isEmpty() ? null : results.get(0);
    }
}
