package com.madhan.TransitAssist.dto;

import com.madhan.TransitAssist.model.HelperAvailability;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class HelperDTO {

    @NotBlank(message = "Helper name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    private String phone;

    private String specialization;

    private HelperAvailability availability = HelperAvailability.AVAILABLE;

    public HelperDTO() {
    }

    public HelperDTO(String name, String phone, String specialization, HelperAvailability availability) {
        this.name = name;
        this.phone = phone;
        this.specialization = specialization;
        this.availability = availability != null ? availability : HelperAvailability.AVAILABLE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
}
