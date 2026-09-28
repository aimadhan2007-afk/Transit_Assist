package com.madhan.TransitAssist.restcontroller;

import com.madhan.TransitAssist.dto.ApiResponse;
import com.madhan.TransitAssist.dto.DashboardStatsDTO;
import com.madhan.TransitAssist.dto.RegisterRequest;
import com.madhan.TransitAssist.model.User;
import com.madhan.TransitAssist.service.StaffService;
import com.madhan.TransitAssist.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private final UserService userService;
    private final StaffService staffService;

    public AdminRestController(UserService userService, StaffService staffService) {
        this.userService = userService;
        this.staffService = staffService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleUserStatus(@PathVariable Long id) {
        User user = userService.toggleUserStatus(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("email", user.getEmail());
        data.put("enabled", user.isEnabled());

        String statusStr = user.isEnabled() ? "enabled" : "disabled";
        return ResponseEntity.ok(ApiResponse.success("User account " + statusStr + " successfully", data));
    }

    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createStaff(@Valid @RequestBody RegisterRequest request) {
        User staff = userService.createStaff(request);
        Map<String, Object> data = new HashMap<>();
        data.put("id", staff.getId());
        data.put("fullName", staff.getFullName());
        data.put("email", staff.getEmail());
        data.put("role", staff.getRole().name());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff account created successfully", data));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getSystemStats() {
        DashboardStatsDTO stats = staffService.getStaffDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("System statistics retrieved successfully", stats));
    }
}
