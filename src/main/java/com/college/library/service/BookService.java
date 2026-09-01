package com.college.library.service;

import com.college.library.dto.BookDto;
import com.college.library.dto.BookSearchDto;
import com.college.library.dto.PageResult;
import com.college.library.entity.*;
import com.college.library.exception.ResourceNotFoundException;
import com.college.library.exception.ValidationException;
import com.college.library.repository.AuthorRepository;
import com.college.library.repository.BookRepository;
import com.college.library.repository.CategoryRepository;
import com.college.library.repository.PublisherRepository;
import com.college.library.util.FileUploadUtil;
import com.college.library.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository = new BookRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final AuthorRepository authorRepository = new AuthorRepository();
    private final PublisherRepository publisherRepository = new PublisherRepository();

    public PageResult<Book> searchBooks(BookSearchDto search) {
        return bookRepository.search(search);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findByIdWithDetails(id);
    }

    public Book createBook(BookDto dto, byte[] fileContent, String originalFilename, String uploadBasePath) {
        ValidationUtil.validate(dto);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Book book = mapDtoToEntity(new Book(), dto, category);

        if (fileContent != null && fileContent.length > 0) {
            String filePath = FileUploadUtil.saveBookFile(fileContent, originalFilename, uploadBasePath);
            book.setFilePath(filePath);
            book.setFileName(originalFilename);
            book.setDigitalAvailable(true);
        }

        Book saved = bookRepository.save(book);
        log.info("Book created: {}", saved.getTitle());
        return saved;
    }

    public Book updateBook(Long id, BookDto dto, byte[] fileContent, String originalFilename, String uploadBasePath) {
        ValidationUtil.validate(dto);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        mapDtoToEntity(book, dto, category);

        if (fileContent != null && fileContent.length > 0) {
            FileUploadUtil.deleteFile(book.getFilePath(), uploadBasePath);
            String filePath = FileUploadUtil.saveBookFile(fileContent, originalFilename, uploadBasePath);
            book.setFilePath(filePath);
            book.setFileName(originalFilename);
            book.setDigitalAvailable(true);
        }

        return bookRepository.save(book);
    }

    public void deleteBook(Long id, String uploadBasePath) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        FileUploadUtil.deleteFile(book.getFilePath(), uploadBasePath);
        bookRepository.delete(id);
        log.info("Book deleted: {}", book.getTitle());
    }

    private Book mapDtoToEntity(Book book, BookDto dto, Category category) {
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setDescription(dto.getDescription());
        book.setEdition(dto.getEdition());
        book.setTags(dto.getTags());
        book.setCategory(category);

        if (dto.getPublisherId() != null) {
            Publisher publisher = publisherRepository.findById(dto.getPublisherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Publisher not found"));
            book.setPublisher(publisher);
        } else {
            book.setPublisher(null);
        }

        book.setAuthors(resolveAuthors(dto.getAuthorNames()));
        return book;
    }

    private Set<Author> resolveAuthors(String authorNames) {
        if (authorNames == null || authorNames.isBlank()) {
            return new HashSet<>();
        }
        return Arrays.stream(authorNames.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(name -> authorRepository.findByName(name)
                        .orElseGet(() -> {
                            Author author = new Author();
                            author.setName(name);
                            return authorRepository.save(author);
                        }))
                .collect(Collectors.toSet());
    }

    public long getTotalBooks() {
        return bookRepository.count();
    }
}
