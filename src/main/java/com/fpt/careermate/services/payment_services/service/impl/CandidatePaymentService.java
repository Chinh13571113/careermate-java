package com.fpt.careermate.services.payment_services.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

public interface CandidatePaymentService {
    String createPaymentUrl(HttpServletRequest httpServletRequest, String orderCode);
    String paymentReturn(HttpServletRequest request, Model model);
}
