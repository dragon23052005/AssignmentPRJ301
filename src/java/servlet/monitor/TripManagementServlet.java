package servlet.monitor;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.*;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * TripManagementServlet – Quản lý chuyến xe của Monitor.
 *
 * URL: /monitor/trip
 *
 * Vai trò: Monitor (quản lý xe) dùng servlet này để:
 *   - Xem danh sách chuyến của xe mình hôm nay
 *   - Tạo chuyến mới (tự động khởi tạo điểm danh và báo vắng)
 *   - Báo khởi hành (SCHEDULED → DEPARTED)
 *   - Báo hoàn thành (DEPARTED → COMPLETED)
 *   - Cập nhật ghi chú chuyến
 *
 * GET  /monitor/trip              → Xem danh sách + chi tiết chuyến
 * GET  /monitor/trip?action=depart&tripId=X   → Khởi hành
 * GET  /monitor/trip?action=complete&tripId=X → Hoàn thành
 * POST /monitor/trip action=createTrip  → Tạo chuyến mới
 * POST /monitor/trip action=updateNotes → Cập nhật ghi chú
 *
 * Logic quan trọng khi tạo chuyến:
 *   1. Lấy danh sách HS đăng ký tuyến này hôm nay
 *   2. Khởi tạo điểm danh mặc định ABSENT cho tất cả
 *   3. Tự động set NOTIFIED_ABSENT cho HS đã báo nghỉ hôm nay
 */
public class TripManagementServlet extends HttpServlet {

    /**
     * GET: Xử lý xem danh sách chuyến và các action nhanh (depart, complete).
     *
     * Luồng:
     *   1. Xác định xe của Monitor đang đăng nhập
     *   2. Nếu có action (depart/complete) → thực hiện rồi redirect
     *   3. Lấy danh sách chuyến hôm nay của xe
     *   4. Xác định chuyến đang chọn (theo tripId param hoặc mặc định chuyến đầu)
     *   5. Load điểm danh + danh sách điểm dừng của chuyến được chọn
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Khởi tạo DAO cần dùng
        VehicleDAO    vehicleDAO = new VehicleDAO();
        TripDAO       tripDAO    = new TripDAO();
        AttendanceDAO attDAO     = new AttendanceDAO();

        // Lấy thông tin user đang đăng nhập từ session
        User user = (User) request.getSession().getAttribute("loggedUser");

        // ── Kiểm tra xe được phân công ──────────────────────────────────────
        List<Vehicle> vehicles = vehicleDAO.getListByMonitorId(user.getId());
        if (vehicles.isEmpty()) {
            // Monitor chưa được phân công xe → hiển thị lỗi
            request.setAttribute("error", "Bạn chưa được phân công xe nào!");
            request.getRequestDispatcher("/WEB-INF/views/monitor/trip.jsp").forward(request, response);
            return;
        }

        String action    = request.getParameter("action");
        String tripIdStr = request.getParameter("tripId");

        // ── Xử lý action nhanh: khởi hành ───────────────────────────────────
        if ("depart".equals(action) && tripIdStr != null) {
            int tripId = parseIntSafe(tripIdStr, -1);
            if (tripId > 0) {
                tripDAO.depart(tripId);
                // Redirect để tránh resubmit khi F5
                response.sendRedirect(request.getContextPath() + "/monitor/trip?tripId=" + tripIdStr + "&msg=departed");
            }
            return;
        }

        // ── Xử lý action nhanh: hoàn thành ──────────────────────────────────
        if ("complete".equals(action) && tripIdStr != null) {
            int tripId = parseIntSafe(tripIdStr, -1);
            if (tripId > 0) {
                tripDAO.complete(tripId);
                response.sendRedirect(request.getContextPath() + "/monitor/trip?tripId=" + tripIdStr + "&msg=completed");
            }
            return;
        }

        // ── Load dữ liệu hiển thị ────────────────────────────────────────────
        Date today = Date.valueOf(java.time.LocalDate.now());
        List<Trip> todayTrips = new java.util.ArrayList<>();
        for (Vehicle v : vehicles) {
            todayTrips.addAll(tripDAO.getByVehicleAndDate(v.getId(), today));
        }

        // Xác định chuyến được chọn để xem chi tiết điểm danh
        Trip selectedTrip = null;
        if (tripIdStr != null) {
            // Có chỉ định tripId trong URL → load chuyến đó
            selectedTrip = tripDAO.getById(parseIntSafe(tripIdStr, -1));
        } else if (!todayTrips.isEmpty()) {
            // Không chỉ định → mặc định chọn chuyến đầu tiên trong ngày
            selectedTrip = todayTrips.get(0);
        }

        // Đẩy dữ liệu vào request để JSP hiển thị
        request.setAttribute("vehicles",     vehicles); // Gửi List xe ra View để chọn tạo chuyến
        request.setAttribute("vehicle",      vehicles.get(0)); // Cho tương thích
        request.setAttribute("todayTrips",   todayTrips);
        request.setAttribute("selectedTrip", selectedTrip);

        if (selectedTrip != null) {
            // Load danh sách điểm danh và điểm dừng của chuyến được chọn
            request.setAttribute("attendances", attDAO.getByTrip(selectedTrip.getId()));
            request.setAttribute("stops", new StopDAO().getByRoute(selectedTrip.getRouteId()));
        }

        request.setAttribute("msg", request.getParameter("msg")); // Thông báo sau redirect
        request.getRequestDispatcher("/WEB-INF/views/monitor/trip.jsp").forward(request, response);
    }

    /**
     * POST: Xử lý các thao tác thay đổi dữ liệu (createTrip, updateNotes).
     *
     * action=createTrip:
     *   - Tạo chuyến mới cho xe của Monitor
     *   - Tự động khởi tạo điểm danh cho tất cả HS trong tuyến hôm nay
     *   - Tự động đánh NOTIFIED_ABSENT cho HS đã báo nghỉ
     *
     * action=updateNotes:
     *   - Cập nhật ghi chú của chuyến (ghi chú sự cố, thời tiết, v.v.)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        VehicleDAO             vehicleDAO = new VehicleDAO();
        TripDAO                tripDAO    = new TripDAO();
        StudentDAO             studentDAO = new StudentDAO();
        AttendanceDAO          attDAO     = new AttendanceDAO();
        AbsenceNotificationDAO anDAO      = new AbsenceNotificationDAO();

        User    user    = (User) request.getSession().getAttribute("loggedUser");
        String  action  = request.getParameter("action");
        List<Vehicle> vehicles = vehicleDAO.getListByMonitorId(user.getId());

        // ── Tạo chuyến mới ───────────────────────────────────────────────────
        if ("createTrip".equals(action) && !vehicles.isEmpty()) {
            int vehicleId = parseIntSafe(request.getParameter("vehicleId"), -1);
            Vehicle selectedVehicle = vehicles.stream().filter(v -> v.getId() == vehicleId).findFirst().orElse(vehicles.get(0));
            
            Trip t = new Trip();
            t.setVehicleId(selectedVehicle.getId());
            t.setRouteId(selectedVehicle.getRouteId());
            t.setTripDate(Date.valueOf(request.getParameter("tripDate")));
            t.setTripType(request.getParameter("tripType")); // MORNING hoặc AFTERNOON
            t.setNotes(request.getParameter("notes"));
            t.setCreatedBy(user.getId());

            int tripId = tripDAO.insert(t);
            if (tripId > 0) {
                // Lấy danh sách HS đăng ký tuyến này vào ngày đó
                List<Student> students = studentDAO.getByRouteAndDate(selectedVehicle.getRouteId(), t.getTripDate());
                List<Integer> ids = students.stream().map(Student::getId).toList();

                // Khởi tạo điểm danh ABSENT cho tất cả HS
                attDAO.initForTrip(tripId, ids);

                // Tự động NOTIFIED_ABSENT cho HS đã báo nghỉ hôm nay
                for (Student s : students) {
                    if (anDAO.isAbsentToday(s.getId())) {
                        attDAO.markNotifiedAbsent(tripId, s.getId());
                    }
                }

                response.sendRedirect(request.getContextPath() + "/monitor/trip?tripId=" + tripId + "&msg=created");
            } else {
                // Tạo chuyến thất bại (có thể do lỗi DB)
                response.sendRedirect(request.getContextPath() + "/monitor/trip?msg=createError");
            }

        // ── Cập nhật ghi chú chuyến ──────────────────────────────────────────
        } else if ("updateNotes".equals(action)) {
            int tripId = parseIntSafe(request.getParameter("tripId"), -1);
            if (tripId > 0) {
                tripDAO.updateNotes(tripId, request.getParameter("notes"));
                response.sendRedirect(request.getContextPath() + "/monitor/trip?tripId=" + tripId + "&msg=updated");
            }
        }
    }

    /**
     * Chuyển đổi chuỗi thành int an toàn, trả về defaultValue nếu chuỗi không hợp lệ.
     * Tái sử dụng để tránh NumberFormatException khi parse tham số URL.
     *
     * @param s            Chuỗi cần chuyển đổi
     * @param defaultValue Giá trị mặc định nếu parse thất bại
     */
    private int parseIntSafe(String s, int defaultValue) {
        if (s == null || s.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
