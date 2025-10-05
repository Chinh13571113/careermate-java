package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.RoleRequest;
import com.fpt.careermate.services.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    RoleResponse create(RoleRequest request);
    List<RoleResponse> getAll();
    void delete(String roleName);
}
