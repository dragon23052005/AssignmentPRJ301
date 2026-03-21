package model;

import java.sql.Timestamp;
import java.sql.Time;

public class Route {
    private int id;
    private String routeName;
    private String routeCode;
    private String description;
    private Time morningDeparture;
    private Time afternoonDeparture;
    private boolean isActive;
    private Timestamp createdAt;
    private int stopCount; // extra field for display
    private int studentCount; // extra field for display

    public Route() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getRouteCode() { return routeCode; }
    public void setRouteCode(String routeCode) { this.routeCode = routeCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Time getMorningDeparture() { return morningDeparture; }
    public void setMorningDeparture(Time morningDeparture) { this.morningDeparture = morningDeparture; }
    public Time getAfternoonDeparture() { return afternoonDeparture; }
    public void setAfternoonDeparture(Time afternoonDeparture) { this.afternoonDeparture = afternoonDeparture; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public int getStopCount() { return stopCount; }
    public void setStopCount(int stopCount) { this.stopCount = stopCount; }
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
}
