package com.college.library.dto;

public class BookSearchDto {

    private String keyword;
    private Long categoryId;
    private Long authorId;
    private Long publisherId;
    private Boolean availableOnly;
    private Boolean digitalOnly;
    private String sortBy = "title";
    private String sortDir = "asc";
    private int page = 0;
    private int size = 10;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public Long getPublisherId() { return publisherId; }
    public void setPublisherId(Long publisherId) { this.publisherId = publisherId; }
    public Boolean getAvailableOnly() { return availableOnly; }
    public void setAvailableOnly(Boolean availableOnly) { this.availableOnly = availableOnly; }
    public Boolean getDigitalOnly() { return digitalOnly; }
    public void setDigitalOnly(Boolean digitalOnly) { this.digitalOnly = digitalOnly; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
