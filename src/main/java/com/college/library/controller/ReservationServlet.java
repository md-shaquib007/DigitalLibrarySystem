package com.college.library.controller;

import com.college.library.entity.Reservation;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.service.ReservationService;
import com.college.library.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ReservationServlet", urlPatterns = {"/reservations", "/reservations/*"})
public class ReservationServlet extends BaseServlet {

    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = SessionUtil.getLoggedInUser(request);
        if (currentUser == null) {
            redirect(response, "/login");
            return;
        }

        boolean isLibrarian = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.LIBRARIAN;
        boolean staffView = isLibrarian && "all".equalsIgnoreCase(request.getParameter("view"));

        List<Reservation> reservations = staffView
                ? reservationService.getPendingReservations()
                : reservationService.getUserReservations(currentUser.getId());

        Map<String, Object> model = new HashMap<>();
        model.put("reservations", reservations);
        model.put("staffView", staffView);
        addFlashToModel(request, model);

        render(request, response, "reservations/list", model);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = SessionUtil.getLoggedInUser(request);
        if (currentUser == null) {
            redirect(response, "/login");
            return;
        }

        String pathInfo = request.getPathInfo() != null ? request.getPathInfo() : "";
        try {
            if (pathInfo.endsWith("/cancel")) {
                String[] parts = pathInfo.split("/");
                if (parts.length >= 2) {
                    Long reservationId = parseLong(parts[1]);
                    reservationService.cancelReservation(reservationId, currentUser.getId());
                    setFlashMessage(request, "success", "Reservation cancelled.");
                }
            } else {
                Long bookId = parseLong(request.getParameter("bookId"));
                if (bookId != null) {
                    reservationService.reserveBook(currentUser.getId(), bookId);
                    setFlashMessage(request, "success", "Book reserved successfully!");
                } else {
                    setFlashMessage(request, "danger", "Book ID is required.");
                }
            }
        } catch (Exception e) {
            setFlashMessage(request, "danger", e.getMessage());
        }

        redirect(response, "/reservations");
    }
}
