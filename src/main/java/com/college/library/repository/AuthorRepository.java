package com.college.library.repository;

import com.college.library.entity.Author;
import com.college.library.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

public class AuthorRepository extends BaseRepository {

    public Author save(Author author) {
        return execute(session -> {
            if (author.getId() == null) {
                session.persist(author);
                return author;
            }
            return session.merge(author);
        });
    }

    public Optional<Author> findById(Long id) {
        return executeReadOnly(session -> Optional.ofNullable(session.get(Author.class, id)));
    }

    public Optional<Author> findByName(String name) {
        return executeReadOnly(session -> {
            String hql = "FROM Author a WHERE a.name = :name";
            return session.createQuery(hql, Author.class)
                    .setParameter("name", name)
                    .uniqueResultOptional();
        });
    }

    public List<Author> findAll() {
        return executeReadOnly(session -> {
            String hql = "FROM Author a ORDER BY a.name";
            return session.createQuery(hql, Author.class).list();
        });
    }

    public void delete(Long id) {
        executeVoid(session -> {
            Author author = session.get(Author.class, id);
            if (author == null) {
                throw new ResourceNotFoundException("Author not found: " + id);
            }
            session.remove(author);
        });
    }
}
