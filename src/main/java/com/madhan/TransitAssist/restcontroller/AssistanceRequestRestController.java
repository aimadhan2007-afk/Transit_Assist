package com.madhan.TransitAssist.restcontroller;

import com.madhan.TransitAssist.dto.ApiResponse;
import com.madhan.TransitAssist.dto.AssistanceRequestDTO;
import com.madhan.TransitAssist.exception.UnauthorizedException;
import com.madhan.TransitAssist.model.AssistanceRequest;
import com.madhan.TransitAssist.model.Role;
import com.madhan.TransitAssist.model.User;
import com.madhan.TransitAssist.service.AssistanceRequestService;
import com.madhan.TransitAssist.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class AssistanceRequestRestController {

    private final AssistanceRequestService requestService;
    private final UserService userService;

    public AssistanceRequestRestController(AssistanceRequestService requestService, UserService userService) {
        this.requestService = requestService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createRequest(
            @Valid @RequestBody AssistanceRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("Authentication required to create a request.");
        }

        User user = userService.findByEmail(userDetails.getUsername());
        AssistanceRequest request = requestService.createRequest(dto, user);

        Map<String, Object> data = new HashMap<>();
        data.put("id", request.getId());
        data.put("status", request.getStatus().name());
        data.put("assistanceType", request.getAssistanceType().name());
        data.put("pickupPoint", request.getPickupPoint());
        data.put("travelDate", request.getTravelDate().toString());
        data.put("travelTime", request.getTravelTime().toString());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assistance request created successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssistanceRequest>> getRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByEmail(userDetails.getUsername());
        AssistanceRequest request = requestService.getRequestById(id);

        if (currentUser.getRole() == Role.USER && !request.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this request.");
        }

        return ResponseEntity.ok(ApiResponse.success("Assistance request fetched successfully", request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AssistanceRequest>>> getRequests(
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByEmail(userDetails.getUsername());
        List<AssistanceRequest> list;

        if (currentUser.getRole() == Role.STAFF || currentUser.getRole() == Role.ADMIN) {
            list = requestService.getAllRequests();
        } else {
            list = requestService.getRequestsByUser(currentUser);
        }

        return ResponseEntity.ok(ApiResponse.success("Assistance requests fetched successfully", list));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByEmail(userDetails.getUsername());
        AssistanceRequest request = requestService.cancelRequest(id, currentUser);

        Map<String, Object> data = new HashMap<>();
        data.put("id", request.getId());
        data.put("status", request.getStatus().name());

        return ResponseEntity.ok(ApiResponse.success("Request cancelled successfully", data));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> completeRequest(@PathVariable Long id) {
        AssistanceRequest request = requestService.completeRequest(id);

        Map<String, Object> data = new HashMap<>();
        data.put("id", request.getId());
        data.put("status", request.getStatus().name());

        return ResponseEntity.ok(ApiResponse.success("Request marked as completed successfully", data));
    }
}
