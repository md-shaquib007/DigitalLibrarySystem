package com.college.library.service;

import com.college.library.config.AppConstants;
import com.college.library.entity.*;
import com.college.library.exception.ResourceNotFoundException;
import com.college.library.exception.ValidationException;
import com.college.library.repository.BookRepository;
import com.college.library.repository.BorrowRepository;
import com.college.library.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BorrowService {

    private static final Logger log = LoggerFactory.getLogger(BorrowService.class);
    private final BorrowRepository borrowRepository = new BorrowRepository();
    private final BookRepository bookRepository = new BookRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();

    public BorrowRecord borrowBook(Long userId, Long bookId) {
        if (userId == null || bookId == null) {
            throw new ValidationException("User ID and Book ID are required");
        }

        synchronized (("BORROW_BOOK_" + bookId).intern()) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

            if (!book.isAvailable() || book.getAvailableCopies() <= 0) {
                throw new ValidationException("Book is not available for borrowing");
            }

            if (borrowRepository.findActiveBorrow(userId, bookId).isPresent()) {
                throw new ValidationException("You already have this book borrowed");
            }

            User user = new User();
            user.setId(userId);

            BorrowRecord record = new BorrowRecord();
            record.setUser(user);
            record.setBook(book);
            record.setBorrowDate(LocalDate.now());
            record.setDueDate(LocalDate.now().plusDays(AppConstants.BORROW_DAYS));
            record.setStatus(BorrowStatus.BORROWED);

            book.setAvailableCopies(Math.max(0, book.getAvailableCopies() - 1));
            bookRepository.save(book);

            BorrowRecord saved = borrowRepository.save(record);
            log.info("Book borrowed: user={}, book={}", userId, bookId);
            return saved;
        }
    }

    public BorrowRecord returnBook(Long borrowId) {
        if (borrowId == null) {
            throw new ValidationException("Borrow ID is required");
        }

        BorrowRecord record = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new ValidationException("Book already returned");
        }

        Book book = record.getBook();
        Long bookId = book != null ? book.getId() : 0L;

        synchronized (("BORROW_BOOK_" + bookId).intern()) {
            LocalDate returnDate = LocalDate.now();
            record.setReturnDate(returnDate);
            record.setStatus(BorrowStatus.RETURNED);

            if (book != null) {
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                bookRepository.save(book);
            }

            if (record.getDueDate() != null && returnDate.isAfter(record.getDueDate())) {
                long overdueDays = record.getOverdueDays();
                Fine fine = new Fine();
                fine.setBorrowRecord(record);
                fine.setOverdueDays((int) overdueDays);
                fine.setAmount(BigDecimal.valueOf(overdueDays * AppConstants.FINE_PER_DAY));
                record.setFine(fine);
            }

            BorrowRecord saved = borrowRepository.save(record);
            if (bookId > 0) {
                fulfillPendingReservations(bookId);
            }
            log.info("Book returned: borrowId={}", borrowId);
            return saved;
        }
    }

    private void fulfillPendingReservations(Long bookId) {
        if (bookId == null) return;
        reservationRepository.findPendingReservations().stream()
                .filter(r -> r.getBook() != null && bookId.equals(r.getBook().getId()))
                .findFirst()
                .ifPresent(r -> {
                    r.setStatus(ReservationStatus.FULFILLED);
                    reservationRepository.save(r);
                });
    }

    public List<BorrowRecord> getUserBorrowHistory(Long userId) {
        return borrowRepository.findByUserId(userId);
    }

    public List<BorrowRecord> getActiveBorrows() {
        return borrowRepository.findActiveBorrows();
    }

    public long countActiveBorrows() {
        return borrowRepository.countActiveBorrows();
    }

    public long countOverdueBorrows() {
        return borrowRepository.countOverdueBorrows();
    }

    public int recalculateOverdueFines() {
        List<BorrowRecord> activeBorrows = borrowRepository.findActiveBorrows();
        int updatedCount = 0;
        LocalDate today = LocalDate.now();
        for (BorrowRecord record : activeBorrows) {
            if (record.getDueDate() != null && today.isAfter(record.getDueDate())) {
                long overdueDays = record.getOverdueDays();
                if (overdueDays > 0) {
                    Fine fine = record.getFine();
                    if (fine == null) {
                        fine = new Fine();
                        fine.setBorrowRecord(record);
                        record.setFine(fine);
                    }
                    fine.setOverdueDays((int) overdueDays);
                    fine.setAmount(BigDecimal.valueOf(overdueDays * AppConstants.FINE_PER_DAY));
                    borrowRepository.save(record);
                    updatedCount++;
                }
            }
        }
        log.info("Recalculated overdue fines for {} active records", updatedCount);
        return updatedCount;
    }
}
