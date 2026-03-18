package model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private String role; // ADMIN, PARENT, MONITOR, DRIVER
    private boolean isActive;
    private Timestamp createdAt;

    public User() {}

    public User(int id, String username, String fullName, String email, String phone, String role, boolean isActive) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getRoleDisplay() {
        return switch (role) {
            case "ADMIN" -> "Quản trị viên";
            case "PARENT" -> "Phụ huynh";
            case "MONITOR" -> "Quản lý xe";
            case "DRIVER" -> "Lái xe";
            default -> role;
        };
    }

    public String getRoleBadgeClass() {
        return switch (role) {
            case "ADMIN" -> "badge-admin";
            case "PARENT" -> "badge-parent";
            case "MONITOR" -> "badge-monitor";
            case "DRIVER" -> "badge-driver";
            default -> "badge-default";
        };
    }
}
