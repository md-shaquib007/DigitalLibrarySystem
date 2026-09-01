package com.college.library.repository;

import com.college.library.entity.Reservation;
import com.college.library.entity.ReservationStatus;
import com.college.library.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReservationRepository extends BaseRepository {

    public Reservation save(Reservation reservation) {
        return execute(session -> {
            if (reservation.getId() == null) {
                session.persist(reservation);
                return reservation;
            }
            return session.merge(reservation);
        });
    }

    public Optional<Reservation> findById(Long id) {
        return executeReadOnly(session -> Optional.ofNullable(session.get(Reservation.class, id)));
    }

    public Optional<Reservation> findPendingByUserAndBook(Long userId, Long bookId) {
        return executeReadOnly(session -> {
            String hql = """
                FROM Reservation r
                WHERE r.user.id = :userId AND r.book.id = :bookId AND r.status = :status
                """;
            return session.createQuery(hql, Reservation.class)
                    .setParameter("userId", userId)
                    .setParameter("bookId", bookId)
                    .setParameter("status", ReservationStatus.PENDING)
                    .uniqueResultOptional();
        });
    }

    public List<Reservation> findByUserId(Long userId) {
        return executeReadOnly(session -> {
            String hql = """
                FROM Reservation r
                JOIN FETCH r.book b
                JOIN FETCH b.category
                WHERE r.user.id = :userId
                ORDER BY r.reservedAt DESC
                """;
            return session.createQuery(hql, Reservation.class)
                    .setParameter("userId", userId)
                    .list();
        });
    }

    public List<Reservation> findPendingReservations() {
        return executeReadOnly(session -> {
            String hql = """
                FROM Reservation r
                JOIN FETCH r.user u
                JOIN FETCH r.book b
                WHERE r.status = :status
                ORDER BY r.reservedAt
                """;
            return session.createQuery(hql, Reservation.class)
                    .setParameter("status", ReservationStatus.PENDING)
                    .list();
        });
    }

    public long countPending() {
        return executeReadOnly(session ->
                session.createQuery("SELECT COUNT(r) FROM Reservation r WHERE r.status = :status", Long.class)
                        .setParameter("status", ReservationStatus.PENDING)
                        .uniqueResult());
    }

    public void expireOldReservations() {
        executeVoid(session -> {
            String hql = """
                UPDATE Reservation r SET r.status = :expired
                WHERE r.status = :pending AND r.expiresAt < :now
                """;
            session.createMutationQuery(hql)
                    .setParameter("expired", ReservationStatus.EXPIRED)
                    .setParameter("pending", ReservationStatus.PENDING)
                    .setParameter("now", LocalDateTime.now())
                    .executeUpdate();
        });
    }

    public void delete(Long id) {
        executeVoid(session -> {
            Reservation reservation = session.get(Reservation.class, id);
            if (reservation == null) {
                throw new ResourceNotFoundException("Reservation not found: " + id);
            }
            session.remove(reservation);
        });
    }
}
