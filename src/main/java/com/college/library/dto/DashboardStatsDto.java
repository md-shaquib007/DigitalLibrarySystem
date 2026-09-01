package com.college.library.dto;

public class DashboardStatsDto {

    private long totalBooks;
    private long totalUsers;
    private long activeBorrows;
    private long overdueBorrows;
    private long pendingReservations;
    private long totalCategories;

    public long getTotalBooks() { return totalBooks; }
    public void setTotalBooks(long totalBooks) { this.totalBooks = totalBooks; }
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getActiveBorrows() { return activeBorrows; }
    public void setActiveBorrows(long activeBorrows) { this.activeBorrows = activeBorrows; }
    public long getOverdueBorrows() { return overdueBorrows; }
    public void setOverdueBorrows(long overdueBorrows) { this.overdueBorrows = overdueBorrows; }
    public long getPendingReservations() { return pendingReservations; }
    public void setPendingReservations(long pendingReservations) { this.pendingReservations = pendingReservations; }
    public long getTotalCategories() { return totalCategories; }
    public void setTotalCategories(long totalCategories) { this.totalCategories = totalCategories; }
}
