package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.AbsenceNotification;

/**
 * AbsenceNotificationDAO – Xử lý CSDL cho bảng {@code absence_notifications}.
 *
 * Phụ huynh dùng để báo nghỉ học cho con trước khi xe đến đón.
 * Quản lý xe (Monitor) sẽ nhận thông báo và xác nhận (acknowledge).
 *
 * Vòng đời: PENDING → ACKNOWLEDGED
 *
 * Tích hợp với AttendanceDAO:
 *   Khi tạo chuyến mới, hệ thống kiểm tra danh sách báo nghỉ hôm nay
 *   → tự động set NOTIFIED_ABSENT cho những HS đã báo nghỉ.
 */
public class AbsenceNotificationDAO extends DBContext {

    /**
     * SQL nền JOIN học sinh, người xác nhận, và tuyến đường
     * để hiển thị đủ thông tin trên màn hình quản lý.
     */
    private final String BASE_SQL = """
        SELECT an.*, s.full_name AS student_name, s.student_code, s.class_name,
               u2.full_name AS acknowledged_by_name, r.route_name
        FROM absence_notifications an
        JOIN students s ON an.student_id=s.id
        LEFT JOIN users u2 ON an.acknowledged_by=u2.id
        LEFT JOIN registrations reg ON reg.student_id=s.id AND reg.is_active=1
        LEFT JOIN routes r ON reg.route_id=r.id
        """;

    // ─── CÁC PHƯƠNG THỨC ĐỌC DỮ LIỆU ────────────────────────────────────────

    /**
     * Lấy tất cả thông báo nghỉ của con em một phụ huynh, mới nhất trước.
     * Phụ huynh xem lịch sử các lần báo nghỉ của mình.
     *
     * @param parentId ID tài khoản phụ huynh (bảng users)
     */
    public List<AbsenceNotification> getByParent(int parentId) {
        List<AbsenceNotification> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(
                BASE_SQL + " WHERE s.parent_id=? ORDER BY an.absence_date DESC");
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy tất cả thông báo nghỉ hôm nay (theo ngày hệ thống GETDATE()).
     * Monitor dùng để xem ai cần báo vắng trên chuyến hôm nay.
     * Admin cũng dùng trên Dashboard để theo dõi.
     */
    public List<AbsenceNotification> getToday() {
        List<AbsenceNotification> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(
                BASE_SQL + " WHERE an.absence_date=CAST(GETDATE() AS DATE) ORDER BY an.notified_at DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Lấy toàn bộ lịch sử thông báo nghỉ (dùng cho Admin xem tổng quan). */
    public List<AbsenceNotification> getAll() {
        List<AbsenceNotification> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(
                BASE_SQL + " ORDER BY an.absence_date DESC, an.notified_at DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Kiểm tra học sinh đã có thông báo nghỉ hôm nay chưa.
     * Gọi nhanh, không cần load toàn bộ object.
     * Dùng khi tạo chuyến mới để quyết định NOTIFIED_ABSENT.
     *
     * @return true nếu học sinh có thông báo nghỉ ngày hôm nay
     */
    public boolean isAbsentToday(int studentId) {
        String sql = "SELECT id FROM absence_notifications "
                   + "WHERE student_id=? AND absence_date=CAST(GETDATE() AS DATE)";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, studentId);
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Đếm số thông báo nghỉ hôm nay đang ở trạng thái PENDING (chưa xác nhận).
     * Hiển thị badge cảnh báo trên Dashboard admin/monitor.
     */
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM absence_notifications "
                   + "WHERE status='PENDING' AND absence_date=CAST(GETDATE() AS DATE)";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ─── CÁC PHƯƠNG THỨC GHI DỮ LIỆU ────────────────────────────────────────

    /**
     * Phụ huynh báo nghỉ cho con. Thực hiện UPSERT:
     *   - Nếu đã có thông báo cùng studentId + absenceDate → cập nhật lý do + reset về PENDING
     *   - Nếu chưa có → tạo mới
     *
     * Kỹ thuật UPSERT bằng check-then-insert/update thay vì MERGE để dễ đọc hơn.
     *
     * @param studentId  ID học sinh
     * @param absenceDate Ngày nghỉ (không nhất thiết phải là hôm nay)
     * @param reason     Lý do nghỉ
     * @return true nếu thao tác thành công
     */
    public boolean notify(int studentId, java.sql.Date absenceDate, String reason) {
        String check  = "SELECT id FROM absence_notifications WHERE student_id=? AND absence_date=?";
        String insert = "INSERT INTO absence_notifications (student_id,absence_date,reason) VALUES (?,?,?)";
        String update = "UPDATE absence_notifications SET reason=?,status='PENDING',notified_at=GETDATE() "
                      + "WHERE student_id=? AND absence_date=?";
        try {
            // Kiểm tra đã có thông báo chưa
            PreparedStatement chkPs = c.prepareStatement(check);
            chkPs.setInt(1, studentId);
            chkPs.setDate(2, absenceDate);

            if (chkPs.executeQuery().next()) {
                // Đã có → cập nhật lý do và reset trạng thái
                PreparedStatement updPs = c.prepareStatement(update);
                updPs.setString(1, reason);
                updPs.setInt(2, studentId);
                updPs.setDate(3, absenceDate);
                return updPs.executeUpdate() > 0;
            } else {
                // Chưa có → thêm mới
                PreparedStatement insPs = c.prepareStatement(insert);
                insPs.setInt(1, studentId);
                insPs.setDate(2, absenceDate);
                insPs.setString(3, reason);
                return insPs.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Monitor xác nhận đã tiếp nhận thông báo nghỉ (PENDING → ACKNOWLEDGED).
     * Ghi lại người xác nhận và thời điểm xác nhận.
     *
     * @param id     ID thông báo nghỉ
     * @param userId ID Monitor thực hiện xác nhận
     */
    public boolean acknowledge(int id, int userId) {
        String sql = "UPDATE absence_notifications "
                   + "SET status='ACKNOWLEDGED', acknowledged_by=?, acknowledged_at=GETDATE() "
                   + "WHERE id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ─── PHƯƠNG THỨC NỘI BỘ ──────────────────────────────────────────────────

    /** Ánh xạ ResultSet → AbsenceNotification, bao gồm thông tin JOIN từ bảng khác. */
    private AbsenceNotification mapRow(ResultSet rs) throws SQLException {
        AbsenceNotification an = new AbsenceNotification();
        an.setId(rs.getInt("id"));
        an.setStudentId(rs.getInt("student_id"));
        an.setStudentName(rs.getString("student_name"));               // từ JOIN students
        an.setStudentCode(rs.getString("student_code"));
        an.setClassName(rs.getString("class_name"));
        an.setAbsenceDate(rs.getDate("absence_date"));
        an.setReason(rs.getString("reason"));
        an.setNotifiedAt(rs.getTimestamp("notified_at"));
        an.setStatus(rs.getString("status"));                          // PENDING / ACKNOWLEDGED
        an.setAcknowledgedBy(rs.getInt("acknowledged_by"));
        an.setAcknowledgedByName(rs.getString("acknowledged_by_name")); // từ JOIN users u2
        an.setAcknowledgedAt(rs.getTimestamp("acknowledged_at"));
        an.setRouteName(rs.getString("route_name"));                   // từ JOIN routes
        return an;
    }
}
