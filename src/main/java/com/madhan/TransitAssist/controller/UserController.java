package com.madhan.TransitAssist.controller;

import com.madhan.TransitAssist.dto.DashboardStatsDTO;
import com.madhan.TransitAssist.model.AssistanceRequest;
import com.madhan.TransitAssist.model.RequestStatus;
import com.madhan.TransitAssist.model.Role;
import com.madhan.TransitAssist.model.User;
import com.madhan.TransitAssist.service.AssistanceRequestService;
import com.madhan.TransitAssist.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserController {

    private final UserService userService;
    private final AssistanceRequestService requestService;

    public UserController(UserService userService, AssistanceRequestService requestService) {
        this.userService = userService;
        this.requestService = requestService;
    }

    @GetMapping("/dashboard")
    public String userDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());

        // Redirect STAFF and ADMIN to their respective dashboards if they land on /dashboard
        if (user.getRole() == Role.STAFF) {
            return "redirect:/staff/dashboard";
        }
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        }

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalRequests(requestService.countTotalByUser(user));
        stats.setRequestedCount(requestService.countByUserAndStatus(user, RequestStatus.REQUESTED));
        stats.setAssignedCount(requestService.countByUserAndStatus(user, RequestStatus.ASSIGNED));
        stats.setCompletedCount(requestService.countByUserAndStatus(user, RequestStatus.COMPLETED));
        stats.setCancelledCount(requestService.countByUserAndStatus(user, RequestStatus.CANCELLED));

        List<AssistanceRequest> recentRequests = requestService.getRequestsByUser(user);
        if (recentRequests.size() > 5) {
            recentRequests = recentRequests.subList(0, 5);
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("stats", stats);
        model.addAttribute("recentRequests", recentRequests);

        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("currentUser", user);
        return "profile";
    }
}
