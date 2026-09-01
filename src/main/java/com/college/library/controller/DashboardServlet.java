package com.college.library.controller;

import com.college.library.dto.DashboardStatsDto;
import com.college.library.entity.Book;
import com.college.library.entity.Category;
import com.college.library.service.DashboardService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard", "/"})
public class DashboardServlet extends BaseServlet {

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DashboardStatsDto stats = dashboardService.getStats();
        List<Book> recentBooks = dashboardService.getRecentBooks(6);
        List<Category> categories = dashboardService.getAllCategories();

        Map<String, Object> model = new HashMap<>();
        model.put("stats", stats);
        model.put("recentBooks", recentBooks);
        model.put("categories", categories);
        addFlashToModel(request, model);

        render(request, response, "dashboard/index", model);
    }
}
