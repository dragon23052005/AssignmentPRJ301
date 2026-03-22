package servlet.admin;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.Student;
import java.io.IOException;
import java.sql.Date;

/**
 * StudentManagementServlet – Quản lý học sinh (Admin).
 *
 * URL: /admin/students
 *
 * GET  /admin/students           → Danh sách học sinh + dropdown phụ huynh & tuyến
 * POST /admin/students action=add  → Thêm học sinh mới (kèm đăng ký tuyến nếu có)
 * POST /admin/students action=edit → Cập nhật học sinh (kèm cập nhật đăng ký tuyến)
 *
 * Logic đặc biệt:
 *   Khi thêm/sửa học sinh, nếu Admin cũng chọn routeId + stopId →
 *   tự động đăng ký xe buýt cho học sinh đó (gọi RegistrationDAO.register()).
 *   RegistrationDAO sẽ tự hủy đăng ký cũ và tạo mới trong 1 transaction.
 */
public class StudentManagementServlet extends HttpServlet {

    /**
     * GET: Hiển thị danh sách học sinh kèm dropdown phụ huynh và tuyến xe.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if ("stops".equals(action)) {
            int routeId = Integer.parseInt(request.getParameter("routeId"));
            StopDAO stopDAO = new StopDAO();
            var stops = stopDAO.getByRoute(routeId);
            response.setContentType("application/json;charset=UTF-8");
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < stops.size(); i++) {
                var s = stops.get(i);
                if (i > 0) sb.append(",");
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

        StudentDAO studentDAO = new StudentDAO();
        UserDAO    userDAO    = new UserDAO();
        RouteDAO   routeDAO   = new RouteDAO();

        request.setAttribute("students", studentDAO.getAll());
        request.setAttribute("parents",  userDAO.getByRole("PARENT")); // Dropdown chọn phụ huynh
        request.setAttribute("routes",   routeDAO.getActive());          // Dropdown chọn tuyến
        request.setAttribute("msg",      request.getParameter("msg"));
        request.getRequestDispatcher("/WEB-INF/views/admin/students.jsp").forward(request, response);
    }

    /**
     * POST: Thêm học sinh mới hoặc cập nhật học sinh.
     *
     * Tương đồng giữa add và edit:
     *   - Đều parse Student từ form
     *   - Đều kiểm tra và đăng ký tuyến nếu có routeId + stopId
     *
     * Sự khác biệt:
     *   - add: Gọi studentDAO.insert() → lấy ID mới → đăng ký tuyến
     *   - edit: Gọi studentDAO.update() với ID đã biết → cập nhật đăng ký tuyến
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        StudentDAO      studentDAO = new StudentDAO();
        RegistrationDAO regDAO     = new RegistrationDAO();
        String action = request.getParameter("action");

        if ("add".equals(action)) {
            // ── Thêm học sinh mới ────────────────────────────────────────────
            Student s = new Student();
            s.setFullName(request.getParameter("fullName"));
            s.setStudentCode(request.getParameter("studentCode"));
            s.setClassName(request.getParameter("className"));
            s.setDateOfBirth(parseDateSafe(request.getParameter("dateOfBirth")));
            s.setParentId(Integer.parseInt(request.getParameter("parentId")));
            studentDAO.insert(s); // insert() tự gán ID mới vào s

            // Nếu Admin chọn cả tuyến và điểm dừng → đăng ký xe buýt luôn
            registerBusIfSelected(request, regDAO, s.getId());
            response.sendRedirect(request.getContextPath() + "/admin/students?msg=added");

        } else if ("edit".equals(action)) {
            // ── Cập nhật học sinh ────────────────────────────────────────────
            Student s = new Student();
            s.setId(Integer.parseInt(request.getParameter("id")));
            s.setFullName(request.getParameter("fullName"));
            s.setStudentCode(request.getParameter("studentCode"));
            s.setClassName(request.getParameter("className"));
            s.setDateOfBirth(parseDateSafe(request.getParameter("dateOfBirth")));
            studentDAO.update(s);

            // Cập nhật đăng ký xe buýt nếu có thay đổi tuyến/điểm dừng
            registerBusIfSelected(request, regDAO, s.getId());
            response.sendRedirect(request.getContextPath() + "/admin/students?msg=updated");
        }
    }

    /**
     * Đăng ký xe buýt cho học sinh nếu form có routeId và stopId hợp lệ.
     * Tách thành method riêng để tái sử dụng giữa action add và edit.
     *
     * RegistrationDAO.register() sẽ tự hủy đăng ký cũ trong transaction
     * trước khi tạo đăng ký mới → không cần lo duplicated registration.
     *
     * @param req       HttpServletRequest chứa routeId và stopId từ form
     * @param regDAO    DAO để thực hiện đăng ký
     * @param studentId ID học sinh cần đăng ký
     */
    private void registerBusIfSelected(HttpServletRequest request,
                                       RegistrationDAO regDAO,
                                       int studentId) {
        String rid  = request.getParameter("routeId");
        String sid  = request.getParameter("stopId");
        // Chỉ đăng ký khi cả hai trường được chọn và không rỗng
        if (rid != null && !rid.isEmpty() && sid != null && !sid.isEmpty()) {
            regDAO.register(
                studentId,
                Integer.parseInt(rid),
                Integer.parseInt(sid),
                Date.valueOf(java.time.LocalDate.now().toString())
            );
        }
    }

    /**
     * Parse ngày sinh an toàn. Trả về null nếu chuỗi rỗng hoặc null.
     * Tránh IllegalArgumentException khi Date.valueOf(null/empty).
     *
     * @param dateStr Chuỗi ngày dạng "yyyy-MM-dd"
     * @return java.sql.Date hoặc null
     */
    private Date parseDateSafe(String dateStr) {
        return (dateStr != null && !dateStr.isEmpty()) ? Date.valueOf(dateStr) : null;
    }

    private String escapeJson(String s) {
        return s.replace("\"", "\\\"");
    }
}
