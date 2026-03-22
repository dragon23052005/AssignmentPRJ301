package servlet.auth;

import dal.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.User;
import util.PasswordUtil;
import java.io.IOException;

/**
 * LoginServlet – Xử lý đăng nhập và điều hướng sau khi đăng nhập.
 *
 * GET /login → Hiển thị trang login (nếu đã đăng nhập → redirect đến dashboard)
 * POST /login → Xử lý form đăng nhập
 *
 * Luồng đăng nhập:
 * Người dùng nhập username + password → POST → LoginServlet →
 * Kiểm tra DB (UserDAO.login) → Tạo session → Redirect theo role
 *
 * Tính năng Remember Me:
 * Khi chọn "Ghi nhớ đăng nhập", hệ thống lưu cookie "rememberUser"
 * với thời hạn 30 ngày. Lần sau truy cập, username sẽ được điền sẵn.
 *
 * Session lưu đối tượng User với key "loggedUser", hết hạn sau 30 phút.
 * AuthFilter sử dụng "loggedUser" để kiểm tra xác thực.
 */
public class LoginServlet extends HttpServlet {

    /** Tên cookie dùng cho chức năng Remember Me */
    private static final String COOKIE_REMEMBER = "rememberUser";
    private static final String COOKIE_REMEMBER_PWD = "rememberPassword";

    /** Thời gian sống của cookie Remember Me: 30 ngày (tính bằng giây) */
    private static final int COOKIE_MAX_AGE_SECONDS = 30 * 24 * 60 * 60;

    /**
     * GET: Nếu đã đăng nhập → redirect thẳng đến dashboard tương ứng.
     * Nếu chưa đăng nhập nhưng có cookie Remember Me → điền sẵn username.
     * Tránh trường hợp user đã login nhưng vẫn thấy trang login.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Kiểm tra session hiện tại (false = không tạo mới nếu chưa có)
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedUser") != null) {
            // Đã đăng nhập → redirect ngay đến dashboard
            redirectByRole((User) session.getAttribute("loggedUser"), request, response);
            return;
        }

        // Kiểm tra cookie Remember Me để điền sẵn username
        String rememberedUsername = getRememberedUsername(request);
        String rememberedPassword = getRememberedPassword(request);
        if (rememberedUsername != null) {
            request.setAttribute("rememberedUsername", rememberedUsername);
        }
        if (rememberedPassword != null) {
            request.setAttribute("rememberedPassword", rememberedPassword);
        }

        // Chưa đăng nhập → hiển thị trang đăng nhập
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    /**
     * POST: Xử lý dữ liệu form đăng nhập.
     * Nếu sai thông tin → forward lại login.jsp với thông báo lỗi.
     * Nếu đúng → tạo session + xử lý Remember Me + redirect đến dashboard.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); // Để nhận đúng ký tự tiếng Việt

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe"); // checkbox: "on" hoặc null

        // Hash mật khẩu SHA-1 trước khi so sánh với DB
        UserDAO userDAO = new UserDAO();
        User user = userDAO.login(username, PasswordUtil.sha1(password));

        if (user == null) {
            // Đăng nhập thất bại → hiển thị lỗi trên trang login
            request.setAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
            request.setAttribute("rememberedUsername", username); // Giữ lại username đã nhập
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        // Đăng nhập thành công → tạo session mới (true = tạo nếu chưa có)
        HttpSession session = request.getSession(true);
        session.setAttribute("loggedUser", user);
        session.setMaxInactiveInterval(30 * 60); // Session hết hạn sau 30 phút không hoạt động

        // Xử lý Remember Me: lưu hoặc xóa cookie tùy theo lựa chọn của người dùng
        handleRememberMe(response, username, password, "on".equals(rememberMe));

        // Redirect đến dashboard phù hợp theo role
        redirectByRole(user, request, response);
    }

    /**
     * Điều hướng người dùng đến dashboard tương ứng với vai trò.
     * Được tái sử dụng ở cả doGet() (khi đã login) và doPost() (sau khi login).
     *
     * Nếu role không khớp với bất kỳ case nào → redirect về /login (fallback an
     * toàn).
     */
    private void redirectByRole(User user, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String contextPath = request.getContextPath(); // Ví dụ: "/ProjectGroup4"
        response.sendRedirect(switch (user.getRole()) {
            case "ADMIN" -> contextPath + "/admin/dashboard";
            case "PARENT" -> contextPath + "/parent/dashboard";
            case "MONITOR" -> contextPath + "/monitor/dashboard";
            case "DRIVER" -> contextPath + "/driver/dashboard";
            default -> contextPath + "/login"; // Role không xác định → login lại
        });
    }

    /**
     * Xử lý logic Remember Me:
     * - rememberEnabled = true → tạo cookie lưu username và password trong 30 ngày
     * - rememberEnabled = false → xóa cookie nếu đang tồn tại
     *
     * @param response        HttpServletResponse để gắn cookie
     * @param username        Tên đăng nhập cần lưu vào cookie
     * @param password        Mật khẩu cần lưu vào cookie
     * @param rememberEnabled Người dùng có chọn "Ghi nhớ đăng nhập" không
     */
    private void handleRememberMe(HttpServletResponse response, String username, String password,
            boolean rememberEnabled) {
        Cookie cookieUser = new Cookie(COOKIE_REMEMBER, rememberEnabled ? username : "");
        cookieUser.setMaxAge(rememberEnabled ? COOKIE_MAX_AGE_SECONDS : 0); // 0 = xóa cookie
        cookieUser.setPath("/"); // Cookie có hiệu lực trên toàn bộ ứng dụng
        cookieUser.setHttpOnly(true); // Ngăn JavaScript đọc cookie (bảo mật XSS)
        response.addCookie(cookieUser);

        Cookie cookiePwd = new Cookie(COOKIE_REMEMBER_PWD, rememberEnabled ? password : "");
        cookiePwd.setMaxAge(rememberEnabled ? COOKIE_MAX_AGE_SECONDS : 0); // 0 = xóa cookie
        cookiePwd.setPath("/"); // Cookie có hiệu lực trên toàn bộ ứng dụng
        cookiePwd.setHttpOnly(true); // Ngăn JavaScript đọc cookie (bảo mật XSS)
        response.addCookie(cookiePwd);
    }

    /**
     * Tìm kiếm cookie Remember Me trong danh sách cookie của request.
     *
     * @param request HttpServletRequest chứa cookie
     * @return Giá trị username đã lưu, hoặc null nếu không tìm thấy cookie
     */
    private String getRememberedUsername(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;
        for (Cookie cookie : cookies) {
            if (COOKIE_REMEMBER.equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * Tìm kiếm cookie Remember Password trong danh sách cookie của request.
     */
    private String getRememberedPassword(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;
        for (Cookie cookie : cookies) {
            if (COOKIE_REMEMBER_PWD.equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
