package com.college.library.config;

import com.college.library.entity.*;
import com.college.library.repository.*;
import com.college.library.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@WebListener
public class DataInitializer implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Initializing Digital Library System...");
        seedData();
        log.info("Digital Library System ready!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        HibernateUtil.shutdown();
    }

    private void seedData() {
        UserRepository userRepo = new UserRepository();
        CategoryRepository categoryRepo = new CategoryRepository();
        AuthorRepository authorRepo = new AuthorRepository();
        PublisherRepository publisherRepo = new PublisherRepository();
        BookRepository bookRepo = new BookRepository();

        if (userRepo.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(PasswordUtil.hash("admin123"));
            admin.setFullName("System Administrator");
            admin.setEmail("admin@college.edu");
            admin.setRole(Role.ADMIN);
            userRepo.save(admin);
            log.info("Default admin created (admin / admin123)");
        }

        if (userRepo.findByUsername("librarian").isEmpty()) {
            User librarian = new User();
            librarian.setUsername("librarian");
            librarian.setPassword(PasswordUtil.hash("lib123"));
            librarian.setFullName("College Librarian");
            librarian.setEmail("librarian@college.edu");
            librarian.setRole(Role.LIBRARIAN);
            userRepo.save(librarian);
        }

        if (categoryRepo.count() == 0) {
            String[][] categories = {
                {"Technology", "Programming, Full Stack, DevOps, Cloud", "bi-laptop"},
                {"Ethical Hacking", "Cybersecurity, Penetration Testing, Network Security", "bi-shield-lock"},
                {"Marketing", "Digital Marketing, SEO, Branding, Growth", "bi-megaphone"},
                {"Product Management", "Product Strategy, Agile, UX Research", "bi-kanban"},
                {"Personal Development", "Leadership, Productivity, Communication", "bi-person-check"},
                {"Business", "Entrepreneurship, Finance, Management", "bi-briefcase"}
            };
            for (String[] cat : categories) {
                Category category = new Category();
                category.setName(cat[0]);
                category.setDescription(cat[1]);
                category.setIcon(cat[2]);
                categoryRepo.save(category);
            }
        }

        if (bookRepo.count() == 0) {
            seedSampleBooks(categoryRepo, authorRepo, publisherRepo, bookRepo);
        }
    }

    private void seedSampleBooks(CategoryRepository categoryRepo, AuthorRepository authorRepo,
                                  PublisherRepository publisherRepo, BookRepository bookRepo) {
        Category tech = categoryRepo.findByName("Technology").orElse(null);
        Category hacking = categoryRepo.findByName("Ethical Hacking").orElse(null);
        Category marketing = categoryRepo.findByName("Marketing").orElse(null);

        Publisher oreilly = new Publisher();
        oreilly.setName("O'Reilly Media");
        oreilly.setWebsite("https://oreilly.com");
        publisherRepo.save(oreilly);

        Author martin = new Author();
        martin.setName("Robert C. Martin");
        authorRepo.save(martin);

        if (tech != null) {
            Book mernBook = new Book();
            mernBook.setTitle("Full Stack Development with MERN");
            mernBook.setIsbn("978-1234567890");
            mernBook.setDescription("Complete guide to MongoDB, Express, React, and Node.js for building modern web applications.");
            mernBook.setEdition("2nd Edition");
            mernBook.setTotalCopies(5);
            mernBook.setAvailableCopies(5);
            mernBook.setTags("MERN, React, Node.js, MongoDB, Express, JavaScript, Full Stack");
            mernBook.setCategory(tech);
            mernBook.setPublisher(oreilly);
            mernBook.setAuthors(Set.of(martin));
            bookRepo.save(mernBook);

            Book htmlCss = new Book();
            htmlCss.setTitle("HTML & CSS: Design and Build Websites");
            htmlCss.setDescription("Learn HTML5 and CSS3 from scratch. Perfect for beginners starting their web development journey.");
            htmlCss.setTotalCopies(3);
            htmlCss.setAvailableCopies(3);
            htmlCss.setTags("HTML, CSS, Web Development, Frontend");
            htmlCss.setCategory(tech);
            bookRepo.save(htmlCss);
        }

        if (hacking != null) {
            Book hackBook = new Book();
            hackBook.setTitle("The Web Application Hacker's Handbook");
            hackBook.setDescription("Finding and exploiting security flaws in web applications. Essential for ethical hacking students.");
            hackBook.setTotalCopies(2);
            hackBook.setAvailableCopies(2);
            hackBook.setTags("Ethical Hacking, Security, Penetration Testing, OWASP");
            hackBook.setCategory(hacking);
            bookRepo.save(hackBook);
        }

        if (marketing != null) {
            Book marketingBook = new Book();
            marketingBook.setTitle("Digital Marketing for Dummies");
            marketingBook.setDescription("Comprehensive guide to SEO, social media marketing, content strategy, and analytics.");
            marketingBook.setTotalCopies(4);
            marketingBook.setAvailableCopies(4);
            marketingBook.setTags("Marketing, SEO, Social Media, Digital Strategy");
            marketingBook.setCategory(marketing);
            bookRepo.save(marketingBook);
        }
    }
}
