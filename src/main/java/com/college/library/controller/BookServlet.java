package com.college.library.controller;

import com.college.library.dto.BookDto;
import com.college.library.dto.BookSearchDto;
import com.college.library.dto.PageResult;
import com.college.library.entity.Book;
import com.college.library.exception.LibraryException;
import com.college.library.service.BookService;
import com.college.library.service.CatalogService;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "BookServlet", urlPatterns = {"/books", "/books/*"})
public class BookServlet extends BaseServlet {

    private final BookService bookService = new BookService();
    private final CatalogService catalogService = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            listBooks(request, response);
        } else if (pathInfo.matches("/\\d+")) {
            showBook(request, response, Long.parseLong(pathInfo.substring(1)));
        } else if (pathInfo.equals("/new")) {
            showBookForm(request, response, null);
        } else if (pathInfo.matches("/\\d+/edit")) {
            showBookForm(request, response, Long.parseLong(pathInfo.split("/")[1]));
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            createBook(request, response);
        } else if (pathInfo.matches("/\\d+/edit")) {
            updateBook(request, response, Long.parseLong(pathInfo.split("/")[1]));
        } else if (pathInfo.matches("/\\d+/delete")) {
            deleteBook(request, response, Long.parseLong(pathInfo.split("/")[1]));
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void listBooks(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BookSearchDto search = new BookSearchDto();
        search.setKeyword(request.getParameter("keyword"));
        search.setCategoryId(parseLong(request.getParameter("categoryId")));
        search.setAuthorId(parseLong(request.getParameter("authorId")));
        search.setPublisherId(parseLong(request.getParameter("publisherId")));
        search.setAvailableOnly("true".equals(request.getParameter("availableOnly")));
        search.setDigitalOnly("true".equals(request.getParameter("digitalOnly")));
        search.setSortBy(request.getParameter("sortBy") != null ? request.getParameter("sortBy") : "title");
        search.setSortDir(request.getParameter("sortDir") != null ? request.getParameter("sortDir") : "asc");
        search.setPage(parseInt(request.getParameter("page"), 0));
        search.setSize(parseInt(request.getParameter("size"), 10));

        PageResult<Book> pageResult = bookService.searchBooks(search);

        Map<String, Object> model = new HashMap<>();
        model.put("books", pageResult.getContent());
        model.put("pageResult", pageResult);
        model.put("search", search);
        model.put("categories", catalogService.getAllCategories());
        model.put("authors", catalogService.getAllAuthors());
        model.put("publishers", catalogService.getAllPublishers());
        addFlashToModel(request, model);

        render(request, response, "books/list", model);
    }

    private void showBook(HttpServletRequest request, HttpServletResponse response, Long id) throws IOException {
        Book book = bookService.getBookById(id).orElse(null);
        if (book == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("book", book);
        addFlashToModel(request, model);
        render(request, response, "books/detail", model);
    }

    private void showBookForm(HttpServletRequest request, HttpServletResponse response, Long id) throws IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("categories", catalogService.getAllCategories());
        model.put("publishers", catalogService.getAllPublishers());

        if (id != null) {
            bookService.getBookById(id).ifPresent(book -> {
                model.put("book", book);
                BookDto dto = new BookDto();
                dto.setId(book.getId());
                dto.setTitle(book.getTitle());
                dto.setIsbn(book.getIsbn());
                dto.setDescription(book.getDescription());
                dto.setEdition(book.getEdition());
                dto.setTotalCopies(book.getTotalCopies());
                dto.setTags(book.getTags());
                dto.setDigitalAvailable(book.isDigitalAvailable());
                dto.setCategoryId(book.getCategory().getId());
                if (book.getPublisher() != null) dto.setPublisherId(book.getPublisher().getId());
                dto.setAuthorNames(book.getAuthors().stream()
                        .map(a -> a.getName()).reduce((a, b) -> a + ", " + b).orElse(""));
                model.put("dto", dto);
            });
        }

        addFlashToModel(request, model);
        render(request, response, id == null ? "books/form" : "books/edit", model);
    }

    private void createBook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BookFormData formData = parseBookForm(request);
            bookService.createBook(formData.dto, formData.fileContent, formData.fileName, getUploadPath());
            setFlashMessage(request, "success", "Book added successfully!");
            redirect(response, "/books");
        } catch (LibraryException e) {
            setFlashMessage(request, "danger", e.getMessage());
            redirect(response, "/books/new");
        }
    }

    private void updateBook(HttpServletRequest request, HttpServletResponse response, Long id) throws IOException {
        try {
            BookFormData formData = parseBookForm(request);
            bookService.updateBook(id, formData.dto, formData.fileContent, formData.fileName, getUploadPath());
            setFlashMessage(request, "success", "Book updated successfully!");
            redirect(response, "/books/" + id);
        } catch (LibraryException e) {
            setFlashMessage(request, "danger", e.getMessage());
            redirect(response, "/books/" + id + "/edit");
        }
    }

    private void deleteBook(HttpServletRequest request, HttpServletResponse response, Long id) throws IOException {
        try {
            bookService.deleteBook(id, getUploadPath());
            setFlashMessage(request, "success", "Book deleted successfully!");
        } catch (LibraryException e) {
            setFlashMessage(request, "danger", e.getMessage());
        }
        redirect(response, "/books");
    }

    private BookFormData parseBookForm(HttpServletRequest request) throws IOException {
        BookDto dto = new BookDto();
        byte[] fileContent = null;
        String fileName = null;

        if (JakartaServletFileUpload.isMultipartContent(request)) {
            Path tempDir = Path.of(getServletContext().getRealPath("/"), "temp");
            DiskFileItemFactory factory = DiskFileItemFactory.builder().setPath(tempDir).get();
            JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> upload = new JakartaServletFileUpload<>(factory);
            upload.setFileSizeMax(50 * 1024 * 1024);

            List<DiskFileItem> items = upload.parseRequest(request);
            for (DiskFileItem item : items) {
                if (item.isFormField()) {
                    mapFormField(dto, item.getFieldName(), item.getString());
                } else if ("bookFile".equals(item.getFieldName()) && !item.getName().isBlank()) {
                    fileContent = item.getInputStream().readAllBytes();
                    fileName = item.getName();
                }
            }
        } else {
            dto.setTitle(request.getParameter("title"));
            dto.setIsbn(request.getParameter("isbn"));
            dto.setDescription(request.getParameter("description"));
            dto.setEdition(request.getParameter("edition"));
            dto.setTotalCopies(parseInt(request.getParameter("totalCopies"), 1));
            dto.setTags(request.getParameter("tags"));
            dto.setDigitalAvailable("on".equals(request.getParameter("digitalAvailable")));
            dto.setCategoryId(parseLong(request.getParameter("categoryId")));
            dto.setPublisherId(parseLong(request.getParameter("publisherId")));
            dto.setAuthorNames(request.getParameter("authorNames"));
        }

        return new BookFormData(dto, fileContent, fileName);
    }

    private void mapFormField(BookDto dto, String field, String value) {
        switch (field) {
            case "title" -> dto.setTitle(value);
            case "isbn" -> dto.setIsbn(value);
            case "description" -> dto.setDescription(value);
            case "edition" -> dto.setEdition(value);
            case "totalCopies" -> dto.setTotalCopies(parseInt(value, 1));
            case "tags" -> dto.setTags(value);
            case "digitalAvailable" -> dto.setDigitalAvailable("on".equals(value));
            case "categoryId" -> dto.setCategoryId(parseLong(value));
            case "publisherId" -> dto.setPublisherId(parseLong(value));
            case "authorNames" -> dto.setAuthorNames(value);
        }
    }

    private String getUploadPath() {
        return getServletContext().getRealPath("/");
    }

    private record BookFormData(BookDto dto, byte[] fileContent, String fileName) {}
}
