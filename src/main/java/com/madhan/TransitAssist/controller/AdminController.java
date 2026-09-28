package com.madhan.TransitAssist.controller;

import com.madhan.TransitAssist.dto.DashboardStatsDTO;
import com.madhan.TransitAssist.dto.RegisterRequest;
import com.madhan.TransitAssist.dto.WorkloadSummaryDTO;
import com.madhan.TransitAssist.exception.BusinessRuleException;
import com.madhan.TransitAssist.model.AssistanceRequest;
import com.madhan.TransitAssist.model.Role;
import com.madhan.TransitAssist.model.User;
import com.madhan.TransitAssist.service.AssistanceRequestService;
import com.madhan.TransitAssist.service.HelperService;
import com.madhan.TransitAssist.service.StaffService;
import com.madhan.TransitAssist.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final StaffService staffService;
    private final AssistanceRequestService requestService;
    private final HelperService helperService;

    public AdminController(UserService userService, 
                           StaffService staffService, 
                           AssistanceRequestService requestService, 
                           HelperService helperService) {
        this.userService = userService;
        this.staffService = staffService;
        this.requestService = requestService;
        this.helperService = helperService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        DashboardStatsDTO stats = staffService.getStaffDashboardStats();
        List<AssistanceRequest> recentRequests = requestService.getAllRequests();
        if (recentRequests.size() > 5) {
            recentRequests = recentRequests.subList(0, 5);
        }

        List<User> recentUsers = userService.getAllUsers();
        if (recentUsers.size() > 5) {
            recentUsers = recentUsers.subList(0, 5);
        }

        List<WorkloadSummaryDTO> workload = staffService.getHelperWorkload(LocalDate.now());

        model.addAttribute("currentUser", user);
        model.addAttribute("stats", stats);
        model.addAttribute("recentRequests", recentRequests);
        model.addAttribute("recentUsers", recentUsers);
        model.addAttribute("workload", workload);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(@RequestParam(value = "role", required = false) Role role,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User currentUser = userService.findByEmail(userDetails.getUsername());
        List<User> userList;

        if (role != null) {
            userList = userService.getUsersByRole(role);
        } else {
            userList = userService.getAllUsers();
        }

        if (!model.containsAttribute("staffRequest")) {
            model.addAttribute("staffRequest", new RegisterRequest());
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", userList);
        model.addAttribute("selectedRole", role);
        model.addAttribute("allRoles", Role.values());

        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User updated = userService.toggleUserStatus(id);
            String statusStr = updated.isEnabled() ? "enabled" : "disabled";
            redirectAttributes.addFlashAttribute("successMessage", "User account " + statusStr + " successfully.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/staff/new")
    public String createStaffUser(@Valid @ModelAttribute("staffRequest") RegisterRequest request,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid staff details. Please check the fields.");
            return "redirect:/admin/users";
        }

        try {
            userService.createStaff(request);
            redirectAttributes.addFlashAttribute("successMessage", "Staff account created successfully.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/users";
    }
}
