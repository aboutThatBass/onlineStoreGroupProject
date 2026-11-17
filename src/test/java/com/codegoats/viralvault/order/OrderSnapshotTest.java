package com.codegoats.viralvault.order;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "VIRALVAULT_LOCAL_DEV=false")
class OrderSnapshotTest {
    // Shared in-memory SQLite: tests never open the developer's tracked database.
    static final String DATABASE = "jdbc:sqlite:file:snapshot-" + UUID.randomUUID()
            + "?mode=memory&cache=shared";
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> DATABASE);
    }
    @Autowired JdbcTemplate jdbc;
    @Autowired OrderService service;
    @Autowired OrderRepository repository;

    final CheckoutCustomer shipping = new CheckoutCustomer(" Alex ", "Shopper",
            "checkout@example.com", "123 Original St", "San Antonio", "TX", "78249");

    @BeforeEach
    void setup() {
        jdbc.execute("DROP TRIGGER IF EXISTS reject_order_item");
        for (String table : new String[]{"order_snapshots", "order_items", "cart_items", "orders", "users"}) {
            jdbc.update("DELETE FROM " + table);
        }
        jdbc.update("DELETE FROM products WHERE product_id = 777");
        jdbc.update("INSERT INTO users (user_id, username, email, password_hash) VALUES (777, 'snapshot-test', 'profile@example.com', 'test-only')");
        jdbc.update("INSERT INTO products (product_id, name, category, price, quantity) VALUES (777, 'Snapshot product', 'Test', 24.99, 10)");
        jdbc.update("INSERT INTO cart_items (user_id, product_id, quantity) VALUES (777, 777, 2)");
    }

    long place(String code) {
        return ((Number) service.createOrder(777L, code, shipping).get("orderId")).longValue();
    }
    void money(String expected, Object actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(new BigDecimal(actual.toString())));
    }

    @Test
    void savesServerCalculatedReceiptAndShippingAcrossHistoryAndAdminViews() {
        long id = place(" labub00 ");
        Map<String, Object> snapshot = repository.findSnapshot(id);
        money("49.98", snapshot.get("subtotal"));
        money("5.00", snapshot.get("discountAmount"));
        money("0.0825", snapshot.get("taxRate"));
        money("3.71", snapshot.get("taxAmount"));
        money("48.69", snapshot.get("total"));
        assertEquals("LABUB00", snapshot.get("discountCode"));
        assertEquals("Alex", snapshot.get("firstName"));
        assertEquals("123 Original St", snapshot.get("address"));
        assertEquals(snapshot, service.getOrdersForUser(777L).get(0).get("snapshot"));
        assertEquals(snapshot, service.getOrderDetails(id).get("snapshot"));
        assertTrue(service.getOrdersForUser(778L).isEmpty());
        assertEquals(8, jdbc.queryForObject("SELECT quantity FROM products WHERE product_id=777", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM cart_items", Integer.class));
    }

    @Test
    void receiptDoesNotChangeWhenAccountOrCatalogChanges() {
        long id = place("0HIO123");
        Map<String, Object> before = repository.findSnapshot(id);
        jdbc.update("UPDATE users SET full_name='Changed Name', email='changed@example.com', address='New address' WHERE user_id=777");
        jdbc.update("UPDATE products SET price=999 WHERE product_id=777");
        assertEquals(before, repository.findSnapshot(id));
        assertEquals("Alex Shopper", service.getOrderDetails(id).get("customer"));
        assertEquals("checkout@example.com", service.getOrderDetails(id).get("email"));
        money("24.99", repository.findItemsByOrderId(id, 777L).get(0).get("price_at_purchase"));
    }

    @Test
    void noDiscountAndFixedDiscountAreStoredIncludingZeroTotals() {
        long id = place("");
        money("0", repository.findSnapshot(id).get("discountAmount"));
        money("4.12", repository.findSnapshot(id).get("taxAmount"));
        money("54.10", repository.findSnapshot(id).get("total"));
        jdbc.update("UPDATE products SET price=1 WHERE product_id=777");
        jdbc.update("INSERT INTO cart_items (user_id, product_id, quantity) VALUES (777,777,1)");
        long freeOrder = place("TUNGTUNG22");
        money("1", repository.findSnapshot(freeOrder).get("discountAmount"));
        money("0", repository.findSnapshot(freeOrder).get("taxAmount"));
        money("0", repository.findSnapshot(freeOrder).get("total"));
    }

    @Test
    void legacyOrdersSurviveRepeatedAdditiveSchemaInitializationWithoutInventedDetails() {
        long id = repository.createOrder(777L, new BigDecimal("12.34"));
        // Simulate a database created before this feature existed.
        jdbc.execute("DROP TABLE order_snapshots");
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("init.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("init.sql"));
            return null;
        });
        assertNull(service.getOrderDetails(id).get("snapshot"));
        money("12.34", service.getOrderDetails(id).get("totalAmount"));
        assertNull(service.getOrdersForUser(777L).get(0).get("snapshot"));
    }

    @Test
    void invalidShippingOrDiscountCannotCreateAnOrderOrClearCart() {
        assertThrows(IllegalArgumentException.class, () -> service.createOrder(777L, "", null));
        assertThrows(IllegalArgumentException.class, () -> service.createOrder(777L, "",
                new CheckoutCustomer("Alex", "Test", "invalid", "Street", "City", "TX", "78249")));
        assertThrows(IllegalArgumentException.class, () -> place("INVALID"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM cart_items", Integer.class));
    }

    @Test
    void failureAfterSnapshotInsertRollsBackOrderReceiptAndInventory() {
        jdbc.execute("CREATE TRIGGER reject_order_item BEFORE INSERT ON order_items BEGIN SELECT RAISE(ABORT, 'test failure'); END");
        assertThrows(RuntimeException.class, () -> place(""));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM order_snapshots", Integer.class));
        assertEquals(10, jdbc.queryForObject("SELECT quantity FROM products WHERE product_id=777", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM cart_items", Integer.class));
    }
}
