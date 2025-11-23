package com.fpt.careermate.services.order_services.service;

import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.services.order_services.repository.RecruiterInvoiceRepo;
import com.fpt.careermate.services.order_services.repository.RecruiterPackageRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminInvoiceImp  {

    RecruiterInvoiceRepo recruiterInvoiceRepo;
    RecruiterPackageRepo recruiterPackageRepo;
    CoachUtil coachUtil;


}
