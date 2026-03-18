package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil – Tiện ích mã hóa và kiểm tra mật khẩu.
 *
 * Hiện tại dùng SHA-1. Tất cả phương thức đều là static → gọi trực tiếp
 * mà không cần khởi tạo đối tượng:
 *   String hash = PasswordUtil.sha1("mypassword");
 *   boolean ok  = PasswordUtil.verify("mypassword", hash);
 *
 * ⚠️ LƯU Ý BẢO MẬT:
 *   SHA-1 đã bị coi là yếu. Nếu nâng cấp bảo mật, hãy chuyển sang
 *   BCrypt (Spring Security) hoặc PBKDF2 với salt ngẫu nhiên.
 */
public class PasswordUtil {

    // Ngăn không cho khởi tạo lớp này (chỉ dùng các phương thức static)
    private PasswordUtil() {}

    /**
     * Mã hóa chuỗi đầu vào thành chuỗi SHA-1 dạng hex (40 ký tự).
     *
     * @param input Chuỗi cần mã hóa (thường là mật khẩu gốc)
     * @return Chuỗi SHA-1 hex, ví dụ: "da39a3ee5e6b4b0d..."
     * @throws RuntimeException nếu thuật toán SHA-1 không tồn tại (hiếm xảy ra)
     */
    public static String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] result = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Chuyển từng byte thành chuỗi hex 2 ký tự
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Không bao giờ xảy ra với SHA-1, nhưng vẫn cần bắt exception
            throw new RuntimeException("SHA-1 algorithm not available", e);
        }
    }

    /**
     * So sánh mật khẩu gốc với hash đã lưu trong CSDL.
     * Dùng để xác thực đăng nhập.
     *
     * @param raw  Mật khẩu người dùng gõ vào (chưa mã hóa)
     * @param hash Hash SHA-1 lưu trong database
     * @return true nếu mật khẩu khớp, false nếu sai
     */
    public static boolean verify(String raw, String hash) {
        if (raw == null || hash == null) return false; // tránh NullPointerException
        return sha1(raw).equals(hash);
    }
}
