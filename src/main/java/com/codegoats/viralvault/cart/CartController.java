
package com.codegoats.viralvault.cart;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
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

    @GetMapping
    public ResponseEntity<?> getCart(HttpServletRequest request) {
        Long userId = getUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> setQuantity(
        @PathVariable Long productId,
        @RequestBody Map<String, Integer> body,
        HttpServletRequest request
    ) {
        Long userId = getUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer quantity = body.get("quantity");

        if (quantity == null) {
            return ResponseEntity.badRequest().body("Quantity is required.");
        }

        try {
            return ResponseEntity.ok(
                cartService.setQuantity(userId, productId, quantity)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeItem(
        @PathVariable Long productId,
        HttpServletRequest request
    ) {
        Long userId = getUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
            cartService.removeItem(userId, productId)
        );
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        Long userId = getUserId(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(cartService.clearCart(userId));
    }
}
