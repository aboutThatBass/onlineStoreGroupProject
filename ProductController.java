package com.codegoats.viralvault.product;

import com.codegoats.viralvault.user.User;
import com.codegoats.viralvault.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;
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

    public record ProductUpdateRequest(
        String name,
        String category,
        String description,
        Double price,
        Double salePrice,
        Integer quantity,
        String imageUrl
    ) {}

    // Public endpoint: retrieve all products.
    @GetMapping
    public List<Product> getAllProducts() {

        return productService.getAllProducts();
    }

    // Public endpoint: retrieve an individual product.
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id) {

        Product product = productService.getProductById(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }

    // Administrator authorization.
    private ResponseEntity<?> checkAdmin(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null ||
            !(session.getAttribute("userId") instanceof Number)) {

            return ResponseEntity.status(401).body(
                Map.of("message",
                    "Administrator authentication required.")
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
                Map.of("message",
                    "Administrator privileges required.")
            );
        }

        return null;
    }

    // Administrator endpoint: create a product.
    @PostMapping
    public ResponseEntity<?> addProduct(
            @RequestBody ProductUpdateRequest body,
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkAdmin(request);

        if (denied != null) {
            return denied;
        }

        if (body == null ||
            body.price() == null ||
            body.quantity() == null) {

            return ResponseEntity.badRequest().body(
                Map.of("message",
                    "Product price and quantity are required.")
            );
        }

        Product product = new Product(
            null,
            body.name(),
            body.category(),
            body.description(),
            body.price(),
            body.salePrice(),
            body.quantity(),
            body.imageUrl()
        );

        try {

            productService.addProduct(product);

            return ResponseEntity.status(201).body(
                Map.of("message",
                    "Product created successfully.")
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.status(409).body(
                Map.of("message",
                    "Product conflicts with existing data.")
            );
        }
    }

    // Administrator endpoint: update a product.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest body,
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkAdmin(request);

        if (denied != null) {
            return denied;
        }

        if (body == null ||
            body.price() == null ||
            body.quantity() == null) {

            return ResponseEntity.badRequest().body(
                Map.of("message",
                    "Product price and quantity are required.")
            );
        }

        Product existing = productService.getProductById(id);

        if (existing == null) {

            return ResponseEntity.status(404).body(
                Map.of("message", "Product not found.")
            );
        }

        Product product = new Product(
            id,
            body.name(),
            body.category() == null
                ? existing.getCategory()
                : body.category(),
            body.description(),
            body.price(),
            body.salePrice(),
            body.quantity(),
            body.imageUrl()
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

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.status(409).body(
                Map.of("message",
                    "Product conflicts with existing data.")
            );
        }
    }

    // Administrator endpoint: delete a product.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long id,
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkAdmin(request);

        if (denied != null) {
            return denied;
        }

        if (productService.getProductById(id) == null) {

            return ResponseEntity.status(404).body(
                Map.of("message", "Product not found.")
            );
        }

        try {

            boolean deleted =
                productService.removeProduct(id);

            if (!deleted) {

                return ResponseEntity.status(409).body(
                    Map.of("message",
                        "Product cannot be deleted because "
                        + "it is in an order or shopping cart, "
                        + "or no longer exists. "
                        + "Consider setting its quantity to zero.")
                );
            }

            return ResponseEntity.ok(
                Map.of("message",
                    "Product deleted successfully.")
            );

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.status(409).body(
                Map.of("message",
                    "Product is referenced by other records.")
            );
        }
    }
}