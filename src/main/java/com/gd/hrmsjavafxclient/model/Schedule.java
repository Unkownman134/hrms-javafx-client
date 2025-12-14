package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 排班记录 Model (t_employee_schedule)
 * 🚨 关键修正：@JsonProperty 字段名必须与后端 JSON 完全一致，尤其是 "scheduleDate"。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule {

    // --- 新增/修正：适配后端 JSON 字段 ---
    private Integer scheduleId;   // JSON: "scheduleId"
    private Integer empId;        // JSON: "empId"
    private Integer shiftRuleId;  // JSON: "shiftRuleId"

    // --- JavaFX Property 字段 ---
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final StringProperty employeeName = new SimpleStringProperty(); // 客户端填充
    private final StringProperty shiftName = new SimpleStringProperty();
    private final ObjectProperty<LocalTime> clockInTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> clockOutTime = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty note = new SimpleStringProperty();

    public Schedule() {
    }

    // =========================================================
    // 1. JavaBean Accessors (Getter/Setter for Jackson)
    // =========================================================

    // ScheduleId
    public Integer getScheduleId() { return scheduleId; }
    public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }

    // EmpId
    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }

    // ShiftRuleId
    public Integer getShiftRuleId() { return shiftRuleId; }
    public void setShiftRuleId(Integer shiftRuleId) { this.shiftRuleId = shiftRuleId; }

    // Date (匹配后端 JSON "scheduleDate"!)
    public LocalDate getDate() { return date.get(); }
    @JsonProperty("scheduleDate") // 👈 🚨 最终修正：使用后端字段名
    public void setDate(LocalDate date) { this.date.set(date); }

    // EmployeeName (Controller 填充，无需 @JsonProperty)
    public String getEmployeeName() { return employeeName.get(); }
    public void setEmployeeName(String employeeName) { this.employeeName.set(employeeName); }

    // 以下字段 API 未返回，Jackson 默认不会调用 Setter，所以不需要 @JsonProperty 注解。
    // 但是我们需要保留 Setter/Getter 供未来手动填充！

    public String getShiftName() { return shiftName.get(); }
    public void setShiftName(String shiftName) { this.shiftName.set(shiftName); }

    public LocalTime getClockInTime() { return clockInTime.get(); }
    public void setClockInTime(LocalTime clockInTime) { this.clockInTime.set(clockInTime); }

    public LocalTime getClockOutTime() { return clockOutTime.get(); }
    public void setClockOutTime(LocalTime clockOutTime) { this.clockOutTime.set(clockOutTime); }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public String getNote() { return note.get(); }
    public void setNote(String note) { this.note.set(note); }

    // =========================================================
    // 2. Property Accessors (用于 JavaFX TableView 绑定)
    // =========================================================

    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public StringProperty employeeNameProperty() { return employeeName; }
    public StringProperty shiftNameProperty() { return shiftName; }
    public ObjectProperty<LocalTime> clockInTimeProperty() { return clockInTime; }
    public ObjectProperty<LocalTime> clockOutTimeProperty() { return clockOutTime; }
    public StringProperty statusProperty() { return status; }
    public StringProperty noteProperty() { return note; }
}