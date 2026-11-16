package com.codegoats.viralvault.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productMapper =
        (rs, rowNum) -> {
            double rawSale = rs.getDouble("sale_price");
            Double salePrice = rs.wasNull() ? null : rawSale;

            return new Product(
                rs.getLong("product_id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDouble("price"),
                salePrice,
                rs.getInt("quantity"),
                rs.getString("image_url")
            );
        };

    public List<Product> findAll() {

        String sql = """
            SELECT product_id, name, category, description,
                   price, sale_price, quantity, image_url
            FROM products
            ORDER BY product_id
            """;

        return jdbcTemplate.query(sql, productMapper);
    }

    public Product findById(Long id) {

        String sql = """
            SELECT product_id, name, category, description,
                   price, sale_price, quantity, image_url
            FROM products
            WHERE product_id = ?
            """;

        List<Product> results =
            jdbcTemplate.query(sql, productMapper, id);

        return results.isEmpty() ? null : results.get(0);
    }

    public void addProduct(Product product) {

        String sql = """
            INSERT INTO products
                (name, category, description,
                 price, sale_price, quantity, image_url, is_on_sale)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(
            sql,
            product.getName(),
            product.getCategory(),
            product.getDescription(),
            product.getPrice(),
            product.getSalePrice(),
            product.getQuantity(),
            product.getImageUrl(),
            product.getSalePrice() != null ? 1 : 0
        );
    }

    public boolean updateProduct(Long id, Product product) {

        String sql = """
            UPDATE products
            SET name = ?,
                category = ?,
                description = ?,
                price = ?,
                sale_price = ?,
                quantity = ?,
                image_url = ?,
                is_on_sale = ?
            WHERE product_id = ?
            """;

        int rowsUpdated = jdbcTemplate.update(
            sql,
            product.getName(),
            product.getCategory(),
            product.getDescription(),
            product.getPrice(),
            product.getSalePrice(),
            product.getQuantity(),
            product.getImageUrl(),
            product.getSalePrice() != null ? 1 : 0,
            id
        );

        return rowsUpdated > 0;
    }

    public boolean deleteProduct(Long id) {

        String sql = """
            DELETE FROM products
            WHERE product_id = ?
              AND NOT EXISTS (
                  SELECT 1 FROM order_items
                  WHERE product_id = ?
              )
              AND NOT EXISTS (
                  SELECT 1 FROM cart_items
                  WHERE product_id = ?
              )
            """;

        return jdbcTemplate.update(sql, id, id, id) > 0;
    }
}