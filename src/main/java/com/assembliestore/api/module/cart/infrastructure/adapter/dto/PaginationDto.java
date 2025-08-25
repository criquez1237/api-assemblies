package com.assembliestore.api.module.cart.infrastructure.adapter.dto;

public class PaginationDto {
    private int totalItems;
    private int totalPages;
    private int currentPage;
    private boolean hasNextPage;
    private boolean hasPrevPage;
    private int limitPerPage;

    public PaginationDto() {}

    public PaginationDto(int totalItems, int totalPages, int currentPage, boolean hasNextPage, boolean hasPrevPage, int limitPerPage) {
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.hasNextPage = hasNextPage;
        this.hasPrevPage = hasPrevPage;
        this.limitPerPage = limitPerPage;
    }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public boolean isHasNextPage() { return hasNextPage; }
    public void setHasNextPage(boolean hasNextPage) { this.hasNextPage = hasNextPage; }
    public boolean isHasPrevPage() { return hasPrevPage; }
    public void setHasPrevPage(boolean hasPrevPage) { this.hasPrevPage = hasPrevPage; }
    public int getLimitPerPage() { return limitPerPage; }
    public void setLimitPerPage(int limitPerPage) { this.limitPerPage = limitPerPage; }
}
