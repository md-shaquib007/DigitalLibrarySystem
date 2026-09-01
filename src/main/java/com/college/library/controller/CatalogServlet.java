package com.college.library.controller;

import com.college.library.entity.Author;
import com.college.library.entity.Category;
import com.college.library.entity.Publisher;
import com.college.library.exception.LibraryException;
import com.college.library.service.CatalogService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "CatalogServlet", urlPatterns = {"/catalog", "/catalog/*"})
public class CatalogServlet extends BaseServlet {

    private final CatalogService catalogService = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String type = request.getParameter("type");
        if (type == null) type = "categories";

        Map<String, Object> model = new HashMap<>();
        model.put("type", type);

        switch (type) {
            case "authors" -> model.put("items", catalogService.getAllAuthors());
            case "publishers" -> model.put("items", catalogService.getAllPublishers());
            default -> model.put("items", catalogService.getAllCategories());
        }

        addFlashToModel(request, model);
        render(request, response, "catalog/list", model);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String type = request.getParameter("type");

        try {
            switch (action) {
                case "save" -> saveItem(request, type);
                case "delete" -> deleteItem(request, type);
            }
            setFlashMessage(request, "success", "Operation completed successfully!");
        } catch (LibraryException e) {
            setFlashMessage(request, "danger", e.getMessage());
        }

        redirect(response, "/catalog?type=" + (type != null ? type : "categories"));
    }

    private void saveItem(HttpServletRequest request, String type) {
        switch (type) {
            case "categories" -> {
                Category category = new Category();
                Long id = parseLong(request.getParameter("id"));
                if (id != null) category.setId(id);
                category.setName(request.getParameter("name"));
                category.setDescription(request.getParameter("description"));
                category.setIcon(request.getParameter("icon"));
                catalogService.saveCategory(category);
            }
            case "authors" -> {
                Author author = new Author();
                Long id = parseLong(request.getParameter("id"));
                if (id != null) author.setId(id);
                author.setName(request.getParameter("name"));
                author.setBio(request.getParameter("bio"));
                catalogService.saveAuthor(author);
            }
            case "publishers" -> {
                Publisher publisher = new Publisher();
                Long id = parseLong(request.getParameter("id"));
                if (id != null) publisher.setId(id);
                publisher.setName(request.getParameter("name"));
                publisher.setAddress(request.getParameter("address"));
                publisher.setWebsite(request.getParameter("website"));
                catalogService.savePublisher(publisher);
            }
        }
    }

    private void deleteItem(HttpServletRequest request, String type) {
        Long id = parseLong(request.getParameter("id"));
        if (id == null) return;

        switch (type) {
            case "categories" -> catalogService.deleteCategory(id);
            case "authors" -> catalogService.deleteAuthor(id);
            case "publishers" -> catalogService.deletePublisher(id);
        }
    }
}
