package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Stop;

/**
 * StopDAO – Xử lý CSDL cho bảng {@code stops} (điểm dừng xe buýt).
 *
 * Mỗi điểm dừng thuộc 1 tuyến (route_id) và có thứ tự dọc lộ trình (stop_order).
 * Điểm dừng được dùng để:
 *   - Xác định vị trí học sinh lên/xuống xe trong Attendance
 *   - Hiển thị lộ trình khi phụ huynh đăng ký
 *
 * Khác với Route (soft delete), Stop có thể bị xóa cứng (DELETE) vì
 * một điểm dừng không còn liên kết với điểm danh lịch sử phức tạp như tuyến đường.
 */
public class StopDAO extends DBContext {

    // ─── CÁC PHƯƠNG THỨC ĐỌC DỮ LIỆU ────────────────────────────────────────

    /**
     * Lấy danh sách điểm dừng của một tuyến, sắp xếp theo thứ tự lộ trình.
     * Dùng khi:
     *   - Monitor xem điểm dừng trong chuyến xe (để chọn nơi HS lên/xuống)
     *   - Phụ huynh chọn điểm dừng khi đăng ký xe buýt
     *   - Admin quản lý điểm dừng của tuyến
     *
     * @param routeId ID tuyến đường
     */
    public List<Stop> getByRoute(int routeId) {
        List<Stop> list = new ArrayList<>();
        String sql = "SELECT * FROM stops WHERE route_id=? ORDER BY stop_order";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, routeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy toàn bộ điểm dừng, nhóm theo tuyến rồi theo thứ tự.
     * Ít dùng trực tiếp, thường dùng getByRoute() cho hiệu quả hơn.
     */
    public List<Stop> getAll() {
        List<Stop> list = new ArrayList<>();
        String sql = "SELECT * FROM stops ORDER BY route_id, stop_order";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Tìm điểm dừng theo ID. Dùng khi cần load chi tiết để chỉnh sửa.
     *
     * @return Stop hoặc null nếu không tìm thấy
     */
    public Stop getById(int id) {
        String sql = "SELECT * FROM stops WHERE id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ─── CÁC PHƯƠNG THỨC GHI DỮ LIỆU ────────────────────────────────────────

    /**
     * Thêm điểm dừng mới vào tuyến.
     * stop_order xác định vị trí điểm dừng trong lộ trình (1, 2, 3, ...).
     * estimated_morning_time và estimated_afternoon_time có thể null.
     */
    public boolean insert(Stop s) {
        String sql = "INSERT INTO stops (route_id,stop_name,stop_order,address,"
                   + "estimated_morning_time,estimated_afternoon_time) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, s.getRouteId());
            ps.setString(2, s.getStopName());
            ps.setInt(3, s.getStopOrder());
            ps.setString(4, s.getAddress());
            ps.setTime(5, s.getEstimatedMorningTime());   // null = không có giờ ước tính
            ps.setTime(6, s.getEstimatedAfternoonTime());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Cập nhật thông tin điểm dừng.
     * Không cập nhật route_id vì điểm dừng không được chuyển tuyến sau khi tạo.
     */
    public boolean update(Stop s) {
        String sql = "UPDATE stops SET stop_name=?,stop_order=?,address=?,"
                   + "estimated_morning_time=?,estimated_afternoon_time=? WHERE id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, s.getStopName());
            ps.setInt(2, s.getStopOrder());
            ps.setString(3, s.getAddress());
            ps.setTime(4, s.getEstimatedMorningTime());
            ps.setTime(5, s.getEstimatedAfternoonTime());
            ps.setInt(6, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Xóa cứng điểm dừng khỏi hệ thống.
     *
     * ⚠️ CẢNH BÁO: Xóa điểm dừng có thể gây lỗi nếu điểm dừng đang được
     *    tham chiếu bởi attendance (boarded_stop_id/alighted_stop_id).
     *    Nên kiểm tra trước khi xóa để tránh lỗi FK constraint.
     *
     * @return true nếu xóa thành công
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM stops WHERE id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Thường gặp khi điểm dừng đang được dùng (FK constraint violation)
            System.err.println("[StopDAO.delete] Không thể xóa điểm dừng id=" + id
                             + " (có thể đang được tham chiếu): " + e.getMessage());
        }
        return false;
    }

    // ─── PHƯƠNG THỨC NỘI BỘ ──────────────────────────────────────────────────

    /** Ánh xạ ResultSet → Stop. Tất cả cột đều lấy trực tiếp từ bảng stops. */
    private Stop mapRow(ResultSet rs) throws SQLException {
        Stop s = new Stop();
        s.setId(rs.getInt("id"));
        s.setRouteId(rs.getInt("route_id"));
        s.setStopName(rs.getString("stop_name"));
        s.setStopOrder(rs.getInt("stop_order"));
        s.setAddress(rs.getString("address"));
        s.setEstimatedMorningTime(rs.getTime("estimated_morning_time"));
        s.setEstimatedAfternoonTime(rs.getTime("estimated_afternoon_time"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}
