package servlet.auth;

import dal.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.User;
import util.PasswordUtil;
import java.io.IOException;

/**
 * RegisterServlet – Xử lý đăng ký tài khoản phụ huynh mới.
 *
 * URL: /register
 *
 * GET /register → Hiển thị form đăng ký
 * POST /register → Xử lý dữ liệu đăng ký, tạo tài khoản PARENT
 *
 * Luồng đăng ký:
 * Phụ huynh điền form (username, mật khẩu, họ tên, email, sdt)
 * → POST → RegisterServlet → Kiểm tra hợp lệ → Lưu DB → Redirect login
 *
 * Các kiểm tra:
 * 1. Tất cả trường bắt buộc không được để trống
 * 2. Mật khẩu và xác nhận mật khẩu phải khớp
 * 3. Mật khẩu phải đủ dài (tối thiểu 6 ký tự)
 * 4. Username chưa tồn tại trong hệ thống
 * 5. Email chưa được dùng bởi tài khoản khác
 *
 * Sau khi đăng ký thành công → redirect đến /login với thông báo thành công.
 *
 * ⚠️ Lưu ý: Chức năng này chỉ tạo tài khoản PARENT. Các role khác
 * (ADMIN, DRIVER, MONITOR) phải được Admin tạo thủ công.
 */
public class RegisterServlet extends HttpServlet {

    /** Độ dài tối thiểu của mật khẩu */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * GET: Hiển thị trang đăng ký.
     * Nếu user đã đăng nhập → redirect đến dashboard (không cần đăng ký nữa).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Nếu đã đăng nhập → không cần vào trang đăng ký
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedUser") != null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    /**
     * POST: Xử lý form đăng ký.
     * Thực hiện validation đầy đủ trước khi lưu vào DB.
     * Nếu lỗi → forward lại register.jsp kèm thông báo lỗi và giữ lại dữ liệu đã
     * nhập.
     * Nếu thành công → redirect đến trang login.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Lấy dữ liệu từ form
        String username = trimOrEmpty(request.getParameter("username"));
        String password = trimOrEmpty(request.getParameter("password"));
        String confirmPassword = trimOrEmpty(request.getParameter("confirmPassword"));
        String fullName = trimOrEmpty(request.getParameter("fullName"));
        String email = trimOrEmpty(request.getParameter("email"));
        String phone = trimOrEmpty(request.getParameter("phone"));

        // ── Validation ────────────────────────────────────────────────────
        String errorMessage = validateRegistrationInput(username, password, confirmPassword,
                fullName, email, phone);
        if (errorMessage != null) {
            // Lỗi validation → trả về form với thông báo lỗi, giữ lại dữ liệu đã nhập
            request.setAttribute("error", errorMessage);
            request.setAttribute("username", username);
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // ── Kiểm tra trùng lặp trong DB ───────────────────────────────────
        UserDAO userDAO = new UserDAO();

        if (userDAO.usernameExists(username)) {
            request.setAttribute("error", "Tên đăng nhập đã được sử dụng. Vui lòng chọn tên khác!");
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (userDAO.emailExists(email)) {
            request.setAttribute("error", "Email này đã được đăng ký. Vui lòng dùng email khác hoặc đăng nhập!");
            request.setAttribute("username", username);
            request.setAttribute("fullName", fullName);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // ── Tạo tài khoản mới ─────────────────────────────────────────────
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(PasswordUtil.sha1(password)); // Hash mật khẩu SHA-1 trước khi lưu
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPhone(phone);

        boolean registerSuccess = userDAO.registerParent(newUser);

        if (registerSuccess) {
            // Đăng ký thành công → redirect đến login với thông báo
            response.sendRedirect(request.getContextPath() + "/login?registered=true");
        } else {
            // Lỗi khi lưu DB (hiếm gặp)
            request.setAttribute("error", "Đăng ký thất bại do lỗi hệ thống. Vui lòng thử lại sau!");
            request.setAttribute("username", username);
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    /**
     * Kiểm tra toàn bộ dữ liệu form đăng ký.
     *
     * @return null nếu hợp lệ, chuỗi thông báo lỗi nếu không hợp lệ
     */
    private String validateRegistrationInput(String username, String password,
            String confirmPassword, String fullName,
            String email, String phone) {
        // Kiểm tra trường bắt buộc không rỗng
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || fullName.isEmpty() || email.isEmpty()) {
            return "Vui lòng điền đầy đủ tất cả các trường bắt buộc!";
        }

        // Kiểm tra độ dài mật khẩu
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự!";
        }

        // Kiểm tra mật khẩu khớp
        if (!password.equals(confirmPassword)) {
            return "Mật khẩu và xác nhận mật khẩu không khớp!";
        }

        // Kiểm tra định dạng email cơ bản
        if (!email.contains("@") || !email.contains(".")) {
            return "Địa chỉ email không hợp lệ!";
        }

        // Kiểm tra độ dài username
        if (username.length() < 4 || username.length() > 50) {
            return "Tên đăng nhập phải từ 4 đến 50 ký tự!";
        }

        return null; // Không có lỗi
    }

    /**
     * Lấy giá trị parameter, trim khoảng trắng, trả về chuỗi rỗng nếu null.
     * Dùng để tránh NullPointerException và xử lý khoảng trắng thừa.
     */
    private String trimOrEmpty(String value) {
        return (value != null) ? value.trim() : "";
    }
}
