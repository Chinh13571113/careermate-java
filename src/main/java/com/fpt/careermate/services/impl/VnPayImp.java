package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.dto.response.AccountResponse;

import java.util.Map;


public interface VnPayImp {
    String buildPaymentUrl(String orderInfo, String orderType, long amount, String bankCode, String locale) throws Exception;
    boolean validateChecksum(Map<String, String> params, String vnp_SecureHash) throws Exception;
}
