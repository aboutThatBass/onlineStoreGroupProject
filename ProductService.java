package com.codegoats.viralvault.product;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    private void validateProduct(Product product) {

        if (product.getName() == null ||
            product.getName().isBlank()) {

            throw new IllegalArgumentException(
                "Product name is required."
            );
        }

        if (product.getName().trim().length() > 100) {

            throw new IllegalArgumentException(
                "Product name cannot exceed 100 characters."
            );
        }

        if (product.getCategory() == null ||
            product.getCategory().isBlank()) {

            throw new IllegalArgumentException(
                "Product category is required."
            );
        }

        if (product.getCategory().trim().length() > 50) {

            throw new IllegalArgumentException(
                "Category cannot exceed 50 characters."
            );
        }

        if (!Double.isFinite(product.getPrice()) ||
            product.getPrice() < 0 ||
            product.getPrice() > 99999999.99) {

            throw new IllegalArgumentException(
                "Please enter a valid product price."
            );
        }

        if (product.getSalePrice() != null) {
            if (!Double.isFinite(product.getSalePrice()) ||
                product.getSalePrice() < 0 ||
                product.getSalePrice() >= product.getPrice()) {

                throw new IllegalArgumentException(
                    "Sale price must be non-negative and less than the regular price."
                );
            }
            product.setSalePrice(
                Math.round(product.getSalePrice() * 100.0) / 100.0
            );
        }

        if (product.getQuantity() < 0) {

            throw new IllegalArgumentException(
                "Product quantity cannot be negative."
            );
        }

        if (product.getImageUrl() != null &&
            product.getImageUrl().length() > 255) {

            throw new IllegalArgumentException(
                "Image URL cannot exceed 255 characters."
            );
        }

        product.setName(product.getName().trim());
        product.setCategory(product.getCategory().trim());

        product.setPrice(
            Math.round(product.getPrice() * 100.0) / 100.0
        );
    }

    public void addProduct(Product product) {

        validateProduct(product);
        productRepository.addProduct(product);
    }

    public Product updateProduct(Long id, Product product) {

        validateProduct(product);

        boolean updated =
            productRepository.updateProduct(id, product);

        if (!updated) {
            return null;
        }

        return productRepository.findById(id);
    }

    public boolean removeProduct(Long id) {

        return productRepository.deleteProduct(id);
    }

    public List<Product> searchProducts(String searchTerm) {

        String term = searchTerm == null
            ? ""
            : searchTerm.toLowerCase(Locale.ROOT);

        return getAllProducts().stream()
            .filter(product ->
                product.getName()
                    .toLowerCase(Locale.ROOT).contains(term) ||

                (product.getCategory() != null &&
                 product.getCategory()
                    .toLowerCase(Locale.ROOT).contains(term)) ||

                (product.getDescription() != null &&
                 product.getDescription()
                    .toLowerCase(Locale.ROOT).contains(term))
            )
            .toList();
    }

    public List<Product> sortByPriceLowToHigh() {

        return getAllProducts().stream()
            .sorted(
                Comparator.comparingDouble(Product::getPrice)
            )
            .toList();
    }

    public List<Product> sortByPriceHighToLow() {

        return getAllProducts().stream()
            .sorted(
                Comparator.comparingDouble(Product::getPrice)
                    .reversed()
            )
            .toList();
    }

    public List<Product> sortByAvailability() {

        return getAllProducts().stream()
            .sorted(
                Comparator.comparingInt(Product::getQuantity)
                    .reversed()
            )
            .toList();
    }
}