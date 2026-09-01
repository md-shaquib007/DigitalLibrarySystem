package com.college.library.controller;

import com.college.library.config.ThymeleafConfig;
import com.college.library.entity.User;
import com.college.library.util.SessionUtil;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {

    protected TemplateEngine getTemplateEngine() {
        ServletContext context = getServletContext();
        return ThymeleafConfig.getTemplateEngine(context);
    }

    protected void render(HttpServletRequest request, HttpServletResponse response,
                          String template, Map<String, Object> variables) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
        IWebExchange webExchange = application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);

        User user = SessionUtil.getLoggedInUser(request);
        context.setVariable("currentUser", user);
        context.setVariable("isAdmin", user != null && user.getRole().name().equals("ADMIN"));
        context.setVariable("isLibrarian", user != null &&
                (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("LIBRARIAN")));

        if (variables != null) {
            variables.forEach(context::setVariable);
        }

        getTemplateEngine().process(template, context, response.getWriter());
    }

    protected void redirect(HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(requestContextPath() + path);
    }

    protected String requestContextPath() {
        return getServletContext().getContextPath();
    }

    protected Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    protected void setFlashMessage(HttpServletRequest request, String type, String message) {
        request.getSession().setAttribute("flashType", type);
        request.getSession().setAttribute("flashMessage", message);
    }

    protected void addFlashToModel(HttpServletRequest request, Map<String, Object> model) {
        Object flashType = request.getSession().getAttribute("flashType");
        Object flashMessage = request.getSession().getAttribute("flashMessage");
        if (flashMessage != null) {
            model.put("flashType", flashType);
            model.put("flashMessage", flashMessage);
            request.getSession().removeAttribute("flashType");
            request.getSession().removeAttribute("flashMessage");
        }
    }
}
