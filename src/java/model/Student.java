package model;

import java.sql.Date;
import java.sql.Timestamp;

public class Student {
    private int id;
    private String fullName;
    private String studentCode;
    private String className;
    private Date dateOfBirth;
    private int parentId;
    private String parentName;
    private String parentPhone;
    private Timestamp createdAt;

    // Registration info
    private int registrationId;
    private int routeId;
    private String routeName;
    private int stopId;
    private String stopName;
    private boolean registrationActive;

    public Student() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public int getRegistrationId() { return registrationId; }
    public void setRegistrationId(int registrationId) { this.registrationId = registrationId; }
    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public int getStopId() { return stopId; }
    public void setStopId(int stopId) { this.stopId = stopId; }
    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }
    public boolean isRegistrationActive() { return registrationActive; }
    public void setRegistrationActive(boolean registrationActive) { this.registrationActive = registrationActive; }

    private java.sql.Time estimatedMorningTime;
    private java.sql.Time estimatedAfternoonTime;

    public java.sql.Time getEstimatedMorningTime() { return estimatedMorningTime; }
    public void setEstimatedMorningTime(java.sql.Time estimatedMorningTime) { this.estimatedMorningTime = estimatedMorningTime; }
    public java.sql.Time getEstimatedAfternoonTime() { return estimatedAfternoonTime; }
    public void setEstimatedAfternoonTime(java.sql.Time estimatedAfternoonTime) { this.estimatedAfternoonTime = estimatedAfternoonTime; }
}
