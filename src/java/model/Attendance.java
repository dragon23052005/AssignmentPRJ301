package model;

import java.sql.Timestamp;

public class Attendance {
    private int id;
    private int tripId;
    private int studentId;
    private String studentName;
    private String studentCode;
    private String className;
    private String status; // PRESENT, ABSENT, NOTIFIED_ABSENT
    private Timestamp boardedAt;
    private int boardedStopId;
    private String boardedStopName;
    private Timestamp alightedAt;
    private int alightedStopId;
    private String alightedStopName;
    private String notes;
    private int updatedBy;
    private Timestamp updatedAt;
    private String parentPhone;

    public Attendance() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getBoardedAt() { return boardedAt; }
    public void setBoardedAt(Timestamp boardedAt) { this.boardedAt = boardedAt; }
    public int getBoardedStopId() { return boardedStopId; }
    public void setBoardedStopId(int boardedStopId) { this.boardedStopId = boardedStopId; }
    public String getBoardedStopName() { return boardedStopName; }
    public void setBoardedStopName(String boardedStopName) { this.boardedStopName = boardedStopName; }
    public Timestamp getAlightedAt() { return alightedAt; }
    public void setAlightedAt(Timestamp alightedAt) { this.alightedAt = alightedAt; }
    public int getAlightedStopId() { return alightedStopId; }
    public void setAlightedStopId(int alightedStopId) { this.alightedStopId = alightedStopId; }
    public String getAlightedStopName() { return alightedStopName; }
    public void setAlightedStopName(String alightedStopName) { this.alightedStopName = alightedStopName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(int updatedBy) { this.updatedBy = updatedBy; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

    public String getStatusDisplay() {
        return switch (status) {
            case "PRESENT" -> "Có mặt";
            case "ABSENT" -> "Vắng mặt";
            case "NOTIFIED_ABSENT" -> "Đã báo nghỉ";
            default -> status;
        };
    }

    public String getStatusClass() {
        return switch (status) {
            case "PRESENT" -> "status-present";
            case "ABSENT" -> "status-absent";
            case "NOTIFIED_ABSENT" -> "status-notified";
            default -> "";
        };
    }
}
