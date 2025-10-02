package com.fpt.careermate.services.impl;


import com.fpt.careermate.services.dto.request.PackageCreationRequest;
import com.fpt.careermate.services.dto.response.PackageResponse;

import java.util.List;

public interface PackageService {
    PackageResponse createPackage(PackageCreationRequest request);
    List<PackageResponse> getPackageList();
}
