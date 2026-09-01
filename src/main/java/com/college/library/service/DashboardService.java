package com.college.library.service;

import com.college.library.dto.DashboardStatsDto;
import com.college.library.entity.Book;
import com.college.library.entity.Category;
import com.college.library.repository.BookRepository;
import com.college.library.repository.CategoryRepository;

import java.util.List;

public class DashboardService {

    private final BookRepository bookRepository = new BookRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();

    public DashboardStatsDto getStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalBooks(bookRepository.count());
        stats.setTotalCategories(categoryRepository.count());
        return stats;
    }

    public List<Book> getRecentBooks(int limit) {
        return bookRepository.findRecent(limit);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
