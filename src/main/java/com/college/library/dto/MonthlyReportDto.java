package com.college.library.dto;

public class MonthlyReportDto {

    private int year;
    private int month;
    private String monthName;
    private long totalBorrows;
    private long totalReturns;
    private double totalFines;

    public MonthlyReportDto() {}

    public MonthlyReportDto(int year, int month, String monthName, long totalBorrows, long totalReturns, double totalFines) {
        this.year = year;
        this.month = month;
        this.monthName = monthName;
        this.totalBorrows = totalBorrows;
        this.totalReturns = totalReturns;
        this.totalFines = totalFines;
    }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }
    public long getTotalBorrows() { return totalBorrows; }
    public void setTotalBorrows(long totalBorrows) { this.totalBorrows = totalBorrows; }
    public long getTotalReturns() { return totalReturns; }
    public void setTotalReturns(long totalReturns) { this.totalReturns = totalReturns; }
    public double getTotalFines() { return totalFines; }
    public void setTotalFines(double totalFines) { this.totalFines = totalFines; }
}
