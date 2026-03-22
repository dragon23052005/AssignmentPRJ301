package model;

import java.sql.Date;
import java.sql.Timestamp;

public class VehicleReport {
    private int id;
    private int vehicleId;
    private String plateNumber;
    private int reportedBy;
    private String reportedByName;
    private Date reportDate;
    private String issueType;
    private String severity;
    private String description;
    private String status; // OPEN, IN_PROGRESS, RESOLVED
    private Timestamp resolvedAt;
    private Timestamp createdAt;

    public VehicleReport() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public int getReportedBy() { return reportedBy; }
    public void setReportedBy(int reportedBy) { this.reportedBy = reportedBy; }
    public String getReportedByName() { return reportedByName; }
    public void setReportedByName(String reportedByName) { this.reportedByName = reportedByName; }
    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getIssueTypeDisplay() {
        return switch (issueType) {
            case "MECHANICAL" -> "Cơ học";
            case "TIRE" -> "Lốp xe";
            case "BRAKE" -> "Phanh";
            case "ENGINE" -> "Động cơ";
            case "ELECTRICAL" -> "Điện";
            case "BODY" -> "Thân xe";
            default -> "Khác";
        };
    }

    public String getSeverityDisplay() {
        return switch (severity) {
            case "LOW" -> "Thấp";
            case "MEDIUM" -> "Trung bình";
            case "HIGH" -> "Cao";
            case "CRITICAL" -> "Nghiêm trọng";
            default -> severity;
        };
    }

    public String getStatusDisplay() {
        return switch (status) {
            case "OPEN" -> "Mới báo";
            case "IN_PROGRESS" -> "Đang xử lý";
            case "RESOLVED" -> "Đã giải quyết";
            default -> status;
        };
    }

    public String getSeverityClass() {
        return switch (severity) {
            case "LOW" -> "severity-low";
            case "MEDIUM" -> "severity-medium";
            case "HIGH" -> "severity-high";
            case "CRITICAL" -> "severity-critical";
            default -> "";
        };
    }
}
