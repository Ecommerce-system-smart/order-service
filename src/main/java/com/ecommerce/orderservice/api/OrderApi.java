package com.ecommerce.orderservice.api;

import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.ProductResponse;

import com.ecommerce.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.orderservice.utils.EncryptionUtil;

@RestController
@RequestMapping("/api/orders")

public class OrderApi {
    private final ProductClient productClient;
    private final OrderService orderService;

    public OrderApi(ProductClient productClient, OrderService orderService) {
        this.productClient = productClient;
        this.orderService = orderService;
    }

    @GetMapping("/test-feign")
    public String testFeign() {
        // Gọi sang Product Service lấy danh sách
        List<ProductResponse> products = productClient.getAllProducts();

        return "Kết nối thành công! Lấy được " + products.size() + " sản phẩm từ Product Service.";
    }

    @PostMapping("/order-buy")
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        // Controller chỉ làm 1 việc: Gọi Service xử lý và trả kết quả về
        return orderService.placeOrder(orderRequest);
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody Map<String, String> requestMap) {
        try {
            String decrypted = EncryptionUtil.decrypt(requestMap.get("payload"));
            OrderRequest request = new ObjectMapper().readValue(decrypted, OrderRequest.class);
            OrderResponse response = orderService.createOrder(request);
            String json = new ObjectMapper().writeValueAsString(response);
            return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("payload", EncryptionUtil.encrypt(json)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

}
