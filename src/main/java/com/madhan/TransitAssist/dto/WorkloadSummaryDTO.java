package com.madhan.TransitAssist.dto;

import com.madhan.TransitAssist.model.AssistanceRequest;
import com.madhan.TransitAssist.model.HelperAvailability;
import java.util.ArrayList;
import java.util.List;

public class WorkloadSummaryDTO {
    private Long helperId;
    private String helperName;
    private String helperPhone;
    private String specialization;
    private HelperAvailability availability;
    private int assignedRequestsCount;
    private List<AssistanceRequest> requests = new ArrayList<>();

    public WorkloadSummaryDTO() {
    }

    public WorkloadSummaryDTO(Long helperId, String helperName, String helperPhone, 
                              String specialization, HelperAvailability availability, 
                              int assignedRequestsCount, List<AssistanceRequest> requests) {
        this.helperId = helperId;
        this.helperName = helperName;
        this.helperPhone = helperPhone;
        this.specialization = specialization;
        this.availability = availability;
        this.assignedRequestsCount = assignedRequestsCount;
        this.requests = requests != null ? requests : new ArrayList<>();
    }

    public Long getHelperId() {
        return helperId;
    }

    public void setHelperId(Long helperId) {
        this.helperId = helperId;
    }

    public String getHelperName() {
        return helperName;
    }

    public void setHelperName(String helperName) {
        this.helperName = helperName;
    }

    public String getHelperPhone() {
        return helperPhone;
    }

    public void setHelperPhone(String helperPhone) {
        this.helperPhone = helperPhone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public HelperAvailability getAvailability() {
        return availability;
    }

    public void setAvailability(HelperAvailability availability) {
        this.availability = availability;
    }

    public int getAssignedRequestsCount() {
        return assignedRequestsCount;
    }

    public void setAssignedRequestsCount(int assignedRequestsCount) {
        this.assignedRequestsCount = assignedRequestsCount;
    }

    public List<AssistanceRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<AssistanceRequest> requests) {
        this.requests = requests;
    }
}
