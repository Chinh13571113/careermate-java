package com.fpt.careermate.services.impl;


import com.fpt.careermate.services.dto.request.OrderCreationRequest;
import com.fpt.careermate.services.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    String createOrder(int packageId);
    void deleteOrder(int id);
    String checkOrderStatus(int id);
    List<OrderResponse> getOrderList();
    List<OrderResponse> myOrderList();
}
