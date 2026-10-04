package com.codegoats.viralvault.product;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();

    public ProductService() {
        products.add(new Product(1L, "Labubu", "Viral collectible figure", 39.99, 12, "/images/product-placeholder.svg"));
        products.add(new Product(2L, "Owala Water Bottle", "Popular insulated water bottle", 34.99, 8, "/images/product-placeholder.svg"));
        products.add(new Product(3L, "Smiski", "Glow-in-the-dark collectible figure", 14.99, 20, "/images/product-placeholder.svg"));
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public Product getProductById(Long id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public boolean removeProduct(Long id) {
        return products.removeIf(product -> product.getId().equals(id));
    }

    public List<Product> searchProducts(String searchTerm) {
        String term = searchTerm.toLowerCase();
        return products.stream().filter(product -> product.getName().toLowerCase().contains(term) || product.getDescription().toLowerCase().contains(term)).toList();
    }

    public List<Product> sortByPriceLowToHigh() {
        return products.stream().sorted(Comparator.comparingDouble(Product::getPrice)).toList();
    }

    public List<Product> sortByPriceHighToLow() {
        return products.stream().sorted(Comparator.comparingDouble(Product::getPrice).reversed()).toList();
    }

    public List<Product> sortByAvailability() {
        return products.stream().sorted(Comparator.comparingInt(Product::getQuantity).reversed()).toList();
    }
}