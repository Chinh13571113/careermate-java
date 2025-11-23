package com.fpt.careermate.services.order_services.service;

import com.fpt.careermate.services.order_services.repository.CandidatePackageRepo;
import com.fpt.careermate.services.order_services.service.dto.request.PackageCreationRequest;
import com.fpt.careermate.services.order_services.service.dto.response.PackageResponse;
import com.fpt.careermate.services.order_services.service.impl.PackageService;
import com.fpt.careermate.services.order_services.service.mapper.PackageMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PackageImp implements PackageService {

    CandidatePackageRepo candidatePackageRepo;
    PackageMapper packageMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public PackageResponse createPackage(PackageCreationRequest request) {
        return packageMapper.toPackageResponse(candidatePackageRepo.save(packageMapper.toPackage(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<PackageResponse> getPackageList() {
        return packageMapper.toPackageResponseList(candidatePackageRepo.findAll());
    }

}
