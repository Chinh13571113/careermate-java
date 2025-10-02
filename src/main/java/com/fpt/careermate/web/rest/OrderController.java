package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.OrderImp;
import com.fpt.careermate.services.dto.request.OrderCreationRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.OrderResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {

    OrderImp orderImp;

    @PostMapping
    public ApiResponse<String> createOrder(@RequestBody OrderCreationRequest request) {
        return ApiResponse.<String>builder()
                .result(orderImp.createOrder(request))
                .code(200)
                .message("success")
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteOrder(@PathVariable int id) {
        orderImp.deleteOrder(id);
        return ApiResponse.<String>builder()
                .result("")
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/status/{id}")
    public ApiResponse<String> checkOrderStatus(@PathVariable int id) {
        return ApiResponse.<String>builder()
                .result(orderImp.checkOrderStatus(id))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> getOrderList() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderImp.getOrderList())
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/my-order")
    public ApiResponse<List<OrderResponse>> myOrderList() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderImp.myOrderList())
                .code(200)
                .message("success")
                .build();
    }

}
