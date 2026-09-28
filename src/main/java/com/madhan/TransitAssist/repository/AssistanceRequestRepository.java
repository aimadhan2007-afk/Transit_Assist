package com.madhan.TransitAssist.repository;

import com.madhan.TransitAssist.model.AssistanceRequest;
import com.madhan.TransitAssist.model.Helper;
import com.madhan.TransitAssist.model.RequestStatus;
import com.madhan.TransitAssist.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssistanceRequestRepository extends JpaRepository<AssistanceRequest, Long> {
    List<AssistanceRequest> findByUserOrderByCreatedAtDesc(User user);
    List<AssistanceRequest> findByUserAndStatusOrderByCreatedAtDesc(User user, RequestStatus status);
    List<AssistanceRequest> findAllByOrderByCreatedAtDesc();
    List<AssistanceRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
    List<AssistanceRequest> findByTravelDateOrderByTravelTimeAsc(LocalDate travelDate);
    List<AssistanceRequest> findByHelperAndTravelDate(Helper helper, LocalDate travelDate);
    List<AssistanceRequest> findByHelperAndTravelDateAndStatus(Helper helper, LocalDate travelDate, RequestStatus status);
    List<AssistanceRequest> findByHelperOrderByTravelDateDescTravelTimeDesc(Helper helper);
    long countByStatus(RequestStatus status);
    long countByUserAndStatus(User user, RequestStatus status);
    long countByUser(User user);
}
