package com.madhan.TransitAssist.model;

public enum AssistanceType {
    WHEELCHAIR("Wheelchair Assistance"),
    ESCORT("Escort Assistance"),
    SHUTTLE_ASSISTANCE("Shuttle Transit Assistance"),
    BOARDING_ASSISTANCE("Boarding Assistance"),
    OTHER("Other Assistance");

    private final String displayName;

    AssistanceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
