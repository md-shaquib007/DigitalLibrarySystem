package com.college.library.controller;

import com.college.library.entity.Book;
import com.college.library.service.BookService;
import com.college.library.util.FileUploadUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet(name = "DownloadServlet", urlPatterns = {"/download/*"})
public class DownloadServlet extends BaseServlet {

    private final BookService bookService = new BookService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Long bookId = Long.parseLong(pathInfo.substring(1));
        Book book = bookService.getBookById(bookId).orElse(null);

        if (book == null || !book.isDigitalAvailable() || book.getFilePath() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "PDF not available for this book");
            return;
        }

        Path filePath = FileUploadUtil.resolveFilePath(book.getFilePath(), getServletContext().getRealPath("/"));
        if (!Files.exists(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found on server");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + book.getFileName() + "\"");
        response.setContentLengthLong(Files.size(filePath));

        try (OutputStream out = response.getOutputStream()) {
            Files.copy(filePath, out);
        }
    }
}
