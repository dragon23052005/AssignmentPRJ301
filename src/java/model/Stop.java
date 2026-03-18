package model;

import java.sql.Time;
import java.sql.Timestamp;

public class Stop {
    private int id;
    private int routeId;
    private String routeName;
    private String stopName;
    private int stopOrder;
    private String address;
    private Time estimatedMorningTime;
    private Time estimatedAfternoonTime;
    private Timestamp createdAt;

    public Stop() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }
    public int getStopOrder() { return stopOrder; }
    public void setStopOrder(int stopOrder) { this.stopOrder = stopOrder; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Time getEstimatedMorningTime() { return estimatedMorningTime; }
    public void setEstimatedMorningTime(Time estimatedMorningTime) { this.estimatedMorningTime = estimatedMorningTime; }
    public Time getEstimatedAfternoonTime() { return estimatedAfternoonTime; }
    public void setEstimatedAfternoonTime(Time estimatedAfternoonTime) { this.estimatedAfternoonTime = estimatedAfternoonTime; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
