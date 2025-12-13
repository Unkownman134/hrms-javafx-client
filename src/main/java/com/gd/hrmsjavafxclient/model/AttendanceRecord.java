package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 考勤记录 Model (对应 GET /api/attendance/{EmpID})
 * 使用 Property 类支持 JavaFX TableView 绑定。
 */
@JsonIgnoreProperties(ignoreUnknown = true) // 👈 关键修正：忽略 JSON 中多余的字段（如 recordId）
public class AttendanceRecord {
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> clockInTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> clockOutTime = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty(); // 状态：正常/迟到/早退/缺勤
    private final StringProperty note = new SimpleStringProperty();

    public AttendanceRecord() {}

    // --- Getters and Setters for Jackson (API deserialization) ---
    public LocalDate getDate() { return date.get(); }
    public void setDate(LocalDate date) { this.date.set(date); }

    public LocalTime getClockInTime() { return clockInTime.get(); }
    public void setClockInTime(LocalTime clockInTime) { this.clockInTime.set(clockInTime); }

    public LocalTime getClockOutTime() { return clockOutTime.get(); }
    public void setClockOutTime(LocalTime clockOutTime) { this.clockOutTime.set(clockOutTime); }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public String getNote() { return note.get(); }
    public void setNote(String note) { this.note.set(note); }

    // --- Property Getters for JavaFX TableView ---
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public ObjectProperty<LocalTime> clockInTimeProperty() { return clockInTime; }
    public ObjectProperty<LocalTime> clockOutTimeProperty() { return clockOutTime; }
    public StringProperty statusProperty() { return status; }
    public StringProperty noteProperty() { return note; }
}