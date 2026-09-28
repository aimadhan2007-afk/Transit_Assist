package com.madhan.TransitAssist.dto;

public class DashboardStatsDTO {
    private long totalRequests;
    private long requestedCount;
    private long assignedCount;
    private long completedCount;
    private long cancelledCount;
    private long availableHelpers;
    private long busyHelpers;
    private long offlineHelpers;
    private long totalUsers;
    private long totalStaff;
    private long totalHelpers;

    public DashboardStatsDTO() {
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getRequestedCount() {
        return requestedCount;
    }

    public void setRequestedCount(long requestedCount) {
        this.requestedCount = requestedCount;
    }

    public long getAssignedCount() {
        return assignedCount;
    }

    public void setAssignedCount(long assignedCount) {
        this.assignedCount = assignedCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(long cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public long getAvailableHelpers() {
        return availableHelpers;
    }

    public void setAvailableHelpers(long availableHelpers) {
        this.availableHelpers = availableHelpers;
    }

    public long getBusyHelpers() {
        return busyHelpers;
    }

    public void setBusyHelpers(long busyHelpers) {
        this.busyHelpers = busyHelpers;
    }

    public long getOfflineHelpers() {
        return offlineHelpers;
    }

    public void setOfflineHelpers(long offlineHelpers) {
        this.offlineHelpers = offlineHelpers;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalStaff() {
        return totalStaff;
    }

    public void setTotalStaff(long totalStaff) {
        this.totalStaff = totalStaff;
    }

    public long getTotalHelpers() {
        return totalHelpers;
    }

    public void setTotalHelpers(long totalHelpers) {
        this.totalHelpers = totalHelpers;
    }
}
