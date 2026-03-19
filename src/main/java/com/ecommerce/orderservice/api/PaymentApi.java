package com.ecommerce.orderservice.api;

import com.ecommerce.orderservice.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/orders/payment")
public class PaymentApi {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/create-vnpay-url")
    public ResponseEntity<?> createPaymentUrl(HttpServletRequest request) {
        // Giả sử thanh toán đơn hàng 150.000 VNĐ
        long amount = 150000L;
        String orderInfo = "Thanh toan don hang test";

        // Gọi service tạo URL
        String vnpayUrl = paymentService.createPaymentUrl(amount, orderInfo, request);

        return ResponseEntity.ok(Collections.singletonMap("paymentUrl", vnpayUrl));
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<?> paymentReturn(HttpServletRequest request) {
        int paymentStatus = paymentService.verifyPayment(request);

        if (paymentStatus == 1) {
            // TODO: Lấy Order ID từ request.getParameter("vnp_TxnRef") và cập nhật Database thành PAID
            return ResponseEntity.ok(Collections.singletonMap("message", "Thanh toán thành công!"));
        } else if (paymentStatus == 0) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Thanh toán thất bại!"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("message", "Dữ liệu bị giả mạo!"));
        }
    }
}