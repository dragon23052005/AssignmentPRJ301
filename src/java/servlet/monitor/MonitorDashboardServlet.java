package servlet.monitor;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.*;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * MonitorDashboardServlet – Trang chủ của Monitor (quản lý xe).
 *
 * URL: GET /monitor/dashboard
 *
 * Hiển thị tổng quan cho Monitor:
 *   - Thông tin xe được phân công
 *   - Danh sách chuyến hôm nay của xe
 *   - Thông báo nghỉ hôm nay (để biết ai cần đánh NOTIFIED_ABSENT)
 *   - Chuyến đang active (DEPARTED) hoặc chuyến cuối cùng nếu không có
 *   - Danh sách điểm danh của chuyến active đó
 *
 * Logic chọn "activeTrip":
 *   - Ưu tiên chuyến đang DEPARTED (xe đang chạy)
 *   - Nếu không có → chọn chuyến cuối cùng trong ngày (để xem lại)
 */
public class MonitorDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        VehicleDAO            vehicleDAO = new VehicleDAO();
        TripDAO               tripDAO    = new TripDAO();
        AttendanceDAO         attDAO     = new AttendanceDAO();
        AbsenceNotificationDAO anDAO     = new AbsenceNotificationDAO();

        // Lấy thông tin user từ session
        User user = (User) request.getSession().getAttribute("loggedUser");

        String action = request.getParameter("action");
        if ("acknowledge".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            anDAO.acknowledge(id, user.getId());
            response.sendRedirect(request.getContextPath() + "/monitor/dashboard?msg=ack");
            return;
        }

        // Tìm DANH SÁCH xe được phân công cho Monitor hiện tại
        List<Vehicle> vehicles = vehicleDAO.getListByMonitorId(user.getId());
        request.setAttribute("vehicles", vehicles);
        
        // Vẫn set xe đầu tiên thành vehicle để tương thích ngược nếu giao diện phụ thuộc
        Vehicle firstVehicle = vehicles.isEmpty() ? null : vehicles.get(0);
        request.setAttribute("vehicle", firstVehicle);

        if (!vehicles.isEmpty()) {
            Date today = Date.valueOf(java.time.LocalDate.now());

            // Load chuyến hôm nay của TẤT CẢ các xe mà Monitor quản lý
            List<Trip> todayTrips = new java.util.ArrayList<>();
            for (Vehicle v : vehicles) {
                todayTrips.addAll(tripDAO.getByVehicleAndDate(v.getId(), today));
            }
            
            // Thông báo nghỉ hôm nay (cho tất cả học sinh, chung cho DB)
            request.setAttribute("todayTrips",    todayTrips);
            request.setAttribute("todayAbsences", anDAO.getToday());

            // Xác định chuyến đang active để hiển thị điểm danh
            // Ưu tiên chuyến đang DEPARTED; fallback về chuyến cuối nếu tất cả đã xong
            Trip activeTrip = todayTrips.stream()
                .filter(t -> "DEPARTED".equals(t.getStatus()))
                .findFirst()
                .orElse(null);

            if (activeTrip == null && !todayTrips.isEmpty()) {
                // Không có chuyến đang chạy → lấy chuyến cuối cùng trong ngày
                activeTrip = todayTrips.get(todayTrips.size() - 1);
            }

            request.setAttribute("activeTrip", activeTrip);

            // Load điểm danh của chuyến active (để hiển thị bảng điểm danh nhanh)
            if (activeTrip != null) {
                request.setAttribute("attendances", attDAO.getByTrip(activeTrip.getId()));
            }
        }
        // Nếu vehicle == null → JSP hiển thị thông báo "chưa được phân công xe"

        request.getRequestDispatcher("/WEB-INF/views/monitor/dashboard.jsp").forward(request, response);
    }
}
