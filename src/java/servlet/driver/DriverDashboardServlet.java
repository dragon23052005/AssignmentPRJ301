package servlet.driver;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.*;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * DriverDashboardServlet – Trang chủ của Lái xe (Driver).
 *
 * URL: GET /driver/dashboard
 *
 * Hiển thị thông tin cho Lái xe:
 *   - Thông tin xe được giao (biển số, tuyến, v.v.)
 *   - Danh sách chuyến hôm nay của xe (xem ca nào đã đi, đang đi)
 *   - Lịch sử báo cáo sự cố xe gần đây
 *
 * ⚠️ Lái xe chỉ xem, không có quyền thay đổi điểm danh
 *    (đó là trách nhiệm của Monitor).
 *    Lái xe chỉ có quyền báo cáo sự cố xe (VehicleReportServlet).
 */
public class DriverDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        VehicleDAO       vehicleDAO = new VehicleDAO();
        TripDAO          tripDAO    = new TripDAO();
        VehicleReportDAO vrDAO      = new VehicleReportDAO();

        // Lấy thông tin lái xe từ session
        User user = (User) request.getSession().getAttribute("loggedUser");

        // Tìm xe được phân công cho lái xe này
        Vehicle vehicle = vehicleDAO.getByDriverId(user.getId());
        request.setAttribute("vehicle", vehicle);

        if (vehicle != null) {
            Date today = Date.valueOf(java.time.LocalDate.now());

            // Danh sách chuyến hôm nay của xe
            List<Trip> todayTrips = tripDAO.getByVehicleAndDate(vehicle.getId(), today);
            request.setAttribute("todayTrips", todayTrips);

            // Lịch sử báo cáo sự cố của xe (để lái xe xem lại các báo cáo đã gửi)
            request.setAttribute("vehicleReports", vrDAO.getByVehicle(vehicle.getId()));
        }
        // Nếu vehicle == null → JSP hiển thị "chưa được phân công xe"

        request.setAttribute("msg", request.getParameter("msg")); // Thông báo sau redirect
        request.getRequestDispatcher("/WEB-INF/views/driver/dashboard.jsp").forward(request, response);
    }
}
