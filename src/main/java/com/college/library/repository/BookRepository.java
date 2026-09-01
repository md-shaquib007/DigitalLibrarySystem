package com.college.library.repository;

import com.college.library.dto.BookSearchDto;
import com.college.library.dto.PageResult;
import com.college.library.entity.Book;
import com.college.library.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookRepository extends BaseRepository {

    public Book save(Book book) {
        return execute(session -> {
            if (book.getId() == null) {
                session.persist(book);
                return book;
            }
            return session.merge(book);
        });
    }

    public Optional<Book> findById(Long id) {
        return executeReadOnly(session -> Optional.ofNullable(session.get(Book.class, id)));
    }

    public Optional<Book> findByIdWithDetails(Long id) {
        return executeReadOnly(session -> {
            String hql = """
                SELECT DISTINCT b FROM Book b
                LEFT JOIN FETCH b.category
                LEFT JOIN FETCH b.publisher
                LEFT JOIN FETCH b.authors
                WHERE b.id = :id
                """;
            return session.createQuery(hql, Book.class)
                    .setParameter("id", id)
                    .uniqueResultOptional();
        });
    }

    public PageResult<Book> search(BookSearchDto search) {
        return executeReadOnly(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Book> cq = cb.createQuery(Book.class);
            Root<Book> root = cq.from(Book.class);
            root.fetch("category", JoinType.LEFT);
            root.fetch("publisher", JoinType.LEFT);

            List<Predicate> predicates = buildPredicates(cb, root, search);
            cq.where(predicates.toArray(new Predicate[0]));
            cq.distinct(true);

            applySorting(cb, cq, root, search);

            List<Book> books = session.createQuery(cq)
                    .setFirstResult(search.getPage() * search.getSize())
                    .setMaxResults(search.getSize())
                    .list();

            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Book> countRoot = countQuery.from(Book.class);
            List<Predicate> countPredicates = buildPredicates(cb, countRoot, search);
            countQuery.select(cb.countDistinct(countRoot));
            countQuery.where(countPredicates.toArray(new Predicate[0]));

            long total = session.createQuery(countQuery).getSingleResult();
            return new PageResult<>(books, total, search.getPage(), search.getSize());
        });
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Book> root, BookSearchDto search) {
        List<Predicate> predicates = new ArrayList<>();

        if (search.getKeyword() != null && !search.getKeyword().isBlank()) {
            String pattern = "%" + search.getKeyword().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("tags")), pattern),
                    cb.like(cb.lower(root.get("isbn")), pattern)
            ));
        }
        if (search.getCategoryId() != null) {
            predicates.add(cb.equal(root.get("category").get("id"), search.getCategoryId()));
        }
        if (search.getPublisherId() != null) {
            predicates.add(cb.equal(root.get("publisher").get("id"), search.getPublisherId()));
        }
        if (search.getAuthorId() != null) {
            Join<Object, Object> authors = root.join("authors", JoinType.INNER);
            predicates.add(cb.equal(authors.get("id"), search.getAuthorId()));
        }
        if (Boolean.TRUE.equals(search.getAvailableOnly())) {
            predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
        }
        if (Boolean.TRUE.equals(search.getDigitalOnly())) {
            predicates.add(cb.isTrue(root.get("digitalAvailable")));
        }

        return predicates;
    }

    private void applySorting(CriteriaBuilder cb, CriteriaQuery<Book> cq, Root<Book> root, BookSearchDto search) {
        String sortBy = search.getSortBy() != null ? search.getSortBy() : "title";
        boolean asc = !"desc".equalsIgnoreCase(search.getSortDir());

        Order order = switch (sortBy) {
            case "createdAt" -> asc ? cb.asc(root.get("createdAt")) : cb.desc(root.get("createdAt"));
            case "availableCopies" -> asc ? cb.asc(root.get("availableCopies")) : cb.desc(root.get("availableCopies"));
            default -> asc ? cb.asc(root.get("title")) : cb.desc(root.get("title"));
        };
        cq.orderBy(order);
    }

    public long count() {
        return executeReadOnly(session ->
                session.createQuery("SELECT COUNT(b) FROM Book b", Long.class).uniqueResult());
    }

    public List<Book> findRecent(int limit) {
        return executeReadOnly(session -> {
            String hql = "FROM Book b JOIN FETCH b.category ORDER BY b.createdAt DESC";
            return session.createQuery(hql, Book.class).setMaxResults(limit).list();
        });
    }

    public void delete(Long id) {
        executeVoid(session -> {
            Book book = session.get(Book.class, id);
            if (book == null) {
                throw new ResourceNotFoundException("Book not found: " + id);
            }
            session.remove(book);
        });
    }
}
