package com.madhan.TransitAssist.restcontroller;

import com.madhan.TransitAssist.dto.ApiResponse;
import com.madhan.TransitAssist.dto.HelperDTO;
import com.madhan.TransitAssist.model.Helper;
import com.madhan.TransitAssist.service.HelperService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpers")
public class HelperRestController {

    private final HelperService helperService;

    public HelperRestController(HelperService helperService) {
        this.helperService = helperService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Helper>> createHelper(@Valid @RequestBody HelperDTO dto) {
        Helper helper = helperService.createHelper(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Helper created successfully", helper));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Helper>>> getAllHelpers(
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly) {
        List<Helper> list = availableOnly ? helperService.getAvailableHelpers() : helperService.getAllHelpers();
        return ResponseEntity.ok(ApiResponse.success("Helpers retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Helper>> getHelperById(@PathVariable Long id) {
        Helper helper = helperService.getHelperById(id);
        return ResponseEntity.ok(ApiResponse.success("Helper retrieved successfully", helper));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Helper>> updateHelper(@PathVariable Long id, @Valid @RequestBody HelperDTO dto) {
        Helper helper = helperService.updateHelper(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Helper updated successfully", helper));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteHelper(@PathVariable Long id) {
        helperService.deleteHelper(id);
        return ResponseEntity.ok(ApiResponse.success("Helper deleted successfully"));
    }
}
