package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;

/**
 * UserDAO – Xử lý toàn bộ thao tác CSDL liên quan đến bảng {@code users}.
 *
 * Bảng users chứa tất cả người dùng của hệ thống (ADMIN, PARENT, MONITOR,
 * DRIVER).
 *
 * Các phương thức chính:
 * - login() : Xác thực đăng nhập
 * - getAll() : Lấy toàn bộ danh sách user
 * - getByRole() : Lấy user theo role (dùng cho dropdown phân công)
 * - insert() : Thêm user mới (Admin dùng)
 * - registerParent() : Đăng ký tài khoản phụ huynh mới
 * - update() : Cập nhật thông tin user
 * - resetPassword() : Đặt lại mật khẩu theo ID
 * - resetPasswordByEmail(): Đặt lại mật khẩu theo Email (quên mật khẩu)
 * - toggleActive() : Bật/tắt tài khoản
 * - countByRole() : Đếm số user theo role (dùng cho dashboard)
 * - findByEmail() : Tìm user theo email (dùng cho quên mật khẩu)
 * - usernameExists() : Kiểm tra username đã tồn tại chưa
 * - emailExists() : Kiểm tra email đã tồn tại chưa
 */
public class UserDAO extends DBContext {

    // ─── CÁC PHƯƠNG THỨC ĐỌC DỮ LIỆU ────────────────────────────────────────

    /**
     * Xác thực đăng nhập: tìm user theo username + password_hash và is_active=1.
     *
     * ⚠️ password ở đây phải là chuỗi đã hash (SHA-1) – không phải plain text.
     * Xem {@link util.PasswordUtil#sha1(String)}.
     *
     * @param username Tên đăng nhập
     * @param password Mật khẩu đã mã hóa SHA-1
     * @return User nếu đăng nhập thành công, null nếu sai thông tin hoặc bị khóa
     */
    public User login(String username, String password) {
        // Bảo vệ: không truy vấn nếu kết nối lỗi
        if (c == null) {
            System.err.println("[UserDAO.login] Kết nối CSDL là null!");
            return null;
        }
        String sql = "SELECT * FROM users WHERE username=? AND password_hash=? AND is_active=1";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return mapRow(resultSet);
        } catch (SQLException exception) {
            System.err.println("[UserDAO.login] Lỗi SQL: " + exception.getMessage());
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy toàn bộ danh sách user, sắp xếp theo role rồi theo tên.
     * Dùng cho trang quản lý user của ADMIN.
     */
    public List<User> getAll() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, full_name";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
                userList.add(mapRow(resultSet));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return userList;
    }

    /**
     * Lấy danh sách user theo role (chỉ lấy tài khoản đang active).
     * Dùng để tạo dropdown chọn lái xe / quản lý xe khi cấu hình xe.
     *
     * @param role Giá trị role: "ADMIN" | "PARENT" | "MONITOR" | "DRIVER"
     */
    public List<User> getByRole(String role) {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role=? AND is_active=1 ORDER BY full_name";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, role);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
                userList.add(mapRow(resultSet));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return userList;
    }

    /**
     * Tìm user theo ID. Dùng khi cần load thông tin để chỉnh sửa.
     *
     * @return User hoặc null nếu không tìm thấy
     */
    public User getById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return mapRow(resultSet);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm user theo địa chỉ email.
     * Dùng cho chức năng quên mật khẩu: xác minh email tồn tại trước khi reset.
     *
     * @param email Địa chỉ email cần tìm
     * @return User nếu email tồn tại, null nếu không tìm thấy
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email=? AND is_active=1";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return mapRow(resultSet);
        } catch (SQLException exception) {
            System.err.println("[UserDAO.findByEmail] Lỗi SQL: " + exception.getMessage());
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Kiểm tra username đã tồn tại trong hệ thống chưa.
     * Dùng trước khi thêm user mới để tránh trùng lặp.
     *
     * @return true nếu username đã bị dùng
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT id FROM users WHERE username=?";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, username);
            return preparedStatement.executeQuery().next();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * Kiểm tra email đã tồn tại trong hệ thống chưa.
     * Dùng trước khi đăng ký để đảm bảo mỗi email chỉ có 1 tài khoản.
     *
     * @return true nếu email đã được sử dụng
     */
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email=?";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, email);
            return preparedStatement.executeQuery().next();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * Đếm số user đang active theo role. Dùng để hiển thị số liệu trên Dashboard.
     */
    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role=? AND is_active=1";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, role);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getInt(1);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    // ─── CÁC PHƯƠNG THỨC GHI DỮ LIỆU ────────────────────────────────────────

    /**
     * Thêm user mới vào hệ thống (Admin dùng).
     * Mật khẩu lưu vào cột password_hash (phải hash SHA-1 trước khi gọi).
     *
     * @return true nếu thêm thành công
     */
    public boolean insert(User user) {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone, role) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPasswordHash());
            preparedStatement.setString(3, user.getFullName());
            preparedStatement.setString(4, user.getEmail());
            preparedStatement.setString(5, user.getPhone());
            preparedStatement.setString(6, user.getRole());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * Đăng ký tài khoản phụ huynh mới (do chính phụ huynh tự tạo).
     * Luôn tạo với role = "PARENT" và is_active = 1.
     *
     * Khác với insert() (dùng cho Admin tạo bất kỳ role nào),
     * registerParent() chỉ tạo tài khoản phụ huynh để đảm bảo an toàn.
     *
     * @param user Đối tượng User với username, passwordHash, fullName, email, phone
     *             đã được set
     * @return true nếu đăng ký thành công
     */
    public boolean registerParent(User user) {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone, role, is_active) "
                + "VALUES (?, ?, ?, ?, ?, 'PARENT', 1)";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPasswordHash());
            preparedStatement.setString(3, user.getFullName());
            preparedStatement.setString(4, user.getEmail());
            preparedStatement.setString(5, user.getPhone());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException exception) {
            System.err.println("[UserDAO.registerParent] Lỗi SQL: " + exception.getMessage());
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin user (không cập nhật username và password).
     * Admin dùng để sửa thông tin cơ bản và thay đổi trạng thái active.
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET full_name=?, email=?, phone=?, role=?, is_active=? WHERE id=?";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, user.getFullName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPhone());
            preparedStatement.setString(4, user.getRole());
            preparedStatement.setInt(5, user.isActive() ? 1 : 0); // boolean → bit SQL
            preparedStatement.setInt(6, user.getId());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * Đặt lại mật khẩu cho user theo ID (Admin dùng).
     *
     * @param id          ID user cần reset password
     * @param newPassword Mật khẩu mới đã hash SHA-1
     */
    public boolean resetPassword(int id, String newPassword) {
        String sql = "UPDATE users SET password_hash=? WHERE id=?";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, newPassword);
            preparedStatement.setInt(2, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * Đặt lại mật khẩu theo email (dùng cho chức năng Quên mật khẩu).
     * Chỉ cập nhật nếu email tồn tại và tài khoản đang active.
     *
     * Luồng quên mật khẩu:
     * Người dùng nhập email → Hệ thống xác minh → Cho phép đặt mật khẩu mới
     * → Gọi resetPasswordByEmail() → Cập nhật trong DB
     *
     * @param email       Email của tài khoản cần đặt lại mật khẩu
     * @param newPassword Mật khẩu mới đã hash SHA-1
     * @return true nếu cập nhật thành công (email tồn tại và đang active)
     */
    public boolean resetPasswordByEmail(String email, String newPassword) {
        String sql = "UPDATE users SET password_hash=? WHERE email=? AND is_active=1";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, email);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException exception) {
            System.err.println("[UserDAO.resetPasswordByEmail] Lỗi SQL: " + exception.getMessage());
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * Đảo trạng thái is_active của user (1→0 hoặc 0→1) trong 1 câu SQL.
     * Kỹ thuật dùng CASE WHEN giúp tránh race condition khi 2 request cùng lúc.
     */
    public boolean toggleActive(int id) {
        String sql = "UPDATE users SET is_active = CASE WHEN is_active=1 THEN 0 ELSE 1 END WHERE id=?";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    // ─── PHƯƠNG THỨC NỘI BỘ ──────────────────────────────────────────────────

    /**
     * Ánh xạ một hàng ResultSet → đối tượng User.
     * Tập trung tại đây để tránh lặp code ở nhiều phương thức khác nhau.
     *
     * ⚠️ Phương thức này ném SQLException → bắt buộc caller phải xử lý.
     */
    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setRole(resultSet.getString("role"));
        user.setActive(resultSet.getInt("is_active") == 1); // SQL bit → Java boolean
        user.setCreatedAt(resultSet.getTimestamp("created_at"));
        return user;
    }
}
