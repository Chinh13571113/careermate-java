package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.PermissionRequest;
import com.fpt.careermate.services.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse create(PermissionRequest request);
    List<PermissionResponse> getAll();
    void delete(String permission);

}
