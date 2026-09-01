package com.college.library.dto;

public class TopBookDto {

    private Long bookId;
    private String title;
    private String categoryName;
    private long borrowCount;

    public TopBookDto() {}

    public TopBookDto(Long bookId, String title, String categoryName, long borrowCount) {
        this.bookId = bookId;
        this.title = title;
        this.categoryName = categoryName;
        this.borrowCount = borrowCount;
    }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public long getBorrowCount() { return borrowCount; }
    public void setBorrowCount(long borrowCount) { this.borrowCount = borrowCount; }
}
