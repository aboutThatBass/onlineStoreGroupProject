package com.codegoats.viralvault;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private boolean columnExists(String table, String columnName) {
        return jdbcTemplate
            .queryForList("PRAGMA table_info(" + table + ")")
            .stream()
            .anyMatch(column ->
                columnName.equalsIgnoreCase(
                    String.valueOf(column.get("name"))
                )
            );
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (!columnExists("products", "sale_price")) {
            jdbcTemplate.execute("""
                ALTER TABLE products
                ADD COLUMN sale_price DECIMAL(10, 2)
                """);
        }

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS discounts (
                discount_id INTEGER PRIMARY KEY AUTOINCREMENT,
                code VARCHAR(50) NOT NULL UNIQUE,
                discount_percent DECIMAL(5, 2) NOT NULL,
                is_active BOOLEAN NOT NULL DEFAULT 1,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """);

        if (!columnExists("discounts", "discount_type")) {
            jdbcTemplate.execute("""
                ALTER TABLE discounts
                ADD COLUMN discount_type TEXT NOT NULL
                DEFAULT 'percent'
                """);
        }

        if (!columnExists("discounts", "fixed_amount")) {
            jdbcTemplate.execute("""
                ALTER TABLE discounts
                ADD COLUMN fixed_amount DECIMAL(10, 2)
                NOT NULL DEFAULT 0
                """);
        }

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS schema_migrations (
                migration_name TEXT PRIMARY KEY
            )
            """);

        int seedRequired = jdbcTemplate.update("""
            INSERT OR IGNORE INTO schema_migrations
            (migration_name)
            VALUES ('original_discount_codes_v1')
            """);

        if (seedRequired > 0) {

            jdbcTemplate.update("""
                INSERT OR IGNORE INTO discounts
                (code, discount_percent, discount_type,
                 fixed_amount, is_active)
                VALUES ('LABUB00', 10, 'percent', 0, 1)
                """);

            jdbcTemplate.update("""
                INSERT OR IGNORE INTO discounts
                (code, discount_percent, discount_type,
                 fixed_amount, is_active)
                VALUES ('0HIO123', 20, 'percent', 0, 1)
                """);

            jdbcTemplate.update("""
                INSERT OR IGNORE INTO discounts
                (code, discount_percent, discount_type,
                 fixed_amount, is_active)
                VALUES ('TUNGTUNG22', 0, 'fixed', 5, 1)
                """);

            System.out.println(
                "Original discount codes initialized."
            );
        }

        System.out.println(
            "Discount and sales database migration complete."
        );
    }
}