package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Vehicle;

/**
 * VehicleDAO – Xử lý CSDL cho bảng {@code vehicles} (xe buýt trường).
 *
 * Mỗi xe được phân công:
 * - 1 lái xe (driver_id → users)
 * - 1 quản lý xe (monitor_id → users)
 * - 1 tuyến đường (route_id → routes)
 *
 * Các FK trên đều cho phép NULL (Left Join trong BASE_SQL) vì xe có thể
 * chưa được phân công hoặc chưa có tuyến.
 *
 * Xe không bị xóa cứng mà chỉ set is_active=0 (soft delete).
 */
public class VehicleDAO extends DBContext {

    /**
     * SQL nền JOIN bảng routes, driver (users) và monitor (users).
     * Dùng LEFT JOIN vì xe có thể chưa có driver/monitor/route.
     * Alias d = driver, m = monitor để phân biệt 2 lần JOIN users.
     */
    private static final String BASE_SQL = """
            SELECT v.*, r.route_name,
                   d.full_name AS driver_name,
                   m.full_name AS monitor_name
            FROM vehicles v
            LEFT JOIN routes r ON v.route_id = r.id
            LEFT JOIN users  d ON v.driver_id  = d.id
            LEFT JOIN users  m ON v.monitor_id = m.id
            """;

    // ─── CÁC PHƯƠNG THỨC ĐỌC DỮ LIỆU ────────────────────────────────────────

    /**
     * Lấy toàn bộ danh sách xe, sắp xếp theo biển số. Dùng cho trang quản lý xe.
     */
    public List<Vehicle> getAll() {
        List<Vehicle> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " ORDER BY v.plate_number");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm xe theo ID. Dùng khi cần load chi tiết để chỉnh sửa.
     *
     * @return Vehicle hoặc null nếu không tìm thấy
     */
    public Vehicle getById(int id) {
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " WHERE v.id=?");
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
     * Tìm xe được giao cho một lái xe (chỉ lấy xe đang active).
     * Lái xe dùng để tìm xe của mình khi đăng nhập.
     *
     * @return Vehicle của lái xe này, hoặc null nếu chưa được phân công
     */
    public Vehicle getByDriverId(int driverId) {
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " WHERE v.driver_id=? AND v.is_active=1");
            ps.setInt(1, driverId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm xe được giao cho một quản lý xe (chỉ lấy xe đang active).
     * Monitor dùng để tìm xe phụ trách của mình.
     *
     * @return Vehicle đầu tiên của monitor này, hoặc null nếu chưa được phân công
     */
    public Vehicle getByMonitorId(int monitorId) {
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " WHERE v.monitor_id=? AND v.is_active=1");
            ps.setInt(1, monitorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách TẤT CẢ xe được giao cho một quản lý xe.
     * Hỗ trợ trường hợp 1 Monitor quản lý nhiều xe.
     *
     * @return Danh sách Vehicle
     */
    public List<Vehicle> getListByMonitorId(int monitorId) {
        List<Vehicle> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " WHERE v.monitor_id=? AND v.is_active=1");
            ps.setInt(1, monitorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Đếm số xe đang active. Dùng trên Dashboard admin.
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM vehicles WHERE is_active=1";
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
     * Thêm xe mới.
     * Các FK (driverId, monitorId, routeId) nếu bằng 0 → lưu NULL vào DB
     * (dùng ps.setObject() thay vì ps.setInt() để hỗ trợ NULL).
     */
    public boolean insert(Vehicle v) {
        String sql = "INSERT INTO vehicles (plate_number,brand,model,capacity,driver_id,monitor_id,route_id) "
                + "VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, v.getPlateNumber());
            ps.setString(2, v.getBrand());
            ps.setString(3, v.getModel());
            ps.setInt(4, v.getCapacity());
            // setObject(index, value) khi value=null sẽ lưu NULL vào DB
            ps.setObject(5, v.getDriverId() > 0 ? v.getDriverId() : null);
            ps.setObject(6, v.getMonitorId() > 0 ? v.getMonitorId() : null);
            ps.setObject(7, v.getRouteId() > 0 ? v.getRouteId() : null);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin xe.
     * Bao gồm cả việc thay đổi phân công lái xe, monitor, tuyến và trạng thái
     * active.
     */
    public boolean update(Vehicle v) {
        String sql = "UPDATE vehicles SET plate_number=?,brand=?,model=?,capacity=?,"
                + "driver_id=?,monitor_id=?,route_id=?,is_active=? WHERE id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, v.getPlateNumber());
            ps.setString(2, v.getBrand());
            ps.setString(3, v.getModel());
            ps.setInt(4, v.getCapacity());
            ps.setObject(5, v.getDriverId() > 0 ? v.getDriverId() : null);
            ps.setObject(6, v.getMonitorId() > 0 ? v.getMonitorId() : null);
            ps.setObject(7, v.getRouteId() > 0 ? v.getRouteId() : null);
            ps.setInt(8, v.isActive() ? 1 : 0);
            ps.setInt(9, v.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── PHƯƠNG THỨC NỘI BỘ ──────────────────────────────────────────────────

    /**
     * Ánh xạ ResultSet → Vehicle (bao gồm route_name, driver_name, monitor_name từ
     * JOIN).
     */
    private Vehicle mapRow(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setId(rs.getInt("id"));
        v.setPlateNumber(rs.getString("plate_number"));
        v.setBrand(rs.getString("brand"));
        v.setModel(rs.getString("model"));
        v.setCapacity(rs.getInt("capacity"));
        v.setDriverId(rs.getInt("driver_id")); // 0 nếu NULL (JDBC convention)
        v.setDriverName(rs.getString("driver_name")); // từ JOIN users d
        v.setMonitorId(rs.getInt("monitor_id"));
        v.setMonitorName(rs.getString("monitor_name")); // từ JOIN users m
        v.setRouteId(rs.getInt("route_id"));
        v.setRouteName(rs.getString("route_name")); // từ JOIN routes
        v.setActive(rs.getInt("is_active") == 1);
        v.setCreatedAt(rs.getTimestamp("created_at"));
        return v;
    }
}
