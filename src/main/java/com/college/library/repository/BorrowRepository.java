package com.college.library.repository;

import com.college.library.dto.MonthlyReportDto;
import com.college.library.dto.TopBookDto;
import com.college.library.entity.BorrowRecord;
import com.college.library.entity.BorrowStatus;
import com.college.library.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class BorrowRepository extends BaseRepository {

    public BorrowRecord save(BorrowRecord record) {
        return execute(session -> {
            if (record.getId() == null) {
                session.persist(record);
                return record;
            }
            return session.merge(record);
        });
    }

    public Optional<BorrowRecord> findById(Long id) {
        return executeReadOnly(session -> Optional.ofNullable(session.get(BorrowRecord.class, id)));
    }

    public Optional<BorrowRecord> findActiveBorrow(Long userId, Long bookId) {
        return executeReadOnly(session -> {
            String hql = """
                FROM BorrowRecord br
                WHERE br.user.id = :userId AND br.book.id = :bookId AND br.status = :status
                """;
            return session.createQuery(hql, BorrowRecord.class)
                    .setParameter("userId", userId)
                    .setParameter("bookId", bookId)
                    .setParameter("status", BorrowStatus.BORROWED)
                    .uniqueResultOptional();
        });
    }

    public List<BorrowRecord> findByUserId(Long userId) {
        return executeReadOnly(session -> {
            String hql = """
                FROM BorrowRecord br
                JOIN FETCH br.book b
                JOIN FETCH b.category
                WHERE br.user.id = :userId
                ORDER BY br.borrowDate DESC
                """;
            return session.createQuery(hql, BorrowRecord.class)
                    .setParameter("userId", userId)
                    .list();
        });
    }

    public List<BorrowRecord> findActiveBorrows() {
        return executeReadOnly(session -> {
            String hql = """
                FROM BorrowRecord br
                JOIN FETCH br.user u
                JOIN FETCH br.book b
                WHERE br.status = :status
                ORDER BY br.dueDate
                """;
            return session.createQuery(hql, BorrowRecord.class)
                    .setParameter("status", BorrowStatus.BORROWED)
                    .list();
        });
    }

    public long countActiveBorrows() {
        return executeReadOnly(session ->
                session.createQuery("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = :status", Long.class)
                        .setParameter("status", BorrowStatus.BORROWED)
                        .uniqueResult());
    }

    public long countOverdueBorrows() {
        return executeReadOnly(session ->
                session.createQuery("""
                    SELECT COUNT(br) FROM BorrowRecord br
                    WHERE br.status = :status AND br.dueDate < :today
                    """, Long.class)
                        .setParameter("status", BorrowStatus.BORROWED)
                        .setParameter("today", LocalDate.now())
                        .uniqueResult());
    }

    public List<TopBookDto> findTopBorrowedBooks(int limit) {
        return executeReadOnly(session -> {
            String hql = """
                SELECT new com.college.library.dto.TopBookDto(b.id, b.title, c.name, COUNT(br.id))
                FROM BorrowRecord br
                JOIN br.book b
                JOIN b.category c
                GROUP BY b.id, b.title, c.name
                ORDER BY COUNT(br.id) DESC
                """;
            return session.createQuery(hql, TopBookDto.class)
                    .setMaxResults(limit)
                    .list();
        });
    }

    public List<MonthlyReportDto> getMonthlyReports(int months) {
        return executeReadOnly(session -> {
            LocalDate startDate = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);

            String borrowHql = """
                SELECT YEAR(br.borrowDate), MONTH(br.borrowDate), COUNT(br.id)
                FROM BorrowRecord br
                WHERE br.borrowDate >= :startDate
                GROUP BY YEAR(br.borrowDate), MONTH(br.borrowDate)
                """;
            List<Object[]> borrowData = session.createQuery(borrowHql, Object[].class)
                    .setParameter("startDate", startDate)
                    .list();

            String returnHql = """
                SELECT YEAR(br.returnDate), MONTH(br.returnDate), COUNT(br.id)
                FROM BorrowRecord br
                WHERE br.returnDate IS NOT NULL AND br.returnDate >= :startDate
                GROUP BY YEAR(br.returnDate), MONTH(br.returnDate)
                """;
            List<Object[]> returnData = session.createQuery(returnHql, Object[].class)
                    .setParameter("startDate", startDate)
                    .list();

            String fineHql = """
                SELECT YEAR(f.createdAt), MONTH(f.createdAt), SUM(f.amount)
                FROM Fine f
                WHERE f.createdAt >= :startDateTime
                GROUP BY YEAR(f.createdAt), MONTH(f.createdAt)
                """;
            List<Object[]> fineData = session.createQuery(fineHql, Object[].class)
                    .setParameter("startDateTime", startDate.atStartOfDay())
                    .list();

            List<MonthlyReportDto> reports = new java.util.ArrayList<>();
            for (int i = 0; i < months; i++) {
                LocalDate date = startDate.plusMonths(i);
                int year = date.getYear();
                int month = date.getMonthValue();
                String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                long borrows = findCount(borrowData, year, month);
                long returns = findCount(returnData, year, month);
                double fines = findSum(fineData, year, month);

                reports.add(new MonthlyReportDto(year, month, monthName, borrows, returns, fines));
            }
            return reports;
        });
    }

    private long findCount(List<Object[]> data, int year, int month) {
        return data.stream()
                .filter(row -> ((Number) row[0]).intValue() == year && ((Number) row[1]).intValue() == month)
                .mapToLong(row -> ((Number) row[2]).longValue())
                .findFirst()
                .orElse(0L);
    }

    private double findSum(List<Object[]> data, int year, int month) {
        return data.stream()
                .filter(row -> ((Number) row[0]).intValue() == year && ((Number) row[1]).intValue() == month)
                .mapToDouble(row -> row[2] != null ? ((Number) row[2]).doubleValue() : 0.0)
                .findFirst()
                .orElse(0.0);
    }

    public void delete(Long id) {
        executeVoid(session -> {
            BorrowRecord record = session.get(BorrowRecord.class, id);
            if (record == null) {
                throw new ResourceNotFoundException("Borrow record not found: " + id);
            }
            session.remove(record);
        });
    }
}
