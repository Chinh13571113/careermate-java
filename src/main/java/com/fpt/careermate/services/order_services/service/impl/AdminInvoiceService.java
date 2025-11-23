package com.fpt.careermate.services.order_services.service.impl;


import com.fpt.careermate.services.order_services.service.dto.response.PageAdminRecruiterInvoiceResponse;

public interface AdminInvoiceService {
    PageAdminRecruiterInvoiceResponse getAllRecruiterInvoices(
            String status,
            Boolean isActive,
            int page,
            int size
    );
}
