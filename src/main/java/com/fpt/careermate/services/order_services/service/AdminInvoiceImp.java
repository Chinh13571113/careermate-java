package com.fpt.careermate.services.order_services.service;

import com.fpt.careermate.services.order_services.domain.RecruiterInvoice;
import com.fpt.careermate.services.order_services.repository.RecruiterInvoiceRepo;
import com.fpt.careermate.services.order_services.service.dto.response.AdminRecruiterInvoiceResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageAdminRecruiterInvoiceResponse;
import com.fpt.careermate.services.order_services.service.impl.AdminInvoiceService;
import com.fpt.careermate.services.order_services.service.mapper.AdminInvoiceMapper;
import com.fpt.careermate.services.order_services.service.mapper.RecruiterInvoiceMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminInvoiceImp implements AdminInvoiceService {

    RecruiterInvoiceRepo recruiterInvoiceRepo;
    AdminInvoiceMapper adminInvoiceMapper;

    // Admin lấy danh sách toàn bộ invoice của recruiter có filer
    @PreAuthorize("hasRole('ADMIN')")
    public PageAdminRecruiterInvoiceResponse getAllRecruiterInvoices(
            String status,
            Boolean isActive,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<RecruiterInvoice> invoices = recruiterInvoiceRepo.findAllWithFilters(
                status,
                isActive,
                pageable
        );

        PageAdminRecruiterInvoiceResponse adminRecruiterInvoiceResponsePage =
                adminInvoiceMapper.toAdminRecruiterInvoiceResponsePage(invoices);

        List<AdminRecruiterInvoiceResponse> adminRecruiterInvoiceResponses =
                adminInvoiceMapper.toAdminRecruiterInvoiceResponse(invoices.getContent());
        adminRecruiterInvoiceResponsePage.setContent(adminRecruiterInvoiceResponses);

        return adminRecruiterInvoiceResponsePage;
    }
}
