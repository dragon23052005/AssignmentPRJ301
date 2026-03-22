package servlet.parent;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.User;
import java.io.IOException;
import java.sql.Date;

/**
 * RegisterBusServlet – Đăng ký xe buýt cho học sinh (Phụ huynh).
 *
 * URL: /parent/register
 *
 * GET  /parent/register                         → Trang đăng ký + hủy đăng ký
 * GET  /parent/register?action=stops&routeId=X  → Trả JSON danh sách điểm dừng (AJAX)
 * GET  /parent/register?action=cancel&studentId=X → Hủy đăng ký của học sinh
 * POST /parent/register                         → Đăng ký tuyến + điểm dừng mới
 *
 * AJAX endpoint cho tính năng "chọn tuyến → load danh sách điểm dừng tương ứng":
 *   - action=stops trả về JSON array để JavaScript điền vào dropdown điểm dừng
 *   - Không dùng thư viện JSON → tự build chuỗi JSON thủ công
 *
 * ⚠️ Lưu ý bảo mật (tương tự AbsenceNotifyServlet):
 *    Khi đăng ký POST, không kiểm tra studentId có thuộc về phụ huynh này.
 *    Phụ huynh A có thể đăng ký xe cho con của phụ huynh B.
 */
public class RegisterBusServlet extends HttpServlet {

    /**
     * GET: Hiển thị trang đăng ký hoặc xử lý action nhanh (stops, cancel).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        RegistrationDAO regDAO    = new RegistrationDAO();
        StudentDAO      studentDAO = new StudentDAO();
        RouteDAO        routeDAO  = new RouteDAO();
        StopDAO         stopDAO   = new StopDAO();

        User user = (User) request.getSession().getAttribute("loggedUser");
        String action = request.getParameter("action");

        // ── AJAX: Trả danh sách điểm dừng theo tuyến (JSON) ─────────────────
        if ("stops".equals(action)) {
            int routeId = Integer.parseInt(request.getParameter("routeId"));
            var stops = stopDAO.getByRoute(routeId);

            // Build JSON thủ công (không dùng thư viện để tránh dependency thêm)
            response.setContentType("application/json;charset=UTF-8");
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < stops.size(); i++) {
                var s = stops.get(i);
                if (i > 0) sb.append(",");
                // Escape dấu nháy kép trong tên điểm dừng và địa chỉ để tránh JSON invalid
                sb.append("{\"id\":").append(s.getId())
                  .append(",\"stopName\":\"").append(escapeJson(s.getStopName()))
                  .append("\",\"address\":\"")
                  .append(s.getAddress() != null ? escapeJson(s.getAddress()) : "")
                  .append("\"}");
            }
            sb.append("]");
            response.getWriter().write(sb.toString());
            return;
        }

        // ── Hủy đăng ký xe buýt của học sinh ────────────────────────────────
        if ("cancel".equals(action)) {
            int studentId = Integer.parseInt(request.getParameter("studentId"));
            // ⚠️ Nên kiểm tra: studentId có phải con của user.getId() không?
            regDAO.cancel(studentId);
            response.sendRedirect(request.getContextPath() + "/parent/register?msg=cancelled");
            return;
        }

        // ── Hiển thị trang đăng ký chính ────────────────────────────────────
        request.setAttribute("students", studentDAO.getByParentId(user.getId()));
        request.setAttribute("routes",   routeDAO.getActive()); // Chỉ tuyến đang hoạt động
        request.setAttribute("msg",      request.getParameter("msg"));
        request.getRequestDispatcher("/WEB-INF/views/parent/register.jsp").forward(request, response);
    }

    /**
     * POST: Phụ huynh đăng ký tuyến xe buýt cho con.
     *
     * RegistrationDAO.register() sẽ tự động:
     *   1. Hủy đăng ký cũ (nếu có)
     *   2. Tạo đăng ký mới
     * Toàn bộ trong 1 transaction → không bao giờ mất dữ liệu.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        RegistrationDAO regDAO = new RegistrationDAO();

        int  studentId = Integer.parseInt(request.getParameter("studentId"));
        int  routeId   = Integer.parseInt(request.getParameter("routeId"));
        int  stopId    = Integer.parseInt(request.getParameter("stopId"));
        Date startDate = Date.valueOf(request.getParameter("startDate")); // "yyyy-MM-dd"

        boolean ok = regDAO.register(studentId, routeId, stopId, startDate);
        response.sendRedirect(request.getContextPath() + "/parent/register?msg=" + (ok ? "registered" : "error"));
    }

    /**
     * Escape ký tự đặc biệt trong chuỗi để đảm bảo JSON hợp lệ.
     * Chỉ xử lý dấu nháy kép vì phổ biến nhất trong tên địa điểm tiếng Việt.
     *
     * Nếu cần xử lý đầy đủ hơn (newline, tab, backslash), nên dùng thư viện
     * như Gson hoặc Jackson thay vì tự build JSON.
     *
     * @param s Chuỗi đầu vào
     * @return Chuỗi đã escape dấu nháy kép
     */
    private String escapeJson(String s) {
        return s.replace("\"", "\\\"");
    }
}
