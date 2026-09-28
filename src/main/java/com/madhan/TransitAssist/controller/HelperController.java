package com.madhan.TransitAssist.controller;

import com.madhan.TransitAssist.dto.HelperDTO;
import com.madhan.TransitAssist.exception.BusinessRuleException;
import com.madhan.TransitAssist.model.Helper;
import com.madhan.TransitAssist.model.HelperAvailability;
import com.madhan.TransitAssist.model.User;
import com.madhan.TransitAssist.service.HelperService;
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

import java.util.List;

@Controller
@RequestMapping("/staff/helpers")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class HelperController {

    private final HelperService helperService;
    private final UserService userService;

    public HelperController(HelperService helperService, UserService userService) {
        this.helperService = helperService;
        this.userService = userService;
    }

    @GetMapping
    public String listHelpers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Helper> helpers = helperService.getAllHelpers();

        if (!model.containsAttribute("helperDTO")) {
            model.addAttribute("helperDTO", new HelperDTO());
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("helpers", helpers);
        model.addAttribute("availabilities", HelperAvailability.values());

        return "staff/helpers";
    }

    @PostMapping
    public String createHelper(@Valid @ModelAttribute("helperDTO") HelperDTO dto,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            User user = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", user);
            model.addAttribute("helpers", helperService.getAllHelpers());
            model.addAttribute("availabilities", HelperAvailability.values());
            return "staff/helpers";
        }

        helperService.createHelper(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Helper created successfully.");
        return "redirect:/staff/helpers";
    }

    @PostMapping("/{id}/update")
    public String updateHelper(@PathVariable Long id,
                               @Valid @ModelAttribute("helperDTO") HelperDTO dto,
                               RedirectAttributes redirectAttributes) {
        try {
            helperService.updateHelper(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Helper updated successfully.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/helpers";
    }

    @PostMapping("/{id}/availability")
    public String updateAvailability(@PathVariable Long id,
                                     @RequestParam HelperAvailability availability,
                                     RedirectAttributes redirectAttributes) {
        helperService.updateAvailability(id, availability);
        redirectAttributes.addFlashAttribute("successMessage", "Helper status updated to " + availability);
        return "redirect:/staff/helpers";
    }

    @PostMapping("/{id}/delete")
    public String deleteHelper(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            helperService.deleteHelper(id);
            redirectAttributes.addFlashAttribute("successMessage", "Helper removed successfully.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/helpers";
    }
}
