package com.college.library.repository;

import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class UserRepository extends BaseRepository {

    public User save(User user) {
        return execute(session -> {
            if (user.getId() == null) {
                session.persist(user);
                return user;
            }
            return session.merge(user);
        });
    }

    public Optional<User> findById(Long id) {
        return executeReadOnly(session -> Optional.ofNullable(session.get(User.class, id)));
    }

    public Optional<User> findByUsername(String username) {
        return executeReadOnly(session -> {
            String hql = "FROM User u WHERE u.username = :username";
            return session.createQuery(hql, User.class)
                    .setParameter("username", username)
                    .uniqueResultOptional();
        });
    }

    public Optional<User> findByEmail(String email) {
        return executeReadOnly(session -> {
            String hql = "FROM User u WHERE u.email = :email";
            return session.createQuery(hql, User.class)
                    .setParameter("email", email)
                    .uniqueResultOptional();
        });
    }

    public List<User> findByRole(Role role) {
        return executeReadOnly(session -> {
            String hql = "FROM User u WHERE u.role = :role ORDER BY u.fullName";
            return session.createQuery(hql, User.class)
                    .setParameter("role", role)
                    .list();
        });
    }

    public List<User> findAll() {
        return executeReadOnly(session -> {
            String hql = "FROM User u ORDER BY u.fullName";
            return session.createQuery(hql, User.class).list();
        });
    }

    public long count() {
        return executeReadOnly(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<User> root = cq.from(User.class);
            cq.select(cb.count(root));
            return session.createQuery(cq).getSingleResult();
        });
    }

    public void delete(Long id) {
        executeVoid(session -> {
            User user = session.get(User.class, id);
            if (user == null) {
                throw new ResourceNotFoundException("User not found: " + id);
            }
            session.remove(user);
        });
    }
}
