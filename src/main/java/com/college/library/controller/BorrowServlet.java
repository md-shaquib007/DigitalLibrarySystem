package com.college.library.controller;

import com.college.library.entity.BorrowRecord;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.service.BorrowService;
import com.college.library.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "BorrowServlet", urlPatterns = {"/borrows", "/borrows/*"})
public class BorrowServlet extends BaseServlet {

    private final BorrowService borrowService = new BorrowService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = SessionUtil.getLoggedInUser(request);
        if (currentUser == null) {
            redirect(response, "/login");
            return;
        }

        boolean isLibrarian = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.LIBRARIAN;
        boolean staffView = isLibrarian && "all".equalsIgnoreCase(request.getParameter("view"));

        List<BorrowRecord> borrows = staffView
                ? borrowService.getActiveBorrows()
                : borrowService.getUserBorrowHistory(currentUser.getId());

        Map<String, Object> model = new HashMap<>();
        model.put("borrows", borrows);
        model.put("staffView", staffView);
        addFlashToModel(request, model);

        render(request, response, "borrows/list", model);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = SessionUtil.getLoggedInUser(request);
        if (currentUser == null) {
            redirect(response, "/login");
            return;
        }

        String pathInfo = request.getPathInfo() != null ? request.getPathInfo() : "";
        try {
            if (pathInfo.endsWith("/return")) {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 2) {
                    Long borrowId = parseLong(parts[1]);
                    borrowService.returnBook(borrowId);
                    setFlashMessage(request, "success", "Book returned successfully!");
                }
            } else {
                Long bookId = parseLong(request.getParameter("bookId"));
                if (bookId != null) {
                    borrowService.borrowBook(currentUser.getId(), bookId);
                    setFlashMessage(request, "success", "Book borrowed successfully!");
                } else {
                    setFlashMessage(request, "danger", "Book ID is required.");
                }
            }
        } catch (Exception e) {
            setFlashMessage(request, "danger", e.getMessage());
        }

        redirect(response, "/borrows");
    }
}
