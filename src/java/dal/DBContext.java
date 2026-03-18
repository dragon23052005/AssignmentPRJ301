package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBContext – Lớp nền tảng quản lý kết nối CSDL.
 *
 * Mọi DAO (Data Access Object) đều kế thừa lớp này để dùng chung
 * đối tượng Connection {@code c}.
 *
 * ⚠️ LƯU Ý BUG TIỀM ẨN:
 *   - Mỗi lần tạo new XxxDAO() sẽ mở 1 kết nối mới → dễ leak connection
 *     nếu quên gọi closeConnection() sau khi dùng xong.
 *   - Nên dùng Connection Pool (HikariCP / DBCP) trong production.
 *   - Password đang để plain-text trong code → nên chuyển sang
 *     context.xml hoặc file cấu hình bên ngoài.
 */
public class DBContext {

    /** Đối tượng kết nối CSDL, được dùng bởi tất cả các DAO con */
    protected Connection c;

    // ─── Cấu hình kết nối ────────────────────────────────────────────────────
    private static final String DB_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=SchoolBusDB;"
          + "encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "123"; // ← đổi đúng mật khẩu SQL Server của bạn

    /**
     * Hàm khởi tạo: tự động tạo kết nối đến SchoolBusDB khi new DAO().
     * Nếu kết nối thất bại, {@code c} sẽ là null → các DAO phải kiểm tra null.
     */
    public DBContext() {
        try {
            // Nạp driver JDBC cho SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            // In log để dễ debug; trong production nên dùng Logger
            System.err.println("[DBContext] Lỗi kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Đóng kết nối an toàn. Gọi phương thức này sau khi dùng xong DAO
     * để tránh rò rỉ tài nguyên (connection leak).
     */
    public void closeConnection() {
        try {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Kiểm tra kết nối nhanh khi chạy trực tiếp từ IDE */
    public static void main(String[] args) {
        DBContext db = new DBContext();
        if (db.c != null) {
            System.out.println("✅ Kết nối SchoolBusDB thành công!");
        } else {
            System.out.println("❌ Kết nối thất bại! Kiểm tra lại password và tên database.");
        }
        db.closeConnection();
    }
}
