package com.madhan.TransitAssist.service;

import com.madhan.TransitAssist.dto.AssistanceRequestDTO;
import com.madhan.TransitAssist.exception.BusinessRuleException;
import com.madhan.TransitAssist.exception.ResourceNotFoundException;
import com.madhan.TransitAssist.exception.UnauthorizedException;
import com.madhan.TransitAssist.model.*;
import com.madhan.TransitAssist.repository.AssistanceRequestRepository;
import com.madhan.TransitAssist.repository.HelperRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AssistanceRequestService {

    private final AssistanceRequestRepository requestRepository;
    private final HelperRepository helperRepository;

    public AssistanceRequestService(AssistanceRequestRepository requestRepository, HelperRepository helperRepository) {
        this.requestRepository = requestRepository;
        this.helperRepository = helperRepository;
    }

    public AssistanceRequest createRequest(AssistanceRequestDTO dto, User user) {
        if (dto.getTravelDate() == null) {
            throw new BusinessRuleException("Travel date is required");
        }
        if (dto.getTravelTime() == null) {
            throw new BusinessRuleException("Travel time is required");
        }
        if (dto.getPickupPoint() == null || dto.getPickupPoint().trim().isEmpty()) {
            throw new BusinessRuleException("Pickup point is required");
        }
        if (dto.getAssistanceType() == null) {
            throw new BusinessRuleException("Assistance type is required");
        }
        if (dto.getTravelDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Travel date cannot be in the past");
        }

        AssistanceRequest request = new AssistanceRequest();
        request.setUser(user);
        request.setAssistanceType(dto.getAssistanceType());
        request.setPickupPoint(dto.getPickupPoint().trim());
        request.setTravelDate(dto.getTravelDate());
        request.setTravelTime(dto.getTravelTime());
        request.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        request.setStatus(RequestStatus.REQUESTED);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        return requestRepository.save(request);
    }

    public AssistanceRequest assignHelper(Long requestId, Long helperId) {
        AssistanceRequest request = getRequestById(requestId);

        // RULE 2: A COMPLETED request cannot be reassigned
        if (request.getStatus() == RequestStatus.COMPLETED) {
            throw new BusinessRuleException("Completed requests cannot be reassigned.");
        }
        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new BusinessRuleException("Cancelled requests cannot be assigned.");
        }

        Helper helper = helperRepository.findById(helperId)
                .orElseThrow(() -> new ResourceNotFoundException("Helper not found with ID: " + helperId));

        // RULE 5: Only available helpers can be assigned
        if (helper.getAvailability() != HelperAvailability.AVAILABLE) {
            throw new BusinessRuleException("Helper is not available for assignment. Current status: " + helper.getAvailability());
        }

        // RULE 1: Helper cannot be assigned to two overlapping-time requests
        List<AssistanceRequest> assignedRequests = requestRepository.findByHelperAndTravelDate(helper, request.getTravelDate());
        for (AssistanceRequest existing : assignedRequests) {
            if (existing.getStatus() == RequestStatus.ASSIGNED && !existing.getId().equals(request.getId())) {
                long minutesDiff = Math.abs(Duration.between(existing.getTravelTime(), request.getTravelTime()).toMinutes());
                // Consider overlapping if within 60 minutes
                if (minutesDiff < 60) {
                    throw new BusinessRuleException("Helper cannot be assigned because they already have another request during this time.");
                }
            }
        }

        request.setHelper(helper);
        request.setStatus(RequestStatus.ASSIGNED);
        request.setUpdatedAt(LocalDateTime.now());

        // Update helper availability to BUSY if they now have active tasks
        helper.setAvailability(HelperAvailability.BUSY);
        helperRepository.save(helper);

        return requestRepository.save(request);
    }

    public AssistanceRequest completeRequest(Long requestId) {
        AssistanceRequest request = getRequestById(requestId);

        if (request.getStatus() == RequestStatus.COMPLETED) {
            throw new BusinessRuleException("Request is already completed.");
        }
        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new BusinessRuleException("Cancelled requests cannot be completed.");
        }
        if (request.getStatus() == RequestStatus.REQUESTED) {
            throw new BusinessRuleException("Cannot complete a request that has not been assigned to a helper.");
        }

        request.setStatus(RequestStatus.COMPLETED);
        request.setUpdatedAt(LocalDateTime.now());

        // Check if helper has other active assignments today; if not, free them back to AVAILABLE
        Helper helper = request.getHelper();
        if (helper != null) {
            List<AssistanceRequest> todayActive = requestRepository.findByHelperAndTravelDateAndStatus(
                    helper, LocalDate.now(), RequestStatus.ASSIGNED);
            boolean hasOtherActive = todayActive.stream().anyMatch(r -> !r.getId().equals(request.getId()));
            if (!hasOtherActive && helper.getAvailability() == HelperAvailability.BUSY) {
                helper.setAvailability(HelperAvailability.AVAILABLE);
                helperRepository.save(helper);
            }
        }

        return requestRepository.save(request);
    }

    public AssistanceRequest cancelRequest(Long requestId, User currentUser) {
        AssistanceRequest request = getRequestById(requestId);

        // RULE 3: A user can cancel only their own request (Staff/Admin can also manage)
        boolean isOwner = request.getUser().getId().equals(currentUser.getId());
        boolean isStaffOrAdmin = currentUser.getRole() == Role.STAFF || currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isStaffOrAdmin) {
            throw new UnauthorizedException("You are not authorized to cancel this request.");
        }

        // RULE 2 & RULE 4: A COMPLETED request cannot be cancelled
        if (request.getStatus() == RequestStatus.COMPLETED) {
            throw new BusinessRuleException("Completed requests cannot be cancelled.");
        }

        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new BusinessRuleException("This request has already been cancelled.");
        }

        request.setStatus(RequestStatus.CANCELLED);
        request.setUpdatedAt(LocalDateTime.now());

        // If a helper was assigned, check if their status should be restored
        Helper helper = request.getHelper();
        if (helper != null) {
            List<AssistanceRequest> todayActive = requestRepository.findByHelperAndTravelDateAndStatus(
                    helper, LocalDate.now(), RequestStatus.ASSIGNED);
            boolean hasOtherActive = todayActive.stream().anyMatch(r -> !r.getId().equals(request.getId()));
            if (!hasOtherActive && helper.getAvailability() == HelperAvailability.BUSY) {
                helper.setAvailability(HelperAvailability.AVAILABLE);
                helperRepository.save(helper);
            }
        }

        return requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public AssistanceRequest getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assistance request not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<AssistanceRequest> getAllRequests() {
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<AssistanceRequest> getRequestsByUser(User user) {
        return requestRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<AssistanceRequest> getRequestsByStatus(RequestStatus status) {
        return requestRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public List<AssistanceRequest> getRequestsByDate(LocalDate date) {
        return requestRepository.findByTravelDateOrderByTravelTimeAsc(date);
    }

    @Transactional(readOnly = true)
    public long countByStatus(RequestStatus status) {
        return requestRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countByUserAndStatus(User user, RequestStatus status) {
        return requestRepository.countByUserAndStatus(user, status);
    }

    @Transactional(readOnly = true)
    public long countTotalByUser(User user) {
        return requestRepository.countByUser(user);
    }

    @Transactional(readOnly = true)
    public long countTotalRequests() {
        return requestRepository.count();
    }
}
