package com.madhan.TransitAssist.service;

import com.madhan.TransitAssist.dto.DashboardStatsDTO;
import com.madhan.TransitAssist.dto.WorkloadSummaryDTO;
import com.madhan.TransitAssist.model.AssistanceRequest;
import com.madhan.TransitAssist.model.Helper;
import com.madhan.TransitAssist.model.HelperAvailability;
import com.madhan.TransitAssist.model.RequestStatus;
import com.madhan.TransitAssist.model.Role;
import com.madhan.TransitAssist.repository.AssistanceRequestRepository;
import com.madhan.TransitAssist.repository.HelperRepository;
import com.madhan.TransitAssist.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StaffService {

    private final AssistanceRequestRepository requestRepository;
    private final HelperRepository helperRepository;
    private final UserRepository userRepository;

    public StaffService(AssistanceRequestRepository requestRepository, 
                        HelperRepository helperRepository, 
                        UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.helperRepository = helperRepository;
        this.userRepository = userRepository;
    }

    public DashboardStatsDTO getStaffDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalRequests(requestRepository.count());
        stats.setRequestedCount(requestRepository.countByStatus(RequestStatus.REQUESTED));
        stats.setAssignedCount(requestRepository.countByStatus(RequestStatus.ASSIGNED));
        stats.setCompletedCount(requestRepository.countByStatus(RequestStatus.COMPLETED));
        stats.setCancelledCount(requestRepository.countByStatus(RequestStatus.CANCELLED));

        stats.setAvailableHelpers(helperRepository.countByAvailability(HelperAvailability.AVAILABLE));
        stats.setBusyHelpers(helperRepository.countByAvailability(HelperAvailability.BUSY));
        stats.setOfflineHelpers(helperRepository.countByAvailability(HelperAvailability.OFFLINE));
        stats.setTotalHelpers(helperRepository.count());

        stats.setTotalUsers(userRepository.countByRole(Role.USER));
        stats.setTotalStaff(userRepository.countByRole(Role.STAFF));

        return stats;
    }

    public List<WorkloadSummaryDTO> getHelperWorkload(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        List<Helper> allHelpers = helperRepository.findAll();
        List<WorkloadSummaryDTO> workloadList = new ArrayList<>();

        for (Helper helper : allHelpers) {
            List<AssistanceRequest> helperRequests = requestRepository.findByHelperAndTravelDate(helper, date);
            
            // Helper status dynamically reflects today's workload if viewing today
            HelperAvailability currentStatus = helper.getAvailability();
            if (date.equals(LocalDate.now())) {
                boolean hasActive = helperRequests.stream()
                        .anyMatch(r -> r.getStatus() == RequestStatus.ASSIGNED);
                if (hasActive) {
                    currentStatus = HelperAvailability.BUSY;
                }
            }

            WorkloadSummaryDTO summary = new WorkloadSummaryDTO(
                    helper.getId(),
                    helper.getName(),
                    helper.getPhone(),
                    helper.getSpecialization(),
                    currentStatus,
                    helperRequests.size(),
                    helperRequests
            );
            workloadList.add(summary);
        }

        return workloadList;
    }
}
