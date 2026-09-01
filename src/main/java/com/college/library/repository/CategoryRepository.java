package com.college.library.repository;

import com.college.library.entity.Category;
import com.college.library.exception.ResourceNotFoundException;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class CategoryRepository extends BaseRepository {

    public Category save(Category category) {
        return execute(session -> {
            if (category.getId() == null) {
                session.persist(category);
                return category;
            }
            return session.merge(category);
        });
    }

    public Optional<Category> findById(Long id) {
        return executeReadOnly(session -> Optional.ofNullable(session.get(Category.class, id)));
    }

    public Optional<Category> findByName(String name) {
        return executeReadOnly(session -> {
            String hql = "FROM Category c WHERE c.name = :name";
            return session.createQuery(hql, Category.class)
                    .setParameter("name", name)
                    .uniqueResultOptional();
        });
    }

    public List<Category> findAll() {
        return executeReadOnly(session -> {
            String hql = "FROM Category c ORDER BY c.name";
            return session.createQuery(hql, Category.class).list();
        });
    }

    public long count() {
        return executeReadOnly(session ->
                session.createQuery("SELECT COUNT(c) FROM Category c", Long.class).uniqueResult());
    }

    public void delete(Long id) {
        executeVoid(session -> {
            Category category = session.get(Category.class, id);
            if (category == null) {
                throw new ResourceNotFoundException("Category not found: " + id);
            }
            session.remove(category);
        });
    }
}
