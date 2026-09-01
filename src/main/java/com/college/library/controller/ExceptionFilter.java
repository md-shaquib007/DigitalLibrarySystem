package com.college.library.controller;

import com.college.library.exception.LibraryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class ExceptionFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ExceptionFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (LibraryException e) {
            log.warn("Application error: {}", e.getMessage());
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpRequest.getSession().setAttribute("flashType", "danger");
            httpRequest.getSession().setAttribute("flashMessage", e.getMessage());
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/dashboard");
        } catch (Exception e) {
            log.error("Unexpected error", e);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("""
                <!DOCTYPE html>
                <html><head><title>Error</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
                </head><body class="bg-light">
                <div class="container mt-5">
                <div class="alert alert-danger">
                <h4>Something went wrong</h4>
                <p>An unexpected error occurred. Please try again later.</p>
                <a href="%s/dashboard" class="btn btn-primary">Go to Dashboard</a>
                </div></div></body></html>
                """.formatted(((HttpServletRequest) request).getContextPath()));
        }
    }
}
