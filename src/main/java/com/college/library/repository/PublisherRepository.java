package com.college.library.repository;

import com.college.library.entity.Publisher;
import com.college.library.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

public class PublisherRepository extends BaseRepository {

    public Publisher save(Publisher publisher) {
        return execute(session -> {
            if (publisher.getId() == null) {
                session.persist(publisher);
                return publisher;
            }
            return session.merge(publisher);
        });
    }

    public Optional<Publisher> findById(Long id) {
        return executeReadOnly(session -> Optional.ofNullable(session.get(Publisher.class, id)));
    }

    public List<Publisher> findAll() {
        return executeReadOnly(session -> {
            String hql = "FROM Publisher p ORDER BY p.name";
            return session.createQuery(hql, Publisher.class).list();
        });
    }

    public void delete(Long id) {
        executeVoid(session -> {
            Publisher publisher = session.get(Publisher.class, id);
            if (publisher == null) {
                throw new ResourceNotFoundException("Publisher not found: " + id);
            }
            session.remove(publisher);
        });
    }
}
