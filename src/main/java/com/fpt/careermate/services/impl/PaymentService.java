package com.fpt.careermate.services.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

public interface PaymentService {
    String createPaymentUrl(HttpServletRequest httpServletRequest, long amount, String orderCode);
    String paymentReturn(HttpServletRequest request, Model model);
}
