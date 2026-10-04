package com.codegoats.viralvault.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.codegoats.viralvault.product.ProductService;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("welcomeMessage", "Trending finds in one place.");
        model.addAttribute("products", productService.getAllProducts());      
        return "index";
    }
}

