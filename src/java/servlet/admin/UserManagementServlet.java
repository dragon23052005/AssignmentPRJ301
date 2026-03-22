package servlet.admin;

import dal.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import model.User;
import util.PasswordUtil;
import java.io.IOException;

/**
 * UserManagementServlet – Quản lý tài khoản người dùng (Admin).
 *
 * URL: /admin/users
 *
 * GET /admin/users → Danh sách tất cả user
 * GET /admin/users?action=edit&id=X → Load form sửa user
 * GET /admin/users?action=toggle&id=X → Bật/tắt tài khoản (redirect)
 * GET /admin/users?action=resetpwd&id=X → Reset mật khẩu về mặc định (redirect)
 * POST /admin/users action=add → Thêm user mới
 * POST /admin/users action=edit → Cập nhật thông tin user
 *
 * Mật khẩu mặc định khi tạo mới hoặc reset: "Admin@123" (đã hash SHA-1)
 */
public class UserManagementServlet extends HttpServlet {

    /**
     * GET: Xử lý xem danh sách và các action nhanh (toggle, resetpwd).
     *
     * Các action qua GET thực hiện rồi redirect (Pattern: PRG - Post/Redirect/Get)
     * để tránh resubmit khi người dùng reload trang.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserDAO userDAO = new UserDAO();
        String action = request.getParameter("action");

        if ("edit".equals(action)) {
            // Load thông tin user cần sửa vào form
            int id = Integer.parseInt(request.getParameter("id"));
            request.setAttribute("editUser", userDAO.getById(id));

        } else if ("toggle".equals(action)) {
            // Đảo trạng thái active của user (1→0 hoặc 0→1)
            userDAO.toggleActive(Integer.parseInt(request.getParameter("id")));
            response.sendRedirect(request.getContextPath() + "/admin/users?msg=updated");
            return;

        } else if ("resetpwd".equals(action)) {
            // Reset mật khẩu về mặc định "Admin@123" (hash SHA-1 trước khi lưu)
            userDAO.resetPassword(
                    Integer.parseInt(request.getParameter("id")),
                    PasswordUtil.sha1("Admin@123"));
            response.sendRedirect(request.getContextPath() + "/admin/users?msg=reset");
            return;
        }

        // Hiển thị danh sách tất cả user
        request.setAttribute("users", userDAO.getAll());
        request.setAttribute("msg", request.getParameter("msg")); // Thông báo sau redirect
        request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
    }

    /**
     * POST: Xử lý thêm mới và cập nhật user.
     *
     * action=add:
     * - Kiểm tra username trùng trước khi tạo
     * - Mật khẩu mặc định "Admin@123" đã hash SHA-1
     *
     * action=edit:
     * - Cập nhật fullName, email, phone, role, isActive
     * - Không thay đổi username và password
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        UserDAO userDAO = new UserDAO();
        String action = request.getParameter("action");

        if ("add".equals(action)) {
            String username = request.getParameter("username");

            // Kiểm tra username đã tồn tại chưa trước khi tạo
            if (userDAO.usernameExists(username)) {
                request.setAttribute("error", "Tên đăng nhập đã tồn tại!");
                request.setAttribute("users", userDAO.getAll());
                request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
                return;
            }

            // Tạo user mới với mật khẩu mặc định đã hash SHA-1
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPasswordHash(PasswordUtil.sha1("Admin@123"));
            newUser.setFullName(request.getParameter("fullName"));
            newUser.setEmail(request.getParameter("email"));
            newUser.setPhone(request.getParameter("phone"));
            newUser.setRole(request.getParameter("role"));
            userDAO.insert(newUser);
            response.sendRedirect(request.getContextPath() + "/admin/users?msg=added");

        } else if ("edit".equals(action)) {
            // Cập nhật thông tin user (không thay đổi username và password)
            User editedUser = new User();
            editedUser.setId(Integer.parseInt(request.getParameter("id")));
            editedUser.setFullName(request.getParameter("fullName"));
            editedUser.setEmail(request.getParameter("email"));
            editedUser.setPhone(request.getParameter("phone"));
            editedUser.setRole(request.getParameter("role"));
            editedUser.setActive("1".equals(request.getParameter("isActive"))); // "1" = active
            userDAO.update(editedUser);
            response.sendRedirect(request.getContextPath() + "/admin/users?msg=updated");
        }
    }
}
