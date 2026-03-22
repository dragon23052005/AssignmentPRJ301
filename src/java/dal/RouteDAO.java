package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Route;

/**
 * RouteDAO – Xử lý CSDL cho bảng {@code routes} (tuyến đường xe buýt).
 *
 * Mỗi tuyến có:
 *   - Mã tuyến (route_code), tên tuyến (route_name)
 *   - Giờ khởi hành buổi sáng và chiều
 *   - Danh sách điểm dừng (quản lý bởi StopDAO)
 *   - Số học sinh đã đăng ký tuyến (tính từ bảng registrations)
 *
 * Route không bị xóa cứng (hard delete) mà chỉ set is_active=0 (soft delete)
 * để giữ lịch sử cho các chuyến xe đã hoàn thành.
 */
public class RouteDAO extends DBContext {

    // ─── CÁC PHƯƠNG THỨC ĐỌC DỮ LIỆU ────────────────────────────────────────

    /**
     * Lấy toàn bộ tuyến đường kèm số điểm dừng và số học sinh đang đăng ký.
     * Dùng cho bảng quản lý tuyến đường của Admin.
     *
     * Dùng correlated subquery để đếm stop_count và student_count:
     *   - Hiệu quả với dữ liệu vừa phải, dễ đọc hơn GROUP BY.
     *   - Nếu dữ liệu lớn, nên chuyển sang LEFT JOIN + COUNT.
     */
    public List<Route> getAll() {
        List<Route> list = new ArrayList<>();
        String sql = """
            SELECT r.*,
                   (SELECT COUNT(*) FROM stops s WHERE s.route_id=r.id) AS stop_count,
                   (SELECT COUNT(*) FROM registrations reg WHERE reg.route_id=r.id AND reg.is_active=1) AS student_count
            FROM routes r ORDER BY r.route_code
            """;
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy danh sách tuyến đang hoạt động (is_active=1), sắp theo tên.
     * Dùng để tạo dropdown chọn tuyến khi đăng ký xe buýt hoặc phân công xe.
     */
    public List<Route> getActive() {
        List<Route> list = new ArrayList<>();
        String sql = "SELECT * FROM routes WHERE is_active=1 ORDER BY route_name";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Tìm tuyến theo ID.
     * Dùng khi cần load chi tiết tuyến để chỉnh sửa hoặc xem danh sách điểm dừng.
     *
     * @return Route hoặc null nếu không tìm thấy
     */
    public Route getById(int id) {
        String sql = "SELECT * FROM routes WHERE id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Đếm số tuyến đang active. Dùng trên Dashboard admin.
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM routes WHERE is_active=1";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ─── CÁC PHƯƠNG THỨC GHI DỮ LIỆU ────────────────────────────────────────

    /**
     * Thêm tuyến đường mới.
     * route_code không thể chỉnh sửa sau khi tạo (dùng để nhận dạng cố định).
     */
    public boolean insert(Route r) {
        String sql = "INSERT INTO routes (route_name,route_code,description,morning_departure,afternoon_departure) "
                   + "VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, r.getRouteName());
            ps.setString(2, r.getRouteCode());
            ps.setString(3, r.getDescription());
            ps.setTime(4, r.getMorningDeparture());    // null nếu không có ca sáng
            ps.setTime(5, r.getAfternoonDeparture());  // null nếu không có ca chiều
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Cập nhật thông tin tuyến (không cập nhật route_code vì đây là mã cố định).
     */
    public boolean update(Route r) {
        String sql = "UPDATE routes SET route_name=?,description=?,morning_departure=?,afternoon_departure=?,is_active=? "
                   + "WHERE id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, r.getRouteName());
            ps.setString(2, r.getDescription());
            ps.setTime(3, r.getMorningDeparture());
            ps.setTime(4, r.getAfternoonDeparture());
            ps.setInt(5, r.isActive() ? 1 : 0);
            ps.setInt(6, r.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Vô hiệu hóa tuyến (soft delete: set is_active=0).
     * KHÔNG xóa cứng để giữ lịch sử điểm danh của các chuyến đã qua.
     */
    public boolean delete(int id) {
        String sql = "UPDATE routes SET is_active=0 WHERE id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ─── PHƯƠNG THỨC NỘI BỘ ──────────────────────────────────────────────────

    /**
     * Ánh xạ ResultSet → Route.
     * stop_count và student_count là các cột tùy chọn (chỉ có khi query từ getAll()).
     * Dùng try-catch riêng để tránh crash khi các cột này không tồn tại.
     */
    private Route mapRow(ResultSet rs) throws SQLException {
        Route r = new Route();
        r.setId(rs.getInt("id"));
        r.setRouteName(rs.getString("route_name"));
        r.setRouteCode(rs.getString("route_code"));
        r.setDescription(rs.getString("description"));
        r.setMorningDeparture(rs.getTime("morning_departure"));
        r.setAfternoonDeparture(rs.getTime("afternoon_departure"));
        r.setActive(rs.getInt("is_active") == 1);
        r.setCreatedAt(rs.getTimestamp("created_at"));

        // Các cột này chỉ có khi dùng getAll() với subquery → bắt exception khi không có
        try { r.setStopCount(rs.getInt("stop_count")); }       catch (Exception ignored) {}
        try { r.setStudentCount(rs.getInt("student_count")); } catch (Exception ignored) {}
        return r;
    }
}
