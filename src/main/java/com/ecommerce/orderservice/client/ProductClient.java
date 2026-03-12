package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.ProductDto;
import com.ecommerce.orderservice.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service", url = "${app.services.product-url}")
public interface ProductClient {

    // Copy y hệt chữ ký của Controller bên Product Service
    // Ví dụ bên kia là: @GetMapping("/api/products")
    @GetMapping("/api/products")
    List<ProductResponse> getAllProducts();

    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);

    @PutMapping("/api/products/reduce-quantity/{id}")
    void reduceProductQuantity(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);

}
