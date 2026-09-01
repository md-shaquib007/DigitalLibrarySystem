package com.college.library.service;

import com.college.library.entity.Author;
import com.college.library.entity.Category;
import com.college.library.entity.Publisher;
import com.college.library.exception.ResourceNotFoundException;
import com.college.library.exception.ValidationException;
import com.college.library.repository.AuthorRepository;
import com.college.library.repository.CategoryRepository;
import com.college.library.repository.PublisherRepository;

import java.util.List;

public class CatalogService {

    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final AuthorRepository authorRepository = new AuthorRepository();
    private final PublisherRepository publisherRepository = new PublisherRepository();

    // Categories
    public List<Category> getAllCategories() { return categoryRepository.findAll(); }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    public Category saveCategory(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new ValidationException("Category name is required");
        }
        categoryRepository.findByName(category.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(category.getId())) {
                throw new ValidationException("Category name already exists");
            }
        });
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) { categoryRepository.delete(id); }

    // Authors
    public List<Author> getAllAuthors() { return authorRepository.findAll(); }

    public Author saveAuthor(Author author) {
        if (author.getName() == null || author.getName().isBlank()) {
            throw new ValidationException("Author name is required");
        }
        return authorRepository.save(author);
    }

    public void deleteAuthor(Long id) { authorRepository.delete(id); }

    // Publishers
    public List<Publisher> getAllPublishers() { return publisherRepository.findAll(); }

    public Publisher savePublisher(Publisher publisher) {
        if (publisher.getName() == null || publisher.getName().isBlank()) {
            throw new ValidationException("Publisher name is required");
        }
        return publisherRepository.save(publisher);
    }

    public void deletePublisher(Long id) { publisherRepository.delete(id); }
}
