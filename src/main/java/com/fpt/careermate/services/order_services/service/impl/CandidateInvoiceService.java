package com.fpt.careermate.services.order_services.service.impl;


import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateOrderResponse;

public interface CandidateInvoiceService {
    void cancelOrder(int id);
    MyCandidateOrderResponse myCandidatePackage();
}
