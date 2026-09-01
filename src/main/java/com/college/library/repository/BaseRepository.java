package com.college.library.repository;

import com.college.library.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseRepository {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected <T> T execute(Function<Session, T> action) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            T result = action.apply(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Database operation failed", e);
            throw e;
        }
    }

    protected void executeVoid(Consumer<Session> action) {
        execute(session -> {
            action.accept(session);
            return null;
        });
    }

    protected <T> T executeReadOnly(Function<Session, T> action) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return action.apply(session);
        }
    }
}
