package com.fpt.careermate.services;

import com.fpt.careermate.repository.PermissionRepo;
import com.fpt.careermate.repository.RoleRepo;
import com.fpt.careermate.services.dto.request.RoleRequest;
import com.fpt.careermate.services.dto.response.RoleResponse;
import com.fpt.careermate.services.impl.RoleService;
import com.fpt.careermate.services.mapper.RoleMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleImp implements RoleService {
    RoleRepo roleRepository;
    PermissionRepo permissionRepository;
    RoleMapper roleMapper;
    @Override
    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);

        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);

    }

    @Override
    public List<RoleResponse> getAll() {
        return List.of();
    }

    @Override
    public void delete(String roleName) {

    }
}
