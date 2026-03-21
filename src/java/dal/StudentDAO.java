package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Student;

/**
 * StudentDAO – Xử lý CSDL cho bảng {@code students} (học sinh).
 *
 * Mỗi học sinh thuộc 1 phụ huynh (parent_id → users).
 * Học sinh có thể đăng ký 1 tuyến xe (được JOIN từ bảng registrations).
 *
 * BASE_SQL luôn JOIN phụ huynh và đăng ký active để tiện hiển thị
 * thông tin tổng hợp trong 1 lần query.
 *
 * Phương thức getByRouteAndDate() là quan trọng nhất: được dùng khi Monitor
 * tạo chuyến mới để xác định danh sách HS cần điểm danh.
 */
public class StudentDAO extends DBContext {

    /**
     * SQL nền JOIN phụ huynh và đăng ký (active) + tên tuyến + tên điểm dừng.
     * LEFT JOIN registrations để vẫn trả về HS chưa có đăng ký xe buýt.
     */
    private static final String BASE_SQL = """
        SELECT s.*, u.full_name AS parent_name, u.phone AS parent_phone,
               reg.id AS registration_id, reg.route_id, reg.stop_id, reg.is_active AS reg_active,
               r.route_name, st.stop_name, st.estimated_morning_time, st.estimated_afternoon_time
        FROM students s
        JOIN users u ON s.parent_id = u.id
        LEFT JOIN registrations reg ON reg.student_id=s.id AND reg.is_active=1
        LEFT JOIN routes r ON reg.route_id=r.id
        LEFT JOIN stops  st ON reg.stop_id=st.id
        """;

    // ─── CÁC PHƯƠNG THỨC ĐỌC DỮ LIỆU ────────────────────────────────────────

    /** Lấy toàn bộ danh sách học sinh. Dùng cho bảng quản lý học sinh của Admin. */
    public List<Student> getAll() {
        List<Student> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " ORDER BY s.full_name");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy danh sách con em của một phụ huynh.
     * Phụ huynh dùng để xem và quản lý con mình trên dashboard.
     *
     * @param parentId ID tài khoản phụ huynh
     */
    public List<Student> getByParentId(int parentId) {
        List<Student> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " WHERE s.parent_id=? ORDER BY s.full_name");
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy danh sách học sinh đăng ký một tuyến vào một ngày cụ thể.
     * ⭐ Phương thức quan trọng nhất: được gọi khi Monitor tạo chuyến mới
     *    để tạo danh sách điểm danh ban đầu.
     *
     * Điều kiện:
     *   - Đang đăng ký tuyến routeId (reg.is_active=1, JOIN bắt buộc)
     *   - Chưa hết hạn đăng ký vào ngày đó (end_date IS NULL OR end_date >= date)
     * Sắp xếp theo thứ tự điểm dừng rồi tên để thuận tiện điểm danh theo lộ trình.
     *
     * @param routeId ID tuyến đường
     * @param date    Ngày chuyến xe (thường là hôm nay)
     */
    public List<Student> getByRouteAndDate(int routeId, java.sql.Date date) {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.*, u.full_name AS parent_name, u.phone AS parent_phone,
                   reg.id AS registration_id, reg.route_id, reg.stop_id, reg.is_active AS reg_active,
                   r.route_name, st.stop_name, st.estimated_morning_time, st.estimated_afternoon_time
            FROM students s
            JOIN users u ON s.parent_id=u.id
            JOIN registrations reg ON reg.student_id=s.id AND reg.is_active=1
            JOIN routes r ON reg.route_id=r.id
            JOIN stops  st ON reg.stop_id=st.id
            WHERE reg.route_id=? AND (reg.end_date IS NULL OR reg.end_date >= ?)
            ORDER BY st.stop_order, s.full_name
            """;
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, routeId);
            ps.setDate(2, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Tìm học sinh theo ID. Dùng khi cần load chi tiết để chỉnh sửa.
     *
     * @return Student hoặc null nếu không tìm thấy
     */
    public Student getById(int id) {
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " WHERE s.id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /** Đếm tổng số học sinh trong hệ thống. Dùng trên Dashboard admin. */
    public int count() {
        String sql = "SELECT COUNT(*) FROM students";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ─── CÁC PHƯƠNG THỨC GHI DỮ LIỆU ────────────────────────────────────────

    /**
     * Thêm học sinh mới.
     * Dùng OUTPUT INSERTED.id để lấy ID vừa tạo → gán vào đối tượng s
     * để caller có thể dùng ngay (ví dụ: để tạo đăng ký kèm theo).
     *
     * @return true nếu thêm thành công (s.getId() sẽ có giá trị mới)
     */
    public boolean insert(Student s) {
        String sql = "INSERT INTO students (full_name,student_code,class_name,date_of_birth,parent_id) "
                   + "OUTPUT INSERTED.id VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, s.getFullName());
            ps.setString(2, s.getStudentCode());
            ps.setString(3, s.getClassName());
            ps.setDate(4, s.getDateOfBirth());  // null nếu không nhập ngày sinh
            ps.setInt(5, s.getParentId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                s.setId(rs.getInt(1)); // Gán ID trả về vào object để caller dùng tiếp
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Cập nhật thông tin học sinh (không cập nhật parent_id).
     * Phụ huynh không thể bị thay đổi sau khi đã tạo học sinh.
     */
    public boolean update(Student s) {
        String sql = "UPDATE students SET full_name=?,student_code=?,class_name=?,date_of_birth=? WHERE id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, s.getFullName());
            ps.setString(2, s.getStudentCode());
            ps.setString(3, s.getClassName());
            ps.setDate(4, s.getDateOfBirth());
            ps.setInt(5, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ─── PHƯƠNG THỨC NỘI BỘ ──────────────────────────────────────────────────

    /**
     * Ánh xạ ResultSet → Student.
     * Bao gồm thông tin phụ huynh và đăng ký xe buýt từ các bảng JOIN.
     * Lưu ý: reg_active trả về 0 khi không có đăng ký (NULL thành 0 trong JDBC).
     */
    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setFullName(rs.getString("full_name"));
        s.setStudentCode(rs.getString("student_code"));
        s.setClassName(rs.getString("class_name"));
        s.setDateOfBirth(rs.getDate("date_of_birth"));
        s.setParentId(rs.getInt("parent_id"));
        s.setParentName(rs.getString("parent_name"));   // từ JOIN users
        s.setParentPhone(rs.getString("parent_phone")); // từ JOIN users
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setRegistrationId(rs.getInt("registration_id")); // 0 nếu chưa đăng ký
        s.setRouteId(rs.getInt("route_id"));               // 0 nếu chưa đăng ký
        s.setRouteName(rs.getString("route_name"));         // null nếu chưa đăng ký
        s.setStopId(rs.getInt("stop_id"));                  // 0 nếu chưa đăng ký
        s.setStopName(rs.getString("stop_name"));           // null nếu chưa đăng ký
        s.setRegistrationActive(rs.getInt("reg_active") == 1);
        
        try {
            s.setEstimatedMorningTime(rs.getTime("estimated_morning_time"));
            s.setEstimatedAfternoonTime(rs.getTime("estimated_afternoon_time"));
        } catch (SQLException ex) {
            // Cột có thể không tồn tại trong một số truy vấn cũ nếu viết không dùng BASE_SQL. Bỏ qua if lỗi
        }
        
        return s;
    }
}
