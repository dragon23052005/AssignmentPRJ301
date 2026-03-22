package model;

import java.sql.Date;
import java.sql.Timestamp;

public class Trip {
    private int id;
    private int vehicleId;
    private String plateNumber;
    private int routeId;
    private String routeName;
    private Date tripDate;
    private String tripType; // MORNING, AFTERNOON
    private String status; // SCHEDULED, DEPARTED, COMPLETED, CANCELLED
    private Timestamp departedAt;
    private Timestamp arrivedAt;
    private String notes;
    private int createdBy;
    private String createdByName;
    private Timestamp createdAt;
    private int presentCount;
    private int totalCount;

    public Trip() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public Date getTripDate() { return tripDate; }
    public void setTripDate(Date tripDate) { this.tripDate = tripDate; }
    public String getTripType() { return tripType; }
    public void setTripType(String tripType) { this.tripType = tripType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getDepartedAt() { return departedAt; }
    public void setDepartedAt(Timestamp departedAt) { this.departedAt = departedAt; }
    public Timestamp getArrivedAt() { return arrivedAt; }
    public void setArrivedAt(Timestamp arrivedAt) { this.arrivedAt = arrivedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public String getTripTypeDisplay() {
        return "MORNING".equals(tripType) ? "Buổi sáng" : "Buổi chiều";
    }

    public String getStatusDisplay() {
        return switch (status) {
            case "SCHEDULED" -> "Chưa khởi hành";
            case "DEPARTED" -> "Đã khởi hành";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    public String getStatusClass() {
        return switch (status) {
            case "SCHEDULED" -> "status-scheduled";
            case "DEPARTED" -> "status-departed";
            case "COMPLETED" -> "status-completed";
            case "CANCELLED" -> "status-cancelled";
            default -> "";
        };
    }
}
