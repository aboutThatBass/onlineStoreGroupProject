package com.codegoats.viralvault.discount;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DiscountRepository {

    private final JdbcTemplate jdbcTemplate;

    public DiscountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Discount> discountMapper =
        (rs, rowNum) -> new Discount(
            rs.getLong("discount_id"),
            rs.getString("code"),
            rs.getDouble("discount_percent"),
            rs.getInt("is_active") == 1
        );

    public List<Discount> findAll() {
        String sql = """
            SELECT discount_id, code, discount_percent, is_active
            FROM discounts
            ORDER BY discount_id
            """;
        return jdbcTemplate.query(sql, discountMapper);
    }

    public Discount findById(Long id) {
        String sql = """
            SELECT discount_id, code, discount_percent, is_active
            FROM discounts
            WHERE discount_id = ?
            """;
        List<Discount> results = jdbcTemplate.query(sql, discountMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public Discount findByCode(String code) {
        String sql = """
            SELECT discount_id, code, discount_percent, is_active
            FROM discounts
            WHERE UPPER(code) = UPPER(?)
            """;
        List<Discount> results = jdbcTemplate.query(sql, discountMapper, code);
        return results.isEmpty() ? null : results.get(0);
    }

    public void addDiscount(Discount discount) {
        String sql = """
            INSERT INTO discounts (code, discount_percent, is_active)
            VALUES (?, ?, ?)
            """;
        jdbcTemplate.update(
            sql,
            discount.getCode(),
            discount.getDiscountPercent(),
            discount.isActive() ? 1 : 0
        );
    }

    public boolean updateDiscount(Long id, Discount discount) {
        String sql = """
            UPDATE discounts
            SET code = ?,
                discount_percent = ?,
                is_active = ?
            WHERE discount_id = ?
            """;
        int rows = jdbcTemplate.update(
            sql,
            discount.getCode(),
            discount.getDiscountPercent(),
            discount.isActive() ? 1 : 0,
            id
        );
        return rows > 0;
    }
}