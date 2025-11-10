package com.fpt.careermate.services.order_services.service.impl;


import com.fpt.careermate.services.order_services.service.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    void deleteOrder(int id);
    String checkOrderStatus(int id);
    List<OrderResponse> getOrderList();
    List<OrderResponse> myOrderList();
}
