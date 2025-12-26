package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AttendanceRecord {

    private Integer recordId;
    private Integer empId;

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> clockInTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> clockOutTime = new SimpleObjectProperty<>();

    private final StringProperty status = new SimpleStringProperty();
    private final IntegerProperty shiftRuleId = new SimpleIntegerProperty();
    private final StringProperty note = new SimpleStringProperty();

    private final StringProperty employeeName = new SimpleStringProperty();

    public AttendanceRecord() {}


    @JsonProperty("attDate")
    public void setDate(LocalDate date) { this.date.set(date); }
    public LocalDate getDate() { return date.get(); }
    public ObjectProperty<LocalDate> dateProperty() { return date; }

    @JsonProperty("clockInTime")
    public void setClockInTime(LocalTime clockInTime) { this.clockInTime.set(clockInTime); }
    public LocalTime getClockInTime() { return clockInTime.get(); }
    public ObjectProperty<LocalTime> clockInTimeProperty() { return clockInTime; }

    @JsonProperty("clockOutTime")
    public void setClockOutTime(LocalTime clockOutTime) { this.clockOutTime.set(clockOutTime); }
    public LocalTime getClockOutTime() { return clockOutTime.get(); }
    public ObjectProperty<LocalTime> clockOutTimeProperty() { return clockOutTime; }


    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }

    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    public Integer getShiftRuleId() { return shiftRuleId.get(); }
    public void setShiftRuleId(Integer shiftRuleId) { this.shiftRuleId.set(shiftRuleId); }
    public IntegerProperty shiftRuleIdProperty() { return shiftRuleId; }

    public String getNote() { return note.get(); }
    public void setNote(String note) { this.note.set(note); }
    public StringProperty noteProperty() { return note; }

    public String getEmployeeName() { return employeeName.get(); }
    public void setEmployeeName(String employeeName) { this.employeeName.set(employeeName); }
    public StringProperty employeeNameProperty() { return employeeName; }

    @Override
    public String toString() {
        return "AttendanceRecord{" +
                "empId=" + empId +
                ", date=" + date.get() +
                ", clockInTime=" + clockInTime.get() +
                ", clockOutTime=" + clockOutTime.get() +
                ", status='" + status.get() + '\'' +
                '}';
    }
}