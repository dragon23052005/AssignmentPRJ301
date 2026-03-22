package servlet.driver;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.*;
import java.io.IOException;
import java.sql.Date;

/**
 * VehicleReportServlet – Quản lý báo cáo sự cố xe của Lái xe.
 *
 * URL: /driver/report
 *
 * GET  /driver/report → Xem danh sách báo cáo sự cố xe của mình
 * POST /driver/report action=add → Gửi báo cáo sự cố mới
 *
 * Luồng báo cáo sự cố:
 *   1. Lái xe phát hiện vấn đề với xe
 *   2. Điền form: loại sự cố, mức độ, mô tả
 *   3. Submit → tạo record với status=OPEN → Admin/Monitor xử lý
 *
 * Xe được xác định từ session user → getByDriverId()
 * (không cần lái xe nhập ID xe → tránh nhầm lẫn hoặc giả mạo).
 */
public class VehicleReportServlet extends HttpServlet {

    /**
     * GET: Hiển thị trang báo cáo sự cố với danh sách các báo cáo đã gửi.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        VehicleDAO       vehicleDAO = new VehicleDAO();
        VehicleReportDAO vrDAO      = new VehicleReportDAO();

        User user = (User) request.getSession().getAttribute("loggedUser");

        // Tìm xe của lái xe hiện tại
        Vehicle vehicle = vehicleDAO.getByDriverId(user.getId());
        request.setAttribute("vehicle", vehicle);

        if (vehicle != null) {
            // Load lịch sử báo cáo của xe này
            request.setAttribute("reports", vrDAO.getByVehicle(vehicle.getId()));
        }

        request.setAttribute("msg", request.getParameter("msg")); // Thông báo sau redirect
        request.getRequestDispatcher("/WEB-INF/views/driver/report.jsp").forward(request, response);
    }

    /**
     * POST: Xử lý gửi báo cáo sự cố mới.
     * action=add → Tạo báo cáo mới với ngày báo cáo = hôm nay.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        VehicleDAO       vehicleDAO = new VehicleDAO();
        VehicleReportDAO vrDAO      = new VehicleReportDAO();

        User user = (User) request.getSession().getAttribute("loggedUser");
        String action = request.getParameter("action");

        if ("add".equals(action)) {
            // Xác định xe của lái xe → bắt buộc phải có xe mới báo cáo được
            Vehicle vehicle = vehicleDAO.getByDriverId(user.getId());
            if (vehicle == null) {
                // Lái xe chưa được phân công xe → không thể báo cáo
                response.sendRedirect(request.getContextPath() + "/driver/report?msg=noVehicle");
                return;
            }

            // Tạo đối tượng báo cáo từ form data
            VehicleReport r = new VehicleReport();
            r.setVehicleId(vehicle.getId());
            r.setReportedBy(user.getId());
            r.setReportDate(Date.valueOf(java.time.LocalDate.now())); // Ngày báo cáo = hôm nay
            r.setIssueType(request.getParameter("issueType"));   // MECHANICAL, TIRE, BRAKE, ...
            r.setSeverity(request.getParameter("severity"));     // LOW, MEDIUM, HIGH, CRITICAL
            r.setDescription(request.getParameter("description"));

            boolean ok = vrDAO.insert(r);
            // Redirect với thông báo kết quả để tránh resubmit
            response.sendRedirect(request.getContextPath() + "/driver/report?msg=" + (ok ? "reported" : "error"));
        }
    }
}
