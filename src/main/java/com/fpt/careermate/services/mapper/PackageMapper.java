package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Package;
import com.fpt.careermate.services.dto.request.PackageCreationRequest;
import com.fpt.careermate.services.dto.response.PackageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PackageMapper {
    @Mapping(target = "candidates", ignore = true)
    Package toPackage(PackageCreationRequest request);

    PackageResponse toPackageResponse(Package pkg);

    List<PackageResponse> toPackageResponseList(List<Package> packages);
}
