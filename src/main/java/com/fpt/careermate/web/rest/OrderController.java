package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.OrderImp;
import com.fpt.careermate.services.dto.request.OrderCreationRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "Order", description = "Manage order")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {

    OrderImp orderImp;

    @Operation(summary = "Create order")
    @PostMapping
    public ApiResponse<String> createOrder(
            @RequestParam
            @Min(value = 1, message = "packageId must be greater than or equal to 1")
            Integer packageId
    ) {
        return ApiResponse.<String>builder()
                .result(orderImp.createOrder(packageId))
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Delete order by ID")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteOrder(@Positive @PathVariable int id) {
        orderImp.deleteOrder(id);
        return ApiResponse.<String>builder()
                .result("")
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Check order status")
    @GetMapping("/status/{id}")
    public ApiResponse<String> checkOrderStatus(@Positive @PathVariable int id) {
        return ApiResponse.<String>builder()
                .result(orderImp.checkOrderStatus(id))
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Get order list for admin")
    @GetMapping
    public ApiResponse<List<OrderResponse>> getOrderList() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderImp.getOrderList())
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Get order list for candidate")
    @GetMapping("/my-order")
    public ApiResponse<List<OrderResponse>> myOrderList() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderImp.myOrderList())
                .code(200)
                .message("success")
                .build();
    }

}
