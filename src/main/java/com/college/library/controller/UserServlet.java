package com.college.library.controller;

import com.college.library.dto.UserRegistrationDto;
import com.college.library.entity.Role;
import com.college.library.exception.LibraryException;
import com.college.library.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "UserServlet", urlPatterns = {"/users", "/users/*"})
public class UserServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("users", authService.getAllUsers());
        model.put("roles", Role.values());
        addFlashToModel(request, model);
        render(request, response, "users/list", model);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        try {
            switch (action) {
                case "create" -> {
                    UserRegistrationDto dto = new UserRegistrationDto();
                    dto.setUsername(request.getParameter("username"));
                    dto.setPassword(request.getParameter("password"));
                    dto.setFullName(request.getParameter("fullName"));
                    dto.setEmail(request.getParameter("email"));
                    dto.setPhone(request.getParameter("phone"));
                    dto.setRole(Role.valueOf(request.getParameter("role")));
                    authService.createUser(dto);
                    setFlashMessage(request, "success", "User created successfully!");
                }
                case "toggle" -> {
                    Long id = parseLong(request.getParameter("id"));
                    authService.toggleUserStatus(id);
                    setFlashMessage(request, "success", "User status updated!");
                }
                case "delete" -> {
                    Long id = parseLong(request.getParameter("id"));
                    authService.deleteUser(id);
                    setFlashMessage(request, "success", "User deleted!");
                }
            }
        } catch (LibraryException e) {
            setFlashMessage(request, "danger", e.getMessage());
        }

        redirect(response, "/users");
    }
}
