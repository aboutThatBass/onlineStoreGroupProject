package com.codegoats.viralvault.discount;

import com.codegoats.viralvault.user.User;
import com.codegoats.viralvault.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class DiscountController {

    private final DiscountService discountService;
    private final UserRepository userRepository;

    public DiscountController(
            DiscountService discountService,
            UserRepository userRepository) {

        this.discountService = discountService;
        this.userRepository = userRepository;
    }

    public record DiscountRequest(
        String code,
        Double discountPercent,
        Boolean isActive,
        String discountType,
        Double fixedAmount
    ) {}

    private ResponseEntity<?> checkAdmin(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("userId")
                    instanceof Number)) {

            return ResponseEntity.status(401).body(
                Map.of(
                    "message",
                    "Administrator authentication required."
                )
            );
        }

        Number userId =
            (Number) session.getAttribute("userId");

        boolean admin = userRepository
            .findById(userId.longValue())
            .map(User::isAdmin)
            .orElse(false);

        if (!admin) {
            return ResponseEntity.status(403).body(
                Map.of(
                    "message",
                    "Administrator privileges required."
                )
            );
        }

        return null;
    }

    // Administrator: view all discounts

    @GetMapping
    public ResponseEntity<?> getAllDiscounts(
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkAdmin(request);

        if (denied != null) {
            return denied;
        }

        return ResponseEntity.ok(
            discountService.getAllDiscounts()
        );
    }

    // Administrator: create a discount

    @PostMapping
    public ResponseEntity<?> createDiscount(
            @RequestBody DiscountRequest body,
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkAdmin(request);

        if (denied != null) {
            return denied;
        }

        if (body == null) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Request body is required.")
            );
        }

        try {

            Discount created = discountService.createDiscount(
                body.code(),
                body.discountPercent(),
                body.isActive(),
                body.discountType(),
                body.fixedAmount()
            );

            return ResponseEntity.status(201).body(created);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.status(409).body(
                Map.of("message", "Discount code already exists.")
            );
        }
    }

    // Administrator: edit or deactivate a discount

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateDiscount(
            @PathVariable Long id,
            @RequestBody DiscountRequest body,
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkAdmin(request);

        if (denied != null) {
            return denied;
        }

        if (body == null) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Request body is required.")
            );
        }

        try {

            Discount updated = discountService.patchDiscount(
                id,
                body.code(),
                body.discountPercent(),
                body.isActive(),
                body.discountType(),
                body.fixedAmount()
            );

            if (updated == null) {

                return ResponseEntity.status(404).body(
                    Map.of("message", "Discount not found.")
                );
            }

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.status(409).body(
                Map.of("message", "Discount code already exists.")
            );
        }
    }

    // Customer: validate an individual discount code

    @GetMapping("/validate")
    public ResponseEntity<?> validateDiscount(
            @RequestParam String code) {

        try {

            Discount discount =
                discountService.validateDiscount(code);

            return ResponseEntity.ok(
                Map.of(
                    "valid", true,
                    "code", discount.getCode(),
                    "discountType", discount.getDiscountType(),
                    "discountPercent", discount.getDiscountPercent(),
                    "fixedAmount", discount.getFixedAmount()
                )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                Map.of(
                    "valid", false,
                    "message", e.getMessage()
                )
            );
        }
    }
}