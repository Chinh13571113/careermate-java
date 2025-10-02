package com.fpt.careermate.web.rest;

import com.fpt.careermate.config.PaymentConfig;
import com.fpt.careermate.services.PaymentImp;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.impl.PaymentService;
import com.fpt.careermate.util.PaymentUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentController {

    private final PaymentImp paymentImp;
    private final PaymentConfig paymentConfig;
    private final PaymentUtil paymentUtil;

    @PostMapping
    public ApiResponse<String> createPayment(
            @RequestParam long amount,
            @RequestParam String orderCode,
            HttpServletRequest httpServletRequest) {
        String paymentUrl = paymentImp.createPaymentUrl(httpServletRequest, amount, orderCode);

        return ApiResponse.<String>builder()
                .result(paymentUrl)
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/return")
    public ApiResponse<String> paymentReturn(HttpServletRequest httpServletRequest, Model model){
        return ApiResponse.<String>builder()
                .result(paymentImp.paymentReturn(httpServletRequest, model))
                .code(200)
                .build();
    }
}
