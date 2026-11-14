
package com.codegoats.viralvault.order;

import com.codegoats.viralvault.user.User;
import com.codegoats.viralvault.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(
            OrderService orderService,
            UserRepository userRepository) {

        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    private Long getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object userId = session.getAttribute("userId");

        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }

        return null;
    }

    // Verify administrative privileges.
    private boolean isAdministrator(
            HttpServletRequest request) {

        Long userId = getUserId(request);

        if (userId == null) {
            return false;
        }

        return userRepository.findById(userId)
                .map(User::isAdmin)
                .orElse(false);
    }

    private ResponseEntity<?> checkAdmin(
            HttpServletRequest request) {

        if (getUserId(request) == null) {
            return ResponseEntity.status(401).body(
                Map.of(
                    "message",
                    "Administrator authentication required."
                )
            );
        }

        if (!isAdministrator(request)) {
            return ResponseEntity.status(403).body(
                Map.of(
                    "message",
                    "Administrator privileges required."
                )
            );
        }

        return null;
    }

    public record CreateOrderRequest(String discountCode) {
    }

    public record FulfillmentRequest(Boolean fulfilled) {
    }

    // Customer: retrieve personal order history.
    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders(
            HttpServletRequest request) {

        Long userId = getUserId(request);

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return ResponseEntity.ok(
            orderService.getOrdersForUser(userId)
        );
    }

    // Customer: place an order.
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody CreateOrderRequest body,
            HttpServletRequest request) {

        Long userId = getUserId(request);

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in before checking out.");
        }

        try {
            Map<String, Object> order =
                    orderService.createOrder(
                        userId,
                        body.discountCode()
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(order);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Administrator: retrieve all orders.
    @GetMapping
    public ResponseEntity<?> getAllOrders(
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkAdmin(request);

        if (denied != null) {
            return denied;
        }

        return ResponseEntity.ok(
            orderService.getAllOrders()
        );
    }

    // Administrator: retrieve individual order details.
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetails(
            @PathVariable Long id,
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkAdmin(request);

        if (denied != null) {
            return denied;
        }

        Map<String, Object> order =
                orderService.getOrderDetails(id);

        if (order == null) {
            return ResponseEntity.status(404).body(
                Map.of("message", "Order not found.")
            );
        }

        return ResponseEntity.ok(order);
    }

    // Administrator: update order fulfillment status.
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateFulfillment(
            @PathVariable Long id,
            @RequestBody FulfillmentRequest body,
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkAdmin(request);

        if (denied != null) {
            return denied;
        }

        if (body == null || body.fulfilled() == null) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "message",
                    "Fulfillment status is required."
                )
            );
        }

        Map<String, Object> updated =
                orderService.updateFulfillment(
                    id,
                    body.fulfilled()
                );

        if (updated == null) {
            return ResponseEntity.status(404).body(
                Map.of("message", "Order not found.")
            );
        }

        return ResponseEntity.ok(updated);
    }
}
