package servlet.admin;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.Trip;
import model.User;
import java.io.IOException;
import java.sql.Date;

/**
 * TripAdminServlet – Quản lý chuyến xe (Admin).
 *
 * URL: /admin/trips
 *
 * GET /admin/trips → Danh sách toàn bộ chuyến
 * GET /admin/trips?action=cancel&id=X → Hủy chuyến X (CANCELLED)
 * POST /admin/trips action=create → Tạo chuyến mới
 */
public class TripAdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        TripDAO tripDAO = new TripDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();
        RouteDAO routeDAO = new RouteDAO();
        AbsenceNotificationDAO anDAO = new AbsenceNotificationDAO();
        VehicleReportDAO vrDAO = new VehicleReportDAO();

        String action = request.getParameter("action");

        if ("cancel".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            tripDAO.cancel(id);
            response.sendRedirect(request.getContextPath() + "/admin/trips?msg=cancelled");
            return;
        }

        request.setAttribute("trips", tripDAO.getAll());
        request.setAttribute("vehicles", vehicleDAO.getAll());
        request.setAttribute("routes", routeDAO.getActive());
        request.setAttribute("absences", anDAO.getToday());
        request.setAttribute("reports", vrDAO.getOpen());
        request.setAttribute("msg", request.getParameter("msg"));
        request.getRequestDispatcher("/WEB-INF/views/admin/trips.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        TripDAO tripDAO = new TripDAO();
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            User loggedUser = (User) request.getSession().getAttribute("loggedUser");

            Trip newTrip = new Trip();
            newTrip.setVehicleId(Integer.parseInt(request.getParameter("vehicleId")));
            newTrip.setRouteId(Integer.parseInt(request.getParameter("routeId")));
            newTrip.setTripDate(Date.valueOf(request.getParameter("tripDate")));
            newTrip.setTripType(request.getParameter("tripType")); // MORNING / AFTERNOON
            newTrip.setNotes(request.getParameter("notes"));
            newTrip.setCreatedBy(loggedUser.getId());

            int tripId = tripDAO.insert(newTrip);
            response.sendRedirect(request.getContextPath() + "/admin/trips?msg="
                    + (tripId > 0 ? "created" : "error"));
        }
    }
}
