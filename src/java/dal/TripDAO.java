package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Trip;

/**
 * TripDAO – Xử lý CSDL cho bảng {@code trips} (các chuyến xe).
 *
 * Một "Trip" là một chuyến xe cụ thể theo ngày và ca (sáng/chiều).
 * Vòng đời của Trip: SCHEDULED → DEPARTED → COMPLETED (hoặc CANCELLED).
 *
 * BASE_SQL được dùng lại trong nhiều truy vấn để tránh lặp code SQL,
 * đồng thời đảm bảo luôn JOIN đủ thông tin phụ (biển số, tên tuyến,
 * số học sinh có mặt, tổng số học sinh).
 */
public class TripDAO extends DBContext {

    /**
     * Câu SQL cơ sở JOIN đủ các bảng liên quan.
     * Các subquery đếm attendance được inlined để tiện hiển thị tỷ lệ điểm danh.
     *
     * Dùng kết hợp: BASE_SQL + " WHERE ..." hoặc BASE_SQL + " ORDER BY ..."
     */
    private final String BASE_SQL = """
            SELECT t.*, v.plate_number, r.route_name, u.full_name AS created_by_name,
                   (SELECT COUNT(*) FROM attendances a WHERE a.trip_id=t.id AND a.status='PRESENT') AS present_count,
                   (SELECT COUNT(*) FROM attendances a WHERE a.trip_id=t.id) AS total_count
            FROM trips t
            JOIN vehicles v ON t.vehicle_id=v.id
            JOIN routes r ON t.route_id=r.id
            LEFT JOIN users u ON t.created_by=u.id
            """;

    // ─── CÁC PHƯƠNG THỨC ĐỌC DỮ LIỆU ────────────────────────────────────────

    /**
     * Lấy danh sách chuyến xe của một xe cụ thể trong một ngày.
     * Dùng bởi Monitor và Driver để xem chuyến hôm nay của xe mình.
     *
     * @param vehicleId ID xe
     * @param date      Ngày cần xem (java.sql.Date)
     * @return Danh sách Trip sắp theo ca (MORNING trước AFTERNOON)
     */
    public List<Trip> getByVehicleAndDate(int vehicleId, java.sql.Date date) {
        List<Trip> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(
                    BASE_SQL + " WHERE t.vehicle_id=? AND t.trip_date=? ORDER BY t.trip_type");
            ps.setInt(1, vehicleId);
            ps.setDate(2, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy N chuyến xe gần nhất (theo ngày giảm dần).
     * Dùng trên Dashboard admin để hiển thị hoạt động gần đây.
     *
     * ⚠️ Dùng TOP() thay vì LIMIT vì đây là SQL Server.
     *
     * @param limit Số lượng record cần lấy (vd: 10)
     */
    public List<Trip> getRecent(int limit) {
        List<Trip> list = new ArrayList<>();
        // Phải tách riêng câu SQL vì SQL Server không cho TOP(?) dùng với BASE_SQL text
        // block
        String sql = "SELECT TOP(?) t.*, v.plate_number, r.route_name, u.full_name AS created_by_name,"
                + "(SELECT COUNT(*) FROM attendances a WHERE a.trip_id=t.id AND a.status='PRESENT') AS present_count,"
                + "(SELECT COUNT(*) FROM attendances a WHERE a.trip_id=t.id) AS total_count "
                + "FROM trips t JOIN vehicles v ON t.vehicle_id=v.id JOIN routes r ON t.route_id=r.id "
                + "LEFT JOIN users u ON t.created_by=u.id ORDER BY t.trip_date DESC, t.trip_type";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy toàn bộ chuyến xe, sắp xếp mới nhất trước.
     * Dùng cho trang quản lý chuyến xe của Admin.
     */
    public List<Trip> getAll() {
        List<Trip> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " ORDER BY t.trip_date DESC, t.trip_type");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm chuyến xe theo ID.
     * Dùng khi Monitor chọn 1 chuyến để xem chi tiết điểm danh.
     *
     * @return Trip hoặc null nếu không tìm thấy
     */
    public Trip getById(int id) {
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " WHERE t.id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy chuyến xe hôm nay của một tuyến đường, theo ca sáng hoặc chiều.
     * Dùng trên dashboard phụ huynh để xem trạng thái chuyến của con.
     *
     * Dùng TOP(1) + ORDER BY id DESC để lấy chuyến mới nhất nếu có nhiều
     * (trường hợp tạo nhầm rồi tạo lại trong cùng ngày).
     *
     * @param routeId  ID tuyến đường
     * @param tripType "MORNING" hoặc "AFTERNOON"
     * @return Trip hôm nay, hoặc null nếu chưa tạo chuyến
     */
    public Trip getTodayTripByRoute(int routeId, String tripType) {
        String sql = "SELECT TOP(1) t.*, v.plate_number, r.route_name, u.full_name AS created_by_name,"
                + "(SELECT COUNT(*) FROM attendances a WHERE a.trip_id=t.id AND a.status='PRESENT') AS present_count,"
                + "(SELECT COUNT(*) FROM attendances a WHERE a.trip_id=t.id) AS total_count "
                + "FROM trips t JOIN vehicles v ON t.vehicle_id=v.id JOIN routes r ON t.route_id=r.id "
                + "LEFT JOIN users u ON t.created_by=u.id "
                + "WHERE t.route_id=? AND t.trip_date=CAST(GETDATE() AS DATE) AND t.trip_type=? "
                + "ORDER BY t.id DESC";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, routeId);
            ps.setString(2, tripType);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Đếm số chuyến hôm nay. Hiển thị trên Dashboard admin. */
    public int countToday() {
        String sql = "SELECT COUNT(*) FROM trips WHERE trip_date=CAST(GETDATE() AS DATE)";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ─── CÁC PHƯƠNG THỨC GHI DỮ LIỆU ────────────────────────────────────────

    /**
     * Tạo mới một chuyến xe.
     * Dùng kỹ thuật "OUTPUT INSERTED.id" của SQL Server để lấy ID ngay
     * sau khi INSERT mà không cần query thêm.
     *
     * Trạng thái ban đầu luôn là 'SCHEDULED' (cố định trong DAO, không lấy từ
     * ngoài).
     *
     * @return ID chuyến vừa tạo (> 0), hoặc -1 nếu thất bại
     */
    public int insert(Trip t) {
        String sql = "INSERT INTO trips (vehicle_id,route_id,trip_date,trip_type,status,notes,created_by) "
                + "OUTPUT INSERTED.id VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, t.getVehicleId());
            ps.setInt(2, t.getRouteId());
            ps.setDate(3, t.getTripDate());
            ps.setString(4, t.getTripType());
            ps.setString(5, "SCHEDULED"); // Luôn bắt đầu bằng SCHEDULED
            ps.setString(6, t.getNotes());
            ps.setInt(7, t.getCreatedBy());
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1); // Trả về ID vừa insert
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // -1 báo hiệu thất bại
    }

    /**
     * Đánh dấu chuyến đã khởi hành (SCHEDULED → DEPARTED).
     * Đồng thời ghi thời điểm khởi hành bằng GETDATE() của SQL Server.
     */
    public boolean depart(int tripId) {
        try {
            PreparedStatement ps = c.prepareStatement(
                    "UPDATE trips SET status='DEPARTED', departed_at=GETDATE() WHERE id=?");
            ps.setInt(1, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Đánh dấu chuyến đã hoàn thành (DEPARTED → COMPLETED).
     * Đồng thời ghi thời điểm đến bằng GETDATE().
     */
    public boolean complete(int tripId) {
        try {
            PreparedStatement ps = c.prepareStatement(
                    "UPDATE trips SET status='COMPLETED', arrived_at=GETDATE() WHERE id=?");
            ps.setInt(1, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật phần ghi chú (notes) của chuyến xe.
     * Monitor có thể ghi chú thêm trong lúc chuyến đang chạy.
     */
    public boolean updateNotes(int tripId, String notes) {
        try {
            PreparedStatement ps = c.prepareStatement("UPDATE trips SET notes=? WHERE id=?");
            ps.setString(1, notes);
            ps.setInt(2, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Hủy chuyến xe (CANCELLED).
     * Dùng bởi Admin khi cần hủy chuyến đã lên lịch.
     */
    public boolean cancel(int tripId) {
        try {
            PreparedStatement ps = c.prepareStatement(
                    "UPDATE trips SET status='CANCELLED' WHERE id=?");
            ps.setInt(1, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── PHƯƠNG THỨC NỘI BỘ ──────────────────────────────────────────────────

    /**
     * Ánh xạ một hàng ResultSet → đối tượng Trip.
     * Bao gồm cả các cột JOIN từ bảng khác (plate_number, route_name, ...).
     */
    private Trip mapRow(ResultSet rs) throws SQLException {
        Trip t = new Trip();
        t.setId(rs.getInt("id"));
        t.setVehicleId(rs.getInt("vehicle_id"));
        t.setPlateNumber(rs.getString("plate_number")); // từ JOIN vehicles
        t.setRouteId(rs.getInt("route_id"));
        t.setRouteName(rs.getString("route_name")); // từ JOIN routes
        t.setTripDate(rs.getDate("trip_date"));
        t.setTripType(rs.getString("trip_type")); // MORNING / AFTERNOON
        t.setStatus(rs.getString("status")); // SCHEDULED / DEPARTED / COMPLETED / CANCELLED
        t.setDepartedAt(rs.getTimestamp("departed_at"));
        t.setArrivedAt(rs.getTimestamp("arrived_at"));
        t.setNotes(rs.getString("notes"));
        t.setCreatedBy(rs.getInt("created_by"));
        t.setCreatedByName(rs.getString("created_by_name")); // từ JOIN users
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setPresentCount(rs.getInt("present_count")); // từ subquery đếm PRESENT
        t.setTotalCount(rs.getInt("total_count")); // từ subquery đếm tổng
        return t;
    }
}
