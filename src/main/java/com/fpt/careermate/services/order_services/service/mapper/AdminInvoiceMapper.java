package com.fpt.careermate.services.order_services.service.mapper;

import com.fpt.careermate.services.order_services.domain.RecruiterInvoice;
import com.fpt.careermate.services.order_services.service.dto.response.AdminRecruiterInvoiceResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyRecruiterInvoiceResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageAdminRecruiterInvoiceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminInvoiceMapper {
    @Mapping(source = "recruiter.account.email", target = "recruiterEmail")
    @Mapping(source = "recruiterPackage.name", target = "packageName")
    AdminRecruiterInvoiceResponse toAdminRecruiterInvoiceResponse(RecruiterInvoice recruiterInvoice);
    List<AdminRecruiterInvoiceResponse> toAdminRecruiterInvoiceResponse(List<RecruiterInvoice> recruiterInvoices);

    PageAdminRecruiterInvoiceResponse toAdminRecruiterInvoiceResponsePage(Page<RecruiterInvoice> recruiterInvoices);
}
