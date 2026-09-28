package com.madhan.TransitAssist;

import com.madhan.TransitAssist.dto.AssistanceRequestDTO;
import com.madhan.TransitAssist.dto.HelperDTO;
import com.madhan.TransitAssist.dto.RegisterRequest;
import com.madhan.TransitAssist.exception.BusinessRuleException;
import com.madhan.TransitAssist.model.*;
import com.madhan.TransitAssist.service.AssistanceRequestService;
import com.madhan.TransitAssist.service.HelperService;
import com.madhan.TransitAssist.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class TransitAssistApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private AssistanceRequestService requestService;

    @Test
    @DisplayName("Context Loads & Default Admin/Staff Accounts Initialized")
    void contextLoads() {
        assertNotNull(userService.findByEmail("admin@transitassist.com"));
        assertNotNull(userService.findByEmail("staff@transitassist.com"));
        assertEquals(Role.ADMIN, userService.findByEmail("admin@transitassist.com").getRole());
        assertEquals(Role.STAFF, userService.findByEmail("staff@transitassist.com").getRole());
    }

    @Test
    @DisplayName("RULE 7: Required request information must be valid")
    void testRequestCreation() {
        User user = userService.findByEmail("madhan@example.com");
        AssistanceRequestDTO dto = new AssistanceRequestDTO(
                AssistanceType.WHEELCHAIR,
                "Main Hostel Gate",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                "Need wheelchair assistance"
        );

        AssistanceRequest request = requestService.createRequest(dto, user);
        assertNotNull(request.getId());
        assertEquals(RequestStatus.REQUESTED, request.getStatus());
        assertEquals(user.getId(), request.getUser().getId());
    }

    @Test
    @DisplayName("RULE 1: Helper cannot be assigned to overlapping-time requests")
    void testOverlappingRequestAssignment() {
        User user = userService.findByEmail("madhan@example.com");
        LocalDate travelDate = LocalDate.now().plusDays(2);

        // Helper John
        Helper helper = helperService.createHelper(new HelperDTO("Test Helper", "9988776655", "Wheelchair", HelperAvailability.AVAILABLE));

        // Request 1 at 10:00
        AssistanceRequestDTO req1Dto = new AssistanceRequestDTO(
                AssistanceType.WHEELCHAIR, "Hostel A", travelDate, LocalTime.of(10, 0), "Trip 1");
        AssistanceRequest req1 = requestService.createRequest(req1Dto, user);

        // Assign helper to Request 1
        requestService.assignHelper(req1.getId(), helper.getId());
        assertEquals(RequestStatus.ASSIGNED, requestService.getRequestById(req1.getId()).getStatus());

        // Reset helper availability for test simulation of overlapping booking attempt
        helperService.updateAvailability(helper.getId(), HelperAvailability.AVAILABLE);

        // Request 2 at 10:30 (within 60 min overlap window)
        AssistanceRequestDTO req2Dto = new AssistanceRequestDTO(
                AssistanceType.ESCORT, "Hostel B", travelDate, LocalTime.of(10, 30), "Trip 2");
        AssistanceRequest req2 = requestService.createRequest(req2Dto, user);

        // Attempting to assign same helper should throw BusinessRuleException
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            requestService.assignHelper(req2.getId(), helper.getId());
        });

        assertTrue(ex.getMessage().contains("already have another request during this time"));
    }

    @Test
    @DisplayName("RULE 2 & 4: Completed request cannot be cancelled or reassigned")
    void testCompletedRequestImmutability() {
        User user = userService.findByEmail("madhan@example.com");
        Helper helper = helperService.createHelper(new HelperDTO("Speedy Helper", "9988776654", "Escort", HelperAvailability.AVAILABLE));

        AssistanceRequestDTO dto = new AssistanceRequestDTO(
                AssistanceType.ESCORT, "Library", LocalDate.now().plusDays(1), LocalTime.of(14, 0), "Escort needed");
        AssistanceRequest req = requestService.createRequest(dto, user);

        // Assign and complete
        requestService.assignHelper(req.getId(), helper.getId());
        requestService.completeRequest(req.getId());

        AssistanceRequest completedReq = requestService.getRequestById(req.getId());
        assertEquals(RequestStatus.COMPLETED, completedReq.getStatus());

        // Attempt to cancel should fail
        BusinessRuleException cancelEx = assertThrows(BusinessRuleException.class, () -> {
            requestService.cancelRequest(completedReq.getId(), user);
        });
        assertTrue(cancelEx.getMessage().contains("Completed requests cannot be cancelled"));

        // Attempt to reassign should fail
        BusinessRuleException reassignEx = assertThrows(BusinessRuleException.class, () -> {
            requestService.assignHelper(completedReq.getId(), helper.getId());
        });
        assertTrue(reassignEx.getMessage().contains("Completed requests cannot be reassigned"));
    }

    @Test
    @DisplayName("RULE 5: Only available helpers can be assigned")
    void testOnlyAvailableHelpersCanBeAssigned() {
        User user = userService.findByEmail("madhan@example.com");
        Helper busyHelper = helperService.createHelper(new HelperDTO("Busy Helper", "9988776653", "Escort", HelperAvailability.BUSY));

        AssistanceRequestDTO dto = new AssistanceRequestDTO(
                AssistanceType.SHUTTLE_ASSISTANCE, "Admin Block", LocalDate.now().plusDays(1), LocalTime.of(11, 0), "Shuttle help");
        AssistanceRequest req = requestService.createRequest(dto, user);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            requestService.assignHelper(req.getId(), busyHelper.getId());
        });

        assertTrue(ex.getMessage().contains("Helper is not available for assignment"));
    }
}
