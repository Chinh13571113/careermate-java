package com.fpt.careermate.services.order_services.service.mapper;

import com.fpt.careermate.services.order_services.domain.CandidateInvoice;
import com.fpt.careermate.services.order_services.service.dto.response.CandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageCandidateOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "candidatePackage.name", target = "packageName")
    MyCandidateOrderResponse toOrderResponse(CandidateInvoice candidateInvoice);

    @Mapping(source = "candidatePackage.name", target = "packageName")
    @Mapping(source = "candidate.fullName", target = "candidateName")
    @Mapping(target = "isActive", ignore = true)
    CandidateOrderResponse toCandidateOrderResponse(CandidateInvoice candidateInvoice);

    // Chuyển page sang PageCandidateOrderResponse
    PageCandidateOrderResponse toPageCandidateOrderResponse(Page<CandidateInvoice> candidateOrderPage);
}
