package com.college.library.controller;

import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.util.SessionUtil;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String servletPath = httpRequest.getServletPath();
        String pathInfo = httpRequest.getPathInfo() != null ? httpRequest.getPathInfo() : "";
        String fullPath = servletPath + pathInfo;
        String contextPath = httpRequest.getContextPath();

        if (isStaticOrLogin(servletPath)) {
            chain.doFilter(request, response);
            return;
        }

        if (isAdminPath(fullPath, httpRequest.getMethod())) {
            User user = SessionUtil.getLoggedInUser(httpRequest);
            if (user == null) {
                httpResponse.sendRedirect(contextPath + "/login");
                return;
            }
            if (user.getRole() != Role.ADMIN && user.getRole() != Role.LIBRARIAN) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Staff access required");
                return;
            }
            if (isAdminOnlyPath(fullPath) && user.getRole() != Role.ADMIN) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isStaticOrLogin(String path) {
        return path.startsWith("/css/") || path.startsWith("/js/") || "/login".equals(path);
    }

    private boolean isAdminPath(String fullPath, String method) {
        if (fullPath.startsWith("/catalog") || fullPath.startsWith("/users")) {
            return true;
        }
        if (fullPath.startsWith("/books")) {
            if (fullPath.endsWith("/new") || fullPath.contains("/edit") || fullPath.contains("/delete")) {
                return true;
            }
            if ("POST".equalsIgnoreCase(method) &&
                    (fullPath.equals("/books") || fullPath.endsWith("/edit") || fullPath.endsWith("/delete"))) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdminOnlyPath(String fullPath) {
        return fullPath.startsWith("/users");
    }
}
