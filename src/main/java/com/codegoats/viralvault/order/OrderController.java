
package com.codegoats.viralvault.order;

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

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
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

    public record CreateOrderRequest(String discountCode) {
    }

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
}
