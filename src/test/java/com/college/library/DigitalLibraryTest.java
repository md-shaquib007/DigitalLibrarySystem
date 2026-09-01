package com.college.library;

import com.college.library.dto.BookDto;
import com.college.library.entity.Author;
import com.college.library.entity.Book;
import com.college.library.entity.Category;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.util.PasswordUtil;
import com.college.library.util.ValidationUtil;
import com.college.library.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DigitalLibraryTest {

    @Test
    @DisplayName("PasswordUtil hashing and verification test")
    void testPasswordHashing() {
        String plain = "LibraryAdminPass123";
        String hashed = PasswordUtil.hash(plain);

        assertNotNull(hashed);
        assertNotEquals(plain, hashed);
        assertTrue(PasswordUtil.verify(plain, hashed));
        assertFalse(PasswordUtil.verify("WrongPass", hashed));
    }

    @Test
    @DisplayName("User entity role and status check")
    void testUserRole() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@college.edu");
        user.setRole(Role.ADMIN);

        assertEquals("John Doe", user.getFullName());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    @DisplayName("BookDto validation with valid fields")
    void testValidBookDto() {
        BookDto dto = new BookDto();
        dto.setTitle("Effective Java");
        dto.setCategoryId(1L);
        dto.setIsbn("978-0134685991");

        assertDoesNotThrow(() -> ValidationUtil.validate(dto));
    }

    @Test
    @DisplayName("BookDto validation failure when title is blank")
    void testInvalidBookDto() {
        BookDto dto = new BookDto();
        dto.setTitle("");
        dto.setCategoryId(1L);

        assertThrows(ValidationException.class, () -> ValidationUtil.validate(dto));
    }

    @Test
    @DisplayName("Book and Category entity mapping")
    void testBookCategoryMapping() {
        Category cat = new Category();
        cat.setId(10L);
        cat.setName("Computer Science");

        Book book = new Book();
        book.setTitle("Design Patterns");
        book.setCategory(cat);

        assertEquals("Design Patterns", book.getTitle());
        assertEquals("Computer Science", book.getCategory().getName());
    }
}
