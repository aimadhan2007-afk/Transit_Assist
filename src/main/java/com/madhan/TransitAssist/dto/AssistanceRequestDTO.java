package com.madhan.TransitAssist.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.madhan.TransitAssist.model.AssistanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class AssistanceRequestDTO {

    @NotNull(message = "Assistance type is required")
    private AssistanceType assistanceType;

    @NotBlank(message = "Pickup point is required")
    private String pickupPoint;

    @NotNull(message = "Travel date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate travelDate;

    @NotNull(message = "Travel time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime travelTime;

    private String description;

    public AssistanceRequestDTO() {
    }

    public AssistanceRequestDTO(AssistanceType assistanceType, String pickupPoint, 
                                LocalDate travelDate, LocalTime travelTime, String description) {
        this.assistanceType = assistanceType;
        this.pickupPoint = pickupPoint;
        this.travelDate = travelDate;
        this.travelTime = travelTime;
        this.description = description;
    }

    public AssistanceType getAssistanceType() {
        return assistanceType;
    }

    public void setAssistanceType(AssistanceType assistanceType) {
        this.assistanceType = assistanceType;
    }

    public String getPickupPoint() {
        return pickupPoint;
    }

    public void setPickupPoint(String pickupPoint) {
        this.pickupPoint = pickupPoint;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public LocalTime getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(LocalTime travelTime) {
        this.travelTime = travelTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
