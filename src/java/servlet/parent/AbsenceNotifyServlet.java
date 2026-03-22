package servlet.parent;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.User;
import java.io.IOException;
import java.sql.Date;

/**
 * AbsenceNotifyServlet – Phụ huynh báo nghỉ cho con.
 *
 * URL: /parent/absence
 *
 * GET  /parent/absence → Trang xem + form báo nghỉ
 * POST /parent/absence → Gửi thông báo nghỉ
 *
 * Luồng báo nghỉ:
 *   Phụ huynh chọn con + ngày nghỉ + lý do → Submit →
 *   AbsenceNotificationDAO.notify() lưu vào DB →
 *   Monitor sẽ thấy thông báo khi vào dashboard →
 *   Khi tạo chuyến, HS báo nghỉ tự động được đánh NOTIFIED_ABSENT
 *
 * Lưu ý: Phụ huynh có thể báo nghỉ trước (không nhất thiết phải là hôm nay).
 * Nếu đã báo nghỉ cùng ngày trước đó → cập nhật lý do (UPSERT trong DAO).
 */
public class AbsenceNotifyServlet extends HttpServlet {

    /**
     * GET: Hiển thị danh sách con em và lịch sử thông báo nghỉ.
     * Cũng hiển thị form báo nghỉ mới.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        AbsenceNotificationDAO anDAO  = new AbsenceNotificationDAO();
        StudentDAO             studentDAO = new StudentDAO();
        User user = (User) request.getSession().getAttribute("loggedUser");

        // Danh sách con em để phụ huynh chọn khi báo nghỉ
        request.setAttribute("students",      studentDAO.getByParentId(user.getId()));
        // Lịch sử các thông báo nghỉ đã gửi
        request.setAttribute("notifications", anDAO.getByParent(user.getId()));
        request.setAttribute("msg",           request.getParameter("msg")); // Thông báo sau redirect
        request.getRequestDispatcher("/WEB-INF/views/parent/absence.jsp").forward(request, response);
    }

    /**
     * POST: Xử lý form báo nghỉ.
     *
     * ⚠️ Lưu ý bảo mật: Không kiểm tra xem studentId có thuộc về phụ huynh này không.
     *    Phụ huynh A có thể báo nghỉ cho con của phụ huynh B nếu biết studentId.
     *    Nên thêm validation: Kiểm tra student.parentId == user.getId().
     *
     * @param req Tham số từ form: studentId, absenceDate (yyyy-MM-dd), reason
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        AbsenceNotificationDAO anDAO = new AbsenceNotificationDAO();

        int    studentId   = Integer.parseInt(request.getParameter("studentId"));
        String dateStr     = request.getParameter("absenceDate");
        String reason      = request.getParameter("reason");
        Date   absenceDate = Date.valueOf(dateStr); // Chuyển "yyyy-MM-dd" → java.sql.Date

        boolean ok = anDAO.notify(studentId, absenceDate, reason);
        // Redirect với thông báo kết quả (PRG pattern)
        response.sendRedirect(request.getContextPath() + "/parent/absence?msg=" + (ok ? "sent" : "error"));
    }
}
