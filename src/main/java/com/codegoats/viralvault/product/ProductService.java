
package com.codegoats.viralvault.product;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product updateProduct(Long id, Product product) {
        if (product.getName() == null ||
            product.getName().isBlank()) {
            throw new IllegalArgumentException(
                "Product name is required."
            );
        }

        if (!Double.isFinite(product.getPrice()) ||
            product.getPrice() < 0) {
            throw new IllegalArgumentException(
                "Product price cannot be negative or invalid."
            );
        }

        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException(
                "Product quantity cannot be negative."
            );
        }

        product.setName(product.getName().trim());

        boolean updated = productRepository.updateProduct(
            id, product
        );

        if (!updated) {
            return null;
        }

        return productRepository.findById(id);
    }

    public void addProduct(Product product) {
        throw new UnsupportedOperationException(
            "Adding products to SQLite is not implemented yet."
        );
    }

    public boolean removeProduct(Long id) {
        throw new UnsupportedOperationException(
            "Removing products from SQLite is not implemented yet."
        );
    }

    public List<Product> searchProducts(String searchTerm) {
        String term = searchTerm.toLowerCase();

        return getAllProducts().stream()
            .filter(product ->
                product.getName().toLowerCase().contains(term) ||
                (product.getDescription() != null &&
                 product.getDescription().toLowerCase().contains(term))
            )
            .toList();
    }

    public List<Product> sortByPriceLowToHigh() {
        return getAllProducts().stream()
            .sorted(Comparator.comparingDouble(Product::getPrice))
            .toList();
    }

    public List<Product> sortByPriceHighToLow() {
        return getAllProducts().stream()
            .sorted(Comparator.comparingDouble(Product::getPrice).reversed())
            .toList();
    }

    public List<Product> sortByAvailability() {
        return getAllProducts().stream()
            .sorted(Comparator.comparingInt(Product::getQuantity).reversed())
            .toList();
    }
}
