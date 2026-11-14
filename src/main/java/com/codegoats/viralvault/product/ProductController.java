
package com.codegoats.viralvault.product;

import com.codegoats.viralvault.user.User;
import com.codegoats.viralvault.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(
            ProductService productService,
            UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    // Retrieve all products.
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // Retrieve an individual product.
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id) {

        Product product = productService.getProductById(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }

    // Request body for product updates.
    public record ProductUpdateRequest(
            String name,
            String description,
            Double price,
            Integer quantity,
            String imageUrl) {
    }

    // Update an existing product (administrator only).
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest update,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("userId") instanceof Long)) {

            return ResponseEntity.status(401).body(
                Map.of(
                    "message",
                    "Administrator authentication required."
                )
            );
        }

        if (!isAdministrator(session)) {
            return ResponseEntity.status(403).body(
                Map.of(
                    "message",
                    "Administrator privileges required."
                )
            );
        }

        if (update == null
                || update.price() == null
                || update.quantity() == null) {

            return ResponseEntity.badRequest().body(
                Map.of(
                    "message",
                    "Product price and quantity are required."
                )
            );
        }

        Product product = new Product(
            id,
            update.name(),
            update.description(),
            update.price(),
            update.quantity(),
            update.imageUrl()
        );

        try {
            Product updated =
                productService.updateProduct(id, product);

            if (updated == null) {
                return ResponseEntity.status(404).body(
                    Map.of("message", "Product not found.")
                );
            }

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    // Verify that the current session belongs to an admin.
    private boolean isAdministrator(HttpSession session) {

        Object userId = session.getAttribute("userId");

        if (!(userId instanceof Long)) {
            return false;
        }

        return userRepository.findById((Long) userId)
            .map(User::isAdmin)
            .orElse(false);
    }
}
