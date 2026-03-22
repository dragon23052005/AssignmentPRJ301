package servlet.admin;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * AdminDashboardServlet – Trang tổng quan của Quản trị viên (Admin).
 *
 * URL: GET /admin/dashboard
 *
 * Hiển thị các số liệu tổng hợp hệ thống:
 * - Số phụ huynh, lái xe, quản lý xe (đang active)
 * - Số tuyến đường, xe, học sinh
 * - Số chuyến hôm nay
 * - Số thông báo nghỉ chờ xác nhận (PENDING)
 * - Số báo cáo sự cố chưa giải quyết (OPEN/IN_PROGRESS)
 * - 10 chuyến xe gần nhất
 * - Thông báo nghỉ hôm nay
 *
 * ⚠️ Hiệu suất: Servlet này tạo 7 DAO instances và thực hiện nhiều queries
 * mỗi lần tải trang. Nếu hệ thống lớn, nên xem xét cache hoặc
 * gộp các count query thành 1 stored procedure.
 */
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Khởi tạo các DAO cần thiết cho dashboard
        UserDAO userDAO = new UserDAO();
        RouteDAO routeDAO = new RouteDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();
        StudentDAO studentDAO = new StudentDAO();
        TripDAO tripDAO = new TripDAO();
        AbsenceNotificationDAO anDAO = new AbsenceNotificationDAO();
        VehicleReportDAO vrDAO = new VehicleReportDAO();

        // ── Thống kê số lượng người dùng theo role ────────────────────────
        request.setAttribute("totalParents", userDAO.countByRole("PARENT"));
        request.setAttribute("totalDrivers", userDAO.countByRole("DRIVER"));
        request.setAttribute("totalMonitors", userDAO.countByRole("MONITOR"));

        // ── Thống kê tài nguyên hệ thống ─────────────────────────────────
        request.setAttribute("totalRoutes", routeDAO.count());
        request.setAttribute("totalVehicles", vehicleDAO.count());
        request.setAttribute("totalStudents", studentDAO.count());

        // ── Thống kê hoạt động hôm nay ────────────────────────────────────
        request.setAttribute("todayTrips", tripDAO.countToday());
        request.setAttribute("pendingAbsences", anDAO.countPending()); // Cần xác nhận
        request.setAttribute("openReports", vrDAO.countOpen()); // Sự cố chưa xử lý

        // ── Dữ liệu hiển thị trên bảng/list ──────────────────────────────
        request.setAttribute("recentTrips", tripDAO.getRecent(10)); // 10 chuyến gần nhất
        request.setAttribute("todayAbsences", anDAO.getToday()); // Danh sách nghỉ hôm nay

        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }
}
