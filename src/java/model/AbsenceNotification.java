package model;

import java.sql.Date;
import java.sql.Timestamp;

public class AbsenceNotification {
    private int id;
    private int studentId;
    private String studentName;
    private String studentCode;
    private String className;
    private Date absenceDate;
    private String reason;
    private Timestamp notifiedAt;
    private String status; // PENDING, ACKNOWLEDGED
    private int acknowledgedBy;
    private String acknowledgedByName;
    private Timestamp acknowledgedAt;
    private String routeName;

    public AbsenceNotification() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Date getAbsenceDate() { return absenceDate; }
    public void setAbsenceDate(Date absenceDate) { this.absenceDate = absenceDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Timestamp getNotifiedAt() { return notifiedAt; }
    public void setNotifiedAt(Timestamp notifiedAt) { this.notifiedAt = notifiedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(int acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
    public String getAcknowledgedByName() { return acknowledgedByName; }
    public void setAcknowledgedByName(String acknowledgedByName) { this.acknowledgedByName = acknowledgedByName; }
    public Timestamp getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Timestamp acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public String getStatusDisplay() {
        return "PENDING".equals(status) ? "Chờ xác nhận" : "Đã xác nhận";
    }
}
