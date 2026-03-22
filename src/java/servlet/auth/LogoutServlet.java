package servlet.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * LogoutServlet – Xử lý đăng xuất.
 *
 * GET /logout → Hủy session hiện tại, xóa cookie Remember Me và redirect về
 * trang login.
 *
 * Dùng session.invalidate() để xóa toàn bộ dữ liệu session,
 * bao gồm thông tin user đã đăng nhập ("loggedUser").
 *
 * ⚠️ Lưu ý bảo mật: Sau invalidate(), các request kế tiếp
 * sẽ bị AuthFilter chặn và yêu cầu đăng nhập lại.
 * Cookie Remember Me cũng bị xóa khi đăng xuất.
 */
public class LogoutServlet extends HttpServlet {

    /** Tên cookie Remember Me - phải khớp với LoginServlet */
    private static final String COOKIE_REMEMBER = "rememberUser";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Lấy session hiện tại (false = không tạo mới nếu không có)
        HttpSession session = request.getSession(false);

        // Hủy session nếu tồn tại → xóa sạch thông tin đăng nhập
        if (session != null) {
            session.invalidate();
        }

        // Xóa cookie Remember Me khi đăng xuất
        // Đặt Max-Age = 0 để trình duyệt xóa cookie ngay lập tức
        Cookie rememberCookie = new Cookie(COOKIE_REMEMBER, "");
        rememberCookie.setMaxAge(0);
        rememberCookie.setPath("/");
        rememberCookie.setHttpOnly(true);
        response.addCookie(rememberCookie);

        // Redirect về trang đăng nhập
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
