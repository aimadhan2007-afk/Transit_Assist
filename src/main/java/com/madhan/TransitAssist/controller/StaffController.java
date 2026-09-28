package com.madhan.TransitAssist.controller;

import com.madhan.TransitAssist.dto.DashboardStatsDTO;
import com.madhan.TransitAssist.dto.WorkloadSummaryDTO;
import com.madhan.TransitAssist.exception.BusinessRuleException;
import com.madhan.TransitAssist.model.AssistanceRequest;
import com.madhan.TransitAssist.model.Helper;
import com.madhan.TransitAssist.model.RequestStatus;
import com.madhan.TransitAssist.model.User;
import com.madhan.TransitAssist.service.AssistanceRequestService;
import com.madhan.TransitAssist.service.HelperService;
import com.madhan.TransitAssist.service.StaffService;
import com.madhan.TransitAssist.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/staff")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffController {

    private final StaffService staffService;
    private final AssistanceRequestService requestService;
    private final HelperService helperService;
    private final UserService userService;

    public StaffController(StaffService staffService, 
                           AssistanceRequestService requestService, 
                           HelperService helperService, 
                           UserService userService) {
        this.staffService = staffService;
        this.requestService = requestService;
        this.helperService = helperService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String staffDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        DashboardStatsDTO stats = staffService.getStaffDashboardStats();
        List<WorkloadSummaryDTO> workload = staffService.getHelperWorkload(LocalDate.now());

        List<AssistanceRequest> recentRequests = requestService.getAllRequests();
        if (recentRequests.size() > 6) {
            recentRequests = recentRequests.subList(0, 6);
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("stats", stats);
        model.addAttribute("workload", workload);
        model.addAttribute("recentRequests", recentRequests);
        model.addAttribute("availableHelpers", helperService.getAvailableHelpers());

        return "staff/dashboard";
    }

    @GetMapping("/requests")
    public String manageRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userService.findByEmail(userDetails.getUsername());
        List<AssistanceRequest> requests;

        if (status != null) {
            requests = requestService.getRequestsByStatus(status);
        } else if (date != null) {
            requests = requestService.getRequestsByDate(date);
        } else {
            requests = requestService.getAllRequests();
        }

        List<Helper> availableHelpers = helperService.getAvailableHelpers();

        model.addAttribute("currentUser", user);
        model.addAttribute("requests", requests);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDate", date);
        model.addAttribute("allStatuses", RequestStatus.values());
        model.addAttribute("availableHelpers", availableHelpers);

        return "staff/requests";
    }

    @PostMapping("/requests/{requestId}/assign")
    public String assignHelper(@PathVariable Long requestId,
                               @RequestParam Long helperId,
                               RedirectAttributes redirectAttributes) {
        try {
            requestService.assignHelper(requestId, helperId);
            redirectAttributes.addFlashAttribute("successMessage", "Helper assigned successfully.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/requests";
    }

    @PostMapping("/requests/{requestId}/complete")
    public String completeRequest(@PathVariable Long requestId,
                                  RedirectAttributes redirectAttributes) {
        try {
            requestService.completeRequest(requestId);
            redirectAttributes.addFlashAttribute("successMessage", "Request marked as completed successfully.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/requests";
    }

    @GetMapping("/helpers/workload")
    public String helperWorkload(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userService.findByEmail(userDetails.getUsername());
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<WorkloadSummaryDTO> workload = staffService.getHelperWorkload(targetDate);

        model.addAttribute("currentUser", user);
        model.addAttribute("workload", workload);
        model.addAttribute("selectedDate", targetDate);

        return "staff/workload";
    }
}
