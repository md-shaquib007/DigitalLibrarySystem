package com.college.library.controller;

import com.college.library.dto.LoginDto;
import com.college.library.entity.User;
import com.college.library.exception.LibraryException;
import com.college.library.service.AuthService;
import com.college.library.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/logout"})
public class AuthServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/logout".equals(request.getServletPath())) {
            SessionUtil.logout(request);
            redirect(response, "/dashboard");
            return;
        }
        showLogin(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LoginDto dto = new LoginDto();
        dto.setUsername(request.getParameter("username"));
        dto.setPassword(request.getParameter("password"));

        try {
            User user = authService.loginStaff(dto);
            SessionUtil.setLoggedInUser(request, user);
            redirect(response, "/dashboard");
        } catch (LibraryException e) {
            Map<String, Object> model = new HashMap<>();
            model.put("error", e.getMessage());
            model.put("username", dto.getUsername());
            render(request, response, "auth/login", model);
        }
    }

    private void showLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> model = new HashMap<>();
        addFlashToModel(request, model);
        render(request, response, "auth/login", model);
    }
}
