package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Attendance;

/**
 * AttendanceDAO – Xử lý CSDL cho bảng {@code attendances} (điểm danh học sinh).
 *
 * Mỗi record attendance tương ứng với 1 học sinh trong 1 chuyến xe.
 * Trạng thái (status): ABSENT → PRESENT → NOTIFIED_ABSENT
 *
 * Luồng điểm danh tiêu biểu:
 *   1. initForTrip()    : Tạo attendance mặc định ABSENT cho mọi HS khi tạo chuyến
 *   2. markBoarded()    : HS lên xe → chuyển sang PRESENT + ghi điểm dừng lên xe
 *   3. markAlighted()   : HS xuống xe → ghi điểm dừng xuống xe
 *   4. markAbsent()     : HS vắng → reset về ABSENT
 *   5. markNotifiedAbsent(): Phụ huynh đã báo nghỉ → NOTIFIED_ABSENT
 */
public class AttendanceDAO extends DBContext {

    /**
     * SQL nền JOIN đủ các bảng để lấy tên HS, SĐT phụ huynh, tên điểm dừng.
     * Dùng kết hợp với WHERE clause bên ngoài.
     */
    private final String BASE_SQL = """
        SELECT a.*,
               s.full_name AS student_name, s.student_code, s.class_name,
               u.phone AS parent_phone,
               bs.stop_name  AS boarded_stop_name,
               als.stop_name AS alighted_stop_name
        FROM attendances a
        JOIN students s ON a.student_id=s.id
        JOIN users u ON s.parent_id=u.id
        LEFT JOIN stops bs  ON a.boarded_stop_id=bs.id
        LEFT JOIN stops als ON a.alighted_stop_id=als.id
        """;

    // ─── CÁC PHƯƠNG THỨC ĐỌC DỮ LIỆU ────────────────────────────────────────

    /**
     * Lấy danh sách điểm danh của một chuyến xe, sắp xếp theo tên HS.
     * Dùng để Monitor xem ai đã lên/vắng trong chuyến đang chọn.
     */
    public List<Attendance> getByTrip(int tripId) {
        List<Attendance> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " WHERE a.trip_id=? ORDER BY s.full_name");
            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Tìm điểm danh của 1 học sinh trong 1 chuyến cụ thể.
     * Dùng trên Dashboard phụ huynh để hiển thị trạng thái con hôm nay.
     *
     * @return Attendance hoặc null nếu chưa có record
     */
    public Attendance getByTripAndStudent(int tripId, int studentId) {
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " WHERE a.trip_id=? AND a.student_id=?");
            ps.setInt(1, tripId);
            ps.setInt(2, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Lấy điểm danh hôm nay của một học sinh, theo ca sáng hoặc chiều.
     * Dùng kết hợp với thông tin chuyến xe để phụ huynh theo dõi.
     *
     * Lưu ý: JOIN thêm bảng trips để lọc theo ngày hôm nay và tripType.
     * TOP(1) + ORDER BY t.id DESC để chọn chuyến mới nhất.
     *
     * @param studentId ID học sinh
     * @param tripType  "MORNING" hoặc "AFTERNOON"
     */
    public Attendance getTodayByStudent(int studentId, String tripType) {
        String sql = """
             SELECT TOP(1) a.*,
                    s.full_name AS student_name, s.student_code, s.class_name,
                    u.phone AS parent_phone,
                    bs.stop_name  AS boarded_stop_name,
                    als.stop_name AS alighted_stop_name
             FROM attendances a
             JOIN students s ON a.student_id=s.id
             JOIN users u ON s.parent_id=u.id
             LEFT JOIN stops bs  ON a.boarded_stop_id=bs.id
             LEFT JOIN stops als ON a.alighted_stop_id=als.id
             JOIN trips t ON a.trip_id=t.id
             WHERE a.student_id=? AND t.trip_date=CAST(GETDATE() AS DATE) AND t.trip_type=?
             ORDER BY t.id DESC
            """;
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, studentId);
            ps.setString(2, tripType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ─── CÁC PHƯƠNG THỨC GHI DỮ LIỆU ────────────────────────────────────────

    /**
     * Khởi tạo điểm danh ban đầu cho tất cả học sinh khi tạo chuyến mới.
     * Mỗi học sinh mặc định là ABSENT cho đến khi Monitor điểm danh thực tế.
     *
     * Dùng "IF NOT EXISTS" để tránh tạo record trùng nếu gọi nhiều lần.
     * Dùng executeBatch() để chèn cùng lúc, tối ưu hơn nhiều lần executeUpdate().
     *
     * @param tripId     ID chuyến xe vừa tạo
     * @param studentIds Danh sách ID học sinh đăng ký tuyến của chuyến này
     */
    public void initForTrip(int tripId, List<Integer> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) return; // không có học sinh → bỏ qua

        String sql = """
            IF NOT EXISTS (SELECT 1 FROM attendances WHERE trip_id=? AND student_id=?)
                INSERT INTO attendances (trip_id,student_id,status) VALUES (?,?,'ABSENT')
            """;
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            for (int sid : studentIds) {
                ps.setInt(1, tripId); ps.setInt(2, sid);
                ps.setInt(3, tripId); ps.setInt(4, sid);
                ps.addBatch(); // Gom thành batch để chạy 1 lần
            }
            ps.executeBatch(); // Thực thi tất cả
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Đánh dấu học sinh đã lên xe (PRESENT) tại một điểm dừng.
     * Ghi thời điểm lên xe bằng GETDATE() và điểm dừng tương ứng.
     *
     * @param tripId    ID chuyến xe
     * @param studentId ID học sinh
     * @param stopId    ID điểm dừng nơi học sinh lên (0 nếu không rõ điểm dừng)
     * @param updatedBy ID người thực hiện điểm danh (Monitor)
     */
    public boolean markBoarded(int tripId, int studentId, int stopId, int updatedBy) {
        String sql = "UPDATE attendances SET status='PRESENT', boarded_at=GETDATE(), "
                   + "boarded_stop_id=?, updated_by=? WHERE trip_id=? AND student_id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            // Nếu stopId=0 thì lưu NULL vào DB (không chọn điểm dừng cụ thể)
            ps.setObject(1, stopId > 0 ? stopId : null);
            ps.setInt(2, updatedBy);
            ps.setInt(3, tripId);
            ps.setInt(4, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Ghi nhận học sinh đã xuống xe tại một điểm dừng.
     * Chỉ cập nhật thông tin alighted, không đổi status (vẫn là PRESENT).
     *
     * @param stopId ID điểm dừng nơi xuống (0 nếu không rõ)
     */
    public boolean markAlighted(int tripId, int studentId, int stopId, int updatedBy) {
        String sql = "UPDATE attendances SET alighted_at=GETDATE(), alighted_stop_id=?, "
                   + "updated_by=? WHERE trip_id=? AND student_id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setObject(1, stopId > 0 ? stopId : null);
            ps.setInt(2, updatedBy);
            ps.setInt(3, tripId);
            ps.setInt(4, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Đánh dấu học sinh vắng mặt, đồng thời xóa sạch thông tin lên/xuống xe.
     * Dùng khi Monitor sửa lại điểm danh từ PRESENT → ABSENT.
     */
    public boolean markAbsent(int tripId, int studentId, int updatedBy) {
        String sql = "UPDATE attendances SET status='ABSENT', "
                   + "boarded_at=NULL, boarded_stop_id=NULL, "
                   + "alighted_at=NULL, alighted_stop_id=NULL, "
                   + "updated_by=? WHERE trip_id=? AND student_id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, updatedBy);
            ps.setInt(2, tripId);
            ps.setInt(3, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Chuyển trạng thái từ ABSENT → NOTIFIED_ABSENT khi phụ huynh đã báo nghỉ.
     * Chỉ cập nhật nếu status hiện tại là ABSENT (điều kiện AND status='ABSENT').
     * Tránh ghi đè khi HS đã được điểm danh PRESENT.
     */
    public void markNotifiedAbsent(int tripId, int studentId) {
        String sql = "UPDATE attendances SET status='NOTIFIED_ABSENT' "
                   + "WHERE trip_id=? AND student_id=? AND status='ABSENT'";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, tripId);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ─── PHƯƠNG THỨC NỘI BỘ ──────────────────────────────────────────────────

    /** Ánh xạ ResultSet → Attendance (bao gồm cả thông tin JOIN từ các bảng khác). */
    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getInt("id"));
        a.setTripId(rs.getInt("trip_id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setStudentName(rs.getString("student_name"));         // từ JOIN students
        a.setStudentCode(rs.getString("student_code"));
        a.setClassName(rs.getString("class_name"));
        a.setStatus(rs.getString("status"));                    // PRESENT / ABSENT / NOTIFIED_ABSENT
        a.setBoardedAt(rs.getTimestamp("boarded_at"));
        a.setBoardedStopId(rs.getInt("boarded_stop_id"));
        a.setBoardedStopName(rs.getString("boarded_stop_name")); // từ JOIN stops bs
        a.setAlightedAt(rs.getTimestamp("alighted_at"));
        a.setAlightedStopId(rs.getInt("alighted_stop_id"));
        a.setAlightedStopName(rs.getString("alighted_stop_name")); // từ JOIN stops als
        a.setNotes(rs.getString("notes"));
        a.setUpdatedBy(rs.getInt("updated_by"));
        a.setUpdatedAt(rs.getTimestamp("updated_at"));
        a.setParentPhone(rs.getString("parent_phone"));          // từ JOIN users
        return a;
    }
}
