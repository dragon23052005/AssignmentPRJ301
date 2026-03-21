package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Registration;

/**
 * RegistrationDAO – Xử lý CSDL cho bảng {@code registrations} (đăng ký tuyến xe).
 *
 * Mỗi học sinh có thể đăng ký 1 tuyến xe (route) tại 1 điểm dừng (stop).
 * Một học sinh chỉ có 1 registration active tại một thời điểm.
 *
 * Hành động "đăng ký mới" = hủy cũ + tạo mới (soft delete, không xóa cứng).
 * Điều này giữ lại lịch sử đăng ký đầy đủ trong DB.
 */
public class RegistrationDAO extends DBContext {

    // ─── CÁC PHƯƠNG THỨC GHI DỮ LIỆU ────────────────────────────────────────

    /**
     * Đăng ký học sinh vào tuyến xe + điểm dừng.
     *
     * Thực hiện trong 1 transaction:
     *   Bước 1: Hủy đăng ký cũ (set is_active=0, ghi ngày kết thúc = hôm nay)
     *   Bước 2: Tạo đăng ký mới với tuyến/điểm dừng mới
     *
     * Nếu bất kỳ bước nào thất bại → rollback để đảm bảo tính nhất quán.
     *
     * @param studentId ID học sinh
     * @param routeId   ID tuyến đường mới
     * @param stopId    ID điểm dừng đón học sinh
     * @param startDate Ngày bắt đầu có hiệu lực
     * @return true nếu đăng ký thành công
     */
    public boolean register(int studentId, int routeId, int stopId, java.sql.Date startDate) {
        String cancel = "UPDATE registrations SET is_active=0, end_date=CAST(GETDATE() AS DATE) "
                      + "WHERE student_id=? AND is_active=1";
        String insert = "INSERT INTO registrations (student_id,route_id,stop_id,start_date,is_active) "
                      + "VALUES (?,?,?,?,1)";
        try {
            // Bắt đầu transaction thủ công để đảm bảo atomicity
            c.setAutoCommit(false);

            // Bước 1: Hủy đăng ký cũ (nếu có)
            PreparedStatement ps1 = c.prepareStatement(cancel);
            ps1.setInt(1, studentId);
            ps1.executeUpdate();

            // Bước 2: Tạo đăng ký mới
            PreparedStatement ps2 = c.prepareStatement(insert);
            ps2.setInt(1, studentId);
            ps2.setInt(2, routeId);
            ps2.setInt(3, stopId);
            ps2.setDate(4, startDate);
            ps2.executeUpdate();

            c.commit();          // Commit nếu cả 2 bước đều thành công
            c.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            // Rollback nếu có lỗi → dữ liệu không bị thay đổi nửa vời
            try {
                c.rollback();
                c.setAutoCommit(true);
            } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * Hủy đăng ký xe buýt của học sinh (soft delete: set is_active=0).
     * Thường được phụ huynh hoặc Admin gọi khi học sinh thôi đi xe.
     *
     * @param studentId ID học sinh cần hủy
     * @return true nếu hủy thành công (có ít nhất 1 record bị ảnh hưởng)
     */
    public boolean cancel(int studentId) {
        String sql = "UPDATE registrations SET is_active=0, end_date=CAST(GETDATE() AS DATE) "
                   + "WHERE student_id=? AND is_active=1";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ─── CÁC PHƯƠNG THỨC ĐỌC DỮ LIỆU ────────────────────────────────────────

    /**
     * Lấy đăng ký đang active của một học sinh (kèm tên tuyến và điểm dừng).
     * Dùng để hiển thị thông tin đăng ký hiện tại trên trang phụ huynh.
     *
     * @return Registration đang active, hoặc null nếu chưa đăng ký
     */
    public Registration getActiveByStudent(int studentId) {
        String sql = """
            SELECT r.*, ro.route_name, s.stop_name
            FROM registrations r
            JOIN routes ro ON r.route_id=ro.id
            JOIN stops  s  ON r.stop_id=s.id
            WHERE r.student_id=? AND r.is_active=1
            """;
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Lấy toàn bộ lịch sử đăng ký (kể cả đã hủy), mới nhất trước.
     * Dùng cho Admin xem tổng quan đăng ký.
     */
    public List<Registration> getAll() {
        List<Registration> list = new ArrayList<>();
        String sql = """
            SELECT r.*, ro.route_name, s.stop_name
            FROM registrations r
            JOIN routes ro ON r.route_id=ro.id
            JOIN stops  s  ON r.stop_id=s.id
            ORDER BY r.registered_at DESC
            """;
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ─── PHƯƠNG THỨC NỘI BỘ ──────────────────────────────────────────────────

    /** Ánh xạ ResultSet → Registration (bao gồm route_name và stop_name từ JOIN). */
    private Registration mapRow(ResultSet rs) throws SQLException {
        Registration r = new Registration();
        r.setId(rs.getInt("id"));
        r.setStudentId(rs.getInt("student_id"));
        r.setRouteId(rs.getInt("route_id"));
        r.setRouteName(rs.getString("route_name")); // từ JOIN routes
        r.setStopId(rs.getInt("stop_id"));
        r.setStopName(rs.getString("stop_name"));   // từ JOIN stops
        r.setStartDate(rs.getDate("start_date"));
        r.setEndDate(rs.getDate("end_date"));       // null nếu vẫn đang active
        r.setActive(rs.getInt("is_active") == 1);
        r.setRegisteredAt(rs.getTimestamp("registered_at"));
        return r;
    }
}
