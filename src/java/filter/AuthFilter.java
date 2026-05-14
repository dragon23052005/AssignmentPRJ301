package filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.User;
import java.io.IOException;

/**
 * AuthFilter – Bộ lọc bảo mật (Security Filter) chạy trước mọi request.
 *
 * Hai nhiệm vụ chính:
 *   1. Xác thực (Authentication): Kiểm tra xem người dùng đã đăng nhập chưa.
 *      Nếu chưa → redirect về trang /login.
 *   2. Phân quyền (Authorization): Kiểm tra quyền truy cập theo role.
 *      Ví dụ: PARENT không được vào /admin/...
 *
 * Filter này được cấu hình trong web.xml để áp dụng cho tất cả URL
 * (ngoại trừ /login và /logout đã được loại trừ trong web.xml).
 *
 * Luồng hoạt động:
 *   Request → AuthFilter → Servlet → JSP View
 */
// Hùng mạnh 799 ăn cứt
// Hùng mạnh 799
public class AuthFilter implements Filter {

    // Các prefix đường dẫn tương ứng với từng role
    private static final String PATH_ADMIN   = "/admin/";
    private static final String PATH_PARENT  = "/parent/";
    private static final String PATH_MONITOR = "/monitor/";
    private static final String PATH_DRIVER  = "/driver/";

    // Tên attribute lưu thông tin user trong session
    private static final String SESSION_USER = "loggedUser";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // ── Bước 1: Kiểm tra đăng nhập ──────────────────────────────────────
        // getSession(false) → KHÔNG tạo session mới nếu chưa có (tiết kiệm tài nguyên)
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute(SESSION_USER) : null;

        if (user == null) {
            // Chưa đăng nhập → chuyển về trang login
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // ── Bước 2: Kiểm tra phân quyền theo URL prefix ─────────────────────
        String path = request.getServletPath();
        String role = user.getRole();

        // Kiểm tra từng nhóm URL có yêu cầu role phù hợp không
        if (isUnauthorized(path, PATH_ADMIN,   "ADMIN",   role) ||
            isUnauthorized(path, PATH_PARENT,  "PARENT",  role) ||
            isUnauthorized(path, PATH_MONITOR, "MONITOR", role) ||
            isUnauthorized(path, PATH_DRIVER,  "DRIVER",  role)) {

            // Truy cập không đúng quyền → trang thông báo lỗi
            response.sendRedirect(request.getContextPath() + "/access-denied.jsp");
            return;
        }

        // ── Bước 3: Cho phép request đi tiếp đến Servlet/JSP ────────────────
        chain.doFilter(req, res);
    }

    /**
     * Kết hợp kiểm tra prefix URL và quyền role.
     * Tái sử dụng để tránh lặp code if-else.
     *
     * @param path         Đường dẫn request hiện tại (vd: /admin/users)
     * @param pathPrefix   Prefix cần kiểm tra (vd: /admin/)
     * @param requiredRole Role được phép truy cập (vd: "ADMIN")
     * @param userRole     Role thực tế của user đang đăng nhập
     * @return true nếu URL thuộc nhóm bị hạn chế nhưng user không có quyền
     */
    private boolean isUnauthorized(String path, String pathPrefix,
                                   String requiredRole, String userRole) {
        return path.startsWith(pathPrefix) && !requiredRole.equals(userRole);
    }
}
