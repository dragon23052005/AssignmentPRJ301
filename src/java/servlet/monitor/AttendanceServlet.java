package servlet.monitor;

import dal.AttendanceDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.User;
import java.io.IOException;

/**
 * AttendanceServlet – API điểm danh học sinh (AJAX endpoint).
 *
 * URL: POST /monitor/attendance
 *
 * Đây là endpoint JSON, không forward đến JSP.
 * JavaScript trên trang trip.jsp gọi endpoint này qua fetch/AJAX.
 *
 * Tham số từ form:
 *   - action   : "board" | "alight" | "absent"
 *   - tripId   : ID chuyến xe
 *   - studentId: ID học sinh
 *   - stopId   : ID điểm dừng (tùy chọn, có thể null/rỗng)
 *
 * Response: {"success": true/false}
 *
 * Luồng hoạt động:
 *   Monitor bấm nút trên bảng điểm danh →
 *   JavaScript gửi POST request →
 *   AttendanceServlet gọi DAO phù hợp →
 *   Trả về JSON {"success": true} →
 *   JavaScript cập nhật UI mà không reload trang
 */
public class AttendanceServlet extends HttpServlet {

    /**
     * POST: Thực hiện cập nhật trạng thái điểm danh và trả về JSON.
     *
     * ⚠️ Không có GET handler vì đây là API endpoint thuần POST.
     * Nếu truy cập bằng GET → mặc định trả về 405 Method Not Allowed.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        AttendanceDAO attDAO = new AttendanceDAO();
        User user = (User) request.getSession().getAttribute("loggedUser");

        String action  = request.getParameter("action");
        int tripId     = Integer.parseInt(request.getParameter("tripId"));
        int studentId  = Integer.parseInt(request.getParameter("studentId"));

        // stopId là tùy chọn; nếu không có → truyền 0 (DAO sẽ lưu NULL vào DB)
        String stopIdStr = request.getParameter("stopId");
        int stopId = (stopIdStr != null && !stopIdStr.isEmpty())
                   ? Integer.parseInt(stopIdStr) : 0;

        // Thực hiện action tương ứng theo switch expression (Java 14+)
        boolean ok = switch (action) {
            case "board"  -> attDAO.markBoarded(tripId, studentId, stopId, user.getId());
            case "alight" -> attDAO.markAlighted(tripId, studentId, stopId, user.getId());
            case "absent" -> attDAO.markAbsent(tripId, studentId, user.getId());
            default       -> false; // Action không hợp lệ → luôn false
        };

        // Trả về JSON response cho JavaScript
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":" + ok + "}");
    }
}
