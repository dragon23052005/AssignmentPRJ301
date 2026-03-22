package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.VehicleReport;

/**
 * VehicleReportDAO – Xử lý CSDL cho bảng {@code vehicle_reports} (báo cáo sự cố xe).
 *
 * Tài xế (DRIVER) báo cáo sự cố kỹ thuật → Monitor/Admin xem xét và cập nhật trạng thái.
 *
 * Vòng đời của một báo cáo:
 *   OPEN → IN_PROGRESS → RESOLVED
 *
 * Mức độ nghiêm trọng (severity): LOW | MEDIUM | HIGH | CRITICAL
 * Loại sự cố (issueType): MECHANICAL | TIRE | BRAKE | ENGINE | ELECTRICAL | BODY
 */
public class VehicleReportDAO extends DBContext {

    /**
     * SQL nền JOIN với bảng vehicles và users để lấy biển số và tên người báo cáo.
     */
    private final String BASE_SQL = """
        SELECT vr.*, v.plate_number, u.full_name AS reported_by_name
        FROM vehicle_reports vr
        JOIN vehicles v ON vr.vehicle_id=v.id
        JOIN users u ON vr.reported_by=u.id
        """;

    // ─── CÁC PHƯƠNG THỨC ĐỌC DỮ LIỆU ────────────────────────────────────────

    /**
     * Lấy tất cả báo cáo của một xe, mới nhất trước.
     * Dùng trên trang Dashboard lái xe để xem lịch sử báo cáo xe của mình.
     *
     * @param vehicleId ID xe cần xem
     */
    public List<VehicleReport> getByVehicle(int vehicleId) {
        List<VehicleReport> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(
                BASE_SQL + " WHERE vr.vehicle_id=? ORDER BY vr.created_at DESC");
            ps.setInt(1, vehicleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy toàn bộ báo cáo sự cố, mới nhất trước.
     * Dùng cho Admin xem tổng quan.
     */
    public List<VehicleReport> getAll() {
        List<VehicleReport> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(BASE_SQL + " ORDER BY vr.created_at DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy các báo cáo chưa giải quyết (OPEN hoặc IN_PROGRESS).
     * Dùng trên Dashboard admin để nắm bắt các sự cố cần xử lý.
     */
    public List<VehicleReport> getOpen() {
        List<VehicleReport> list = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement(
                BASE_SQL + " WHERE vr.status IN ('OPEN','IN_PROGRESS') ORDER BY vr.created_at DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Đếm số báo cáo chưa giải quyết (OPEN + IN_PROGRESS).
     * Hiển thị badge cảnh báo trên Dashboard admin.
     */
    public int countOpen() {
        String sql = "SELECT COUNT(*) FROM vehicle_reports WHERE status IN ('OPEN','IN_PROGRESS')";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ─── CÁC PHƯƠNG THỨC GHI DỮ LIỆU ────────────────────────────────────────

    /**
     * Tạo mới báo cáo sự cố. Lái xe gọi phương thức này khi phát hiện vấn đề.
     * Trạng thái mặc định sẽ là OPEN (cài trong DB default hoặc có thể đặt tường minh).
     */
    public boolean insert(VehicleReport r) {
        String sql = "INSERT INTO vehicle_reports (vehicle_id,reported_by,report_date,issue_type,severity,description) "
                   + "VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, r.getVehicleId());
            ps.setInt(2, r.getReportedBy());
            ps.setDate(3, r.getReportDate());
            ps.setString(4, r.getIssueType());
            ps.setString(5, r.getSeverity());
            ps.setString(6, r.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Cập nhật trạng thái xử lý báo cáo.
     *
     * Mẹo SQL: Nếu status là "RESOLVED" → tự động ghi thời điểm resolved_at=GETDATE().
     * Điều này tránh phải dùng 2 câu SQL riêng biệt cho 2 trường hợp.
     *
     * @param id     ID báo cáo cần cập nhật
     * @param status Trạng thái mới: "OPEN" | "IN_PROGRESS" | "RESOLVED"
     */
    public boolean updateStatus(int id, String status) {
        // Nếu RESOLVED → thêm cột resolved_at vào câu UPDATE
        String extraSet = "RESOLVED".equals(status) ? ",resolved_at=GETDATE()" : "";
        String sql = "UPDATE vehicle_reports SET status=?" + extraSet + " WHERE id=?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ─── PHƯƠNG THỨC NỘI BỘ ──────────────────────────────────────────────────

    /** Ánh xạ ResultSet → VehicleReport (bao gồm plate_number và reported_by_name từ JOIN). */
    private VehicleReport mapRow(ResultSet rs) throws SQLException {
        VehicleReport r = new VehicleReport();
        r.setId(rs.getInt("id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setPlateNumber(rs.getString("plate_number"));         // từ JOIN vehicles
        r.setReportedBy(rs.getInt("reported_by"));
        r.setReportedByName(rs.getString("reported_by_name")); // từ JOIN users
        r.setReportDate(rs.getDate("report_date"));
        r.setIssueType(rs.getString("issue_type"));
        r.setSeverity(rs.getString("severity"));
        r.setDescription(rs.getString("description"));
        r.setStatus(rs.getString("status"));                   // OPEN / IN_PROGRESS / RESOLVED
        r.setResolvedAt(rs.getTimestamp("resolved_at"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }
}
