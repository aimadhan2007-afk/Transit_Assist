package com.madhan.TransitAssist.restcontroller;

import com.madhan.TransitAssist.dto.ApiResponse;
import com.madhan.TransitAssist.dto.WorkloadSummaryDTO;
import com.madhan.TransitAssist.model.AssistanceRequest;
import com.madhan.TransitAssist.model.RequestStatus;
import com.madhan.TransitAssist.service.AssistanceRequestService;
import com.madhan.TransitAssist.service.StaffService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffRestController {

    private final AssistanceRequestService requestService;
    private final StaffService staffService;

    public StaffRestController(AssistanceRequestService requestService, StaffService staffService) {
        this.requestService = requestService;
        this.staffService = staffService;
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<AssistanceRequest>>> getRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AssistanceRequest> list;
        if (status != null) {
            list = requestService.getRequestsByStatus(status);
        } else if (date != null) {
            list = requestService.getRequestsByDate(date);
        } else {
            list = requestService.getAllRequests();
        }

        return ResponseEntity.ok(ApiResponse.success("Staff requests retrieved successfully", list));
    }

    @PutMapping("/requests/{requestId}/assign/{helperId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assignHelper(
            @PathVariable Long requestId,
            @PathVariable Long helperId) {

        AssistanceRequest request = requestService.assignHelper(requestId, helperId);

        Map<String, Object> data = new HashMap<>();
        data.put("id", request.getId());
        data.put("status", request.getStatus().name());
        data.put("helperId", request.getHelper().getId());
        data.put("helperName", request.getHelper().getName());

        return ResponseEntity.ok(ApiResponse.success("Helper assigned successfully", data));
    }

    @GetMapping("/workload")
    public ResponseEntity<ApiResponse<List<WorkloadSummaryDTO>>> getWorkload(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<WorkloadSummaryDTO> workload = staffService.getHelperWorkload(targetDate);

        return ResponseEntity.ok(ApiResponse.success("Helper workload retrieved successfully", workload));
    }
}
