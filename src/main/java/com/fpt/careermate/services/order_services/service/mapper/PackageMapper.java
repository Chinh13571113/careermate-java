package com.fpt.careermate.services.order_services.service.mapper;

import com.fpt.careermate.services.order_services.domain.Package;
import com.fpt.careermate.services.order_services.service.dto.request.PackageCreationRequest;
import com.fpt.careermate.services.order_services.service.dto.response.PackageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PackageMapper {
    @Mapping(target = "candidateOrder", ignore = true)
    Package toPackage(PackageCreationRequest request);

    PackageResponse toPackageResponse(Package pkg);

    List<PackageResponse> toPackageResponseList(List<Package> packages);
}
