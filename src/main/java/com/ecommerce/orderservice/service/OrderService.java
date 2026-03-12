package com.ecommerce.orderservice.service;


import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.ProductDto;
import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.repo.OrderRepo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {

    private final ProductClient productClient;
    private final OrderRepo orderRepository;

    // Constructor Injection (Thay vì @Autowired)
    public OrderService(ProductClient productClient, OrderRepo orderRepository,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.productClient = productClient;
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setPrice(orderRequest.getPrice());
        order.setQuantity(orderRequest.getQuantity());
        order.setSkuCode(orderRequest.getSkuCode());
        order.setEmail(orderRequest.getEmail()); // Giả sử request có email

        // Lưu đơn hàng vào DB
        orderRepository.save(order);

        // 2. Gửi sự kiện sang Kafka
        // Topic: "notificationTopic" (Tên này phải khớp với bên nhận)
        String topicName = "notificationTopic";
        OrderPlacedEvent event = new OrderPlacedEvent(order.getOrderNumber(), "user@example.com"); // Hardcode email tạm nếu request chưa có

        kafkaTemplate.send(topicName, event);

        System.out.println("DEBUG: Đã gửi tin nhắn Kafka cho Order: " + order.getOrderNumber());
        return topicName;
    }


    @Transactional // Thêm cái này để nếu lỗi thì rollback
    @CircuitBreaker(name = "productService", fallbackMethod = "createOrderFallback")
    public OrderResponse createOrder(OrderRequest request) {
        // 1. GỌI SANG PRODUCT SERVICE CHECK HÀNG
        ProductDto product = productClient.getProductById(request.getProductId());

        if (product == null) {
            throw new BusinessException("Sản phẩm không tồn tại (Product ID: " + request.getProductId() + ")");
        }

        // 2. Check tồn kho sơ bộ
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException("Sản phẩm " + product.getName() + " đã hết hàng!");
        }

        // 3. --- TRỪ TỒN KHO (GỌI SANG PRODUCT SERVICE) ---
        // Nếu bên kia lỗi (hết hàng), nó sẽ ném lỗi và dừng luôn tại đây
        productClient.reduceProductQuantity(request.getProductId(), request.getQuantity());

        // 4. Tạo đơn hàng
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setSkuCode(product.getName());
        order.setProductId(product.getId());
        order.setPrice(BigDecimal.valueOf(product.getPrice()));
        order.setQuantity(request.getQuantity());
        order.setEmail(request.getEmail());

        // 5. Lưu vào Database
        orderRepository.save(order);
        placeOrder(request);
        return mapToResponse(order);
    }

    // --- HÀM FALLBACK (DỰ PHÒNG) ---
    // Yêu cầu: Cùng kiểu trả về, cùng tham số với hàm chính, cộng thêm tham số Exception
    public OrderResponse createOrderFallback(OrderRequest request, Throwable t) {
        // Logic xử lý khi Product Service bị sập
        // Ví dụ: Bạn có thể lưu đơn hàng vào một hàng đợi (Kafka) để xử lý sau,
        // hoặc đơn giản là ném ra một lỗi thông báo rõ ràng cho user.

        System.err.println("Lỗi khi gọi Product Service: " + t.getMessage());

        throw new BusinessException("Hệ thống đang bảo trì phần kho vận, vui lòng quay lại sau! (Fallback active)");
    }

    // Hàm phụ trợ để map Entity sang DTO
    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getSkuCode(),
                order.getPrice(),
                order.getQuantity(),
                order.getStatus(),
                order.getEmail()
        );
    }
}
