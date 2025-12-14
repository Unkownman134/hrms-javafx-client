package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 考勤记录 Model (t_attendance_record)
 * 使用 Property 类支持 JavaFX TableView 绑定。
 * 🌟 最终修正：确保所有字段、Getter、Setter 和 Property 访问器都正确且一致。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttendanceRecord {

    // --- 核心字段 ---
    private Integer recordId;
    private Integer empId;

    // 修正后的字段名称
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> clockInTime = new SimpleObjectProperty<>();
    // 💖 修正点：将 clockOutOutTime 修正为 clockOutTime
    private final ObjectProperty<LocalTime> clockOutTime = new SimpleObjectProperty<>();

    private final StringProperty status = new SimpleStringProperty();
    private final IntegerProperty shiftRuleId = new SimpleIntegerProperty();
    private final StringProperty note = new SimpleStringProperty();

    // --- 客户端辅助字段 ---
    private final StringProperty employeeName = new SimpleStringProperty();

    public AttendanceRecord() {}

    // --- Getter/Setter (Jackson Deserialization) 和 Property Accessors ---

    // Date (attDate)
    @JsonProperty("attDate")
    public void setDate(LocalDate date) { this.date.set(date); }
    public LocalDate getDate() { return date.get(); }
    public ObjectProperty<LocalDate> dateProperty() { return date; }

    // ClockInTime (clockInTime)
    @JsonProperty("clockInTime")
    public void setClockInTime(LocalTime clockInTime) { this.clockInTime.set(clockInTime); }
    public LocalTime getClockInTime() { return clockInTime.get(); }
    public ObjectProperty<LocalTime> clockInTimeProperty() { return clockInTime; }

    // ClockOutTime (clockOutTime) 🌟 全部修正为 clockOutTime
    @JsonProperty("clockOutTime")
    public void setClockOutTime(LocalTime clockOutTime) { this.clockOutTime.set(clockOutTime); }
    public LocalTime getClockOutTime() { return clockOutTime.get(); }
    public ObjectProperty<LocalTime> clockOutTimeProperty() { return clockOutTime; } // 🌟 修正 Property Name

    // --- 其他字段 Getter/Setter ---

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
                ", clockInTime=" + clockInTime.get() + // 使用修正后的字段
                ", clockOutTime=" + clockOutTime.get() + // 使用修正后的字段
                ", status='" + status.get() + '\'' +
                '}';
    }
}