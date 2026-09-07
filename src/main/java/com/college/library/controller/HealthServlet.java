package com.college.library.controller;

import com.college.library.config.HibernateUtil;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "HealthServlet", urlPatterns = {"/api/health"})
public class HealthServlet extends HttpServlet {

    private static final long START_TIME = System.currentTimeMillis();
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Digital Library System");
        health.put("timestamp", Instant.now().toString());
        health.put("uptimeMs", System.currentTimeMillis() - START_TIME);

        Map<String, Object> details = new HashMap<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Object result = session.createNativeQuery("SELECT 1", Object.class).getSingleResult();
            if (result != null) {
                details.put("database", Map.of("status", "UP", "orm", "Hibernate 6"));
            }
        } catch (Exception e) {
            health.put("status", "DOWN");
            details.put("database", Map.of("status", "DOWN", "error", e.getMessage()));
        }

        Runtime runtime = Runtime.getRuntime();
        details.put("memory", Map.of(
                "totalMB", runtime.totalMemory() / (1024 * 1024),
                "freeMB", runtime.freeMemory() / (1024 * 1024),
                "usedMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                "maxMB", runtime.maxMemory() / (1024 * 1024)
        ));

        health.put("components", details);

        response.setContentType("application/json");
        response.setStatus("UP".equals(health.get("status")) ? 200 : 503);
        response.getWriter().write(gson.toJson(health));
    }
}
