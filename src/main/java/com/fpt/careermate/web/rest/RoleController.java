package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.RoleImp;
import com.fpt.careermate.services.dto.request.RoleRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Role", description = "Manage role")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleImp roleImp;

    @Operation(summary = "Create new role", description = "Create a new role")
    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleImp.create(request))
                .build();
    }

    @Operation(summary = "Get all roles", description = "Get a list of all roles")
    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleImp.getAll())
                .build();
    }

    @Operation(summary = "Delete a role", description = "Delete a role by its name")
    @DeleteMapping("/{role}")
    ApiResponse<Void> delete(@PathVariable String role) {
        roleImp.delete(role);
        return ApiResponse.<Void>builder().build();
    }
}
