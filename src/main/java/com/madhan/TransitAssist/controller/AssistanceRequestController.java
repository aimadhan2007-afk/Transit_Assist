package com.madhan.TransitAssist.controller;

import com.madhan.TransitAssist.dto.AssistanceRequestDTO;
import com.madhan.TransitAssist.exception.BusinessRuleException;
import com.madhan.TransitAssist.model.AssistanceRequest;
import com.madhan.TransitAssist.model.AssistanceType;
import com.madhan.TransitAssist.model.RequestStatus;
import com.madhan.TransitAssist.model.User;
import com.madhan.TransitAssist.service.AssistanceRequestService;
import com.madhan.TransitAssist.service.HelperService;
import com.madhan.TransitAssist.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/requests")
public class AssistanceRequestController {

    private final AssistanceRequestService requestService;
    private final UserService userService;
    private final HelperService helperService;

    public AssistanceRequestController(AssistanceRequestService requestService, 
                                       UserService userService, 
                                       HelperService helperService) {
        this.requestService = requestService;
        this.userService = userService;
        this.helperService = helperService;
    }

    @GetMapping("/new")
    public String newRequestForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        if (!model.containsAttribute("requestDTO")) {
            model.addAttribute("requestDTO", new AssistanceRequestDTO());
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("assistanceTypes", AssistanceType.values());
        return "requests/new";
    }

    @PostMapping("/new")
    public String submitRequest(@Valid @ModelAttribute("requestDTO") AssistanceRequestDTO dto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User user = userService.findByEmail(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", user);
            model.addAttribute("assistanceTypes", AssistanceType.values());
            return "requests/new";
        }

        try {
            requestService.createRequest(dto, user);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Your transit assistance request has been submitted successfully.");
            return "redirect:/requests/my";
        } catch (BusinessRuleException ex) {
            model.addAttribute("currentUser", user);
            model.addAttribute("assistanceTypes", AssistanceType.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "requests/new";
        }
    }

    @GetMapping("/my")
    public String myRequests(@RequestParam(value = "status", required = false) RequestStatus status,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<AssistanceRequest> list;

        if (status != null) {
            list = requestService.getRequestsByUser(user).stream()
                    .filter(r -> r.getStatus() == status)
                    .toList();
        } else {
            list = requestService.getRequestsByUser(user);
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("requests", list);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allStatuses", RequestStatus.values());

        return "requests/my";
    }

    @GetMapping("/{id}")
    public String requestDetails(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        AssistanceRequest request = requestService.getRequestById(id);

        model.addAttribute("currentUser", user);
        model.addAttribute("requestItem", request);
        model.addAttribute("availableHelpers", helperService.getAvailableHelpers());

        return "requests/details";
    }

    @PostMapping("/{id}/cancel")
    public String cancelRequest(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername());
        try {
            requestService.cancelRequest(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Request cancelled successfully.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/requests/" + id;
    }

    @PostMapping("/{id}/complete")
    public String completeRequest(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            requestService.completeRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Request marked as completed successfully.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/requests/" + id;
    }
}
