package com.fpt.careermate.web.rest;

import com.fpt.careermate.config.PaymentConfig;
import com.fpt.careermate.services.PackageImp;
import com.fpt.careermate.services.dto.request.PackageCreationRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.PackageResponse;
import com.fpt.careermate.services.impl.PackageService;
import com.fpt.careermate.services.impl.PaymentService;
import com.fpt.careermate.util.PaymentUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/package")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PackageController {

    PackageImp packageImp;

    @PostMapping
    public ApiResponse<PackageResponse> createPackage(@RequestBody PackageCreationRequest request) {
        return ApiResponse.<PackageResponse>builder()
                .result(packageImp.createPackage(request))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping
    public ApiResponse<List<PackageResponse>> getPackageList() {
        return ApiResponse.<List<PackageResponse>>builder()
                .result(packageImp.getPackageList())
                .code(200)
                .message("success")
                .build();
    }

}
