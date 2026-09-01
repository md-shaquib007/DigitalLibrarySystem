package com.college.library.service;

import com.college.library.entity.Book;
import com.college.library.entity.Reservation;
import com.college.library.entity.ReservationStatus;
import com.college.library.entity.User;
import com.college.library.exception.ResourceNotFoundException;
import com.college.library.exception.ValidationException;
import com.college.library.repository.BookRepository;
import com.college.library.repository.BorrowRepository;
import com.college.library.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final BookRepository bookRepository = new BookRepository();
    private final BorrowRepository borrowRepository = new BorrowRepository();

    public Reservation reserveBook(Long userId, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (book.isAvailable()) {
            throw new ValidationException("Book is available — borrow it directly instead of reserving");
        }

        if (reservationRepository.findPendingByUserAndBook(userId, bookId).isPresent()) {
            throw new ValidationException("You already have a pending reservation for this book");
        }

        if (borrowRepository.findActiveBorrow(userId, bookId).isPresent()) {
            throw new ValidationException("You already have this book borrowed");
        }

        User user = new User();
        user.setId(userId);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStatus(ReservationStatus.PENDING);

        Reservation saved = reservationRepository.save(reservation);
        log.info("Book reserved: user={}, book={}", userId, bookId);
        return saved;
    }

    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new ValidationException("Cannot cancel another user's reservation");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ValidationException("Only pending reservations can be cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    public List<Reservation> getUserReservations(Long userId) {
        reservationRepository.expireOldReservations();
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> getPendingReservations() {
        reservationRepository.expireOldReservations();
        return reservationRepository.findPendingReservations();
    }

    public long countPendingReservations() {
        return reservationRepository.countPending();
    }
}
