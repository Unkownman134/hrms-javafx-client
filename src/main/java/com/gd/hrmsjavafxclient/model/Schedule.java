package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 排班记录 Model (t_employee_schedule)
 * 已经根据最新的 API 接口进行了字段对齐 ✨
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule {

    private final IntegerProperty scheduleId = new SimpleIntegerProperty();
    private final IntegerProperty empId = new SimpleIntegerProperty();
    private final IntegerProperty shiftRuleId = new SimpleIntegerProperty();

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

    private final StringProperty employeeName = new SimpleStringProperty();
    private final StringProperty shiftName = new SimpleStringProperty();

    // 🌟 修正：显式指定 LocalTime 的格式，防止后端解析 "2:0:0" 这种格式失败
    private final ObjectProperty<LocalTime> clockInTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> clockOutTime = new SimpleObjectProperty<>();

    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty note = new SimpleStringProperty();

    public Schedule() {}

    public Integer getScheduleId() { return scheduleId.get(); }
    public void setScheduleId(Integer id) { this.scheduleId.set(id); }

    @JsonProperty("empId")
    public Integer getEmpId() { return empId.get(); }
    @JsonProperty("empId")
    public void setEmpId(Integer id) { this.empId.set(id); }

    @JsonProperty("shiftRuleId")
    public Integer getShiftRuleId() { return shiftRuleId.get(); }
    @JsonProperty("shiftRuleId")
    public void setShiftRuleId(Integer id) { this.shiftRuleId.set(id); }

    @JsonProperty("scheduleDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate getScheduleDate() { return date.get(); }
    @JsonProperty("scheduleDate")
    public void setScheduleDate(LocalDate d) { this.date.set(d); }

    public String getShiftName() { return shiftName.get(); }
    public void setShiftName(String s) { this.shiftName.set(s); }

    public String getEmployeeName() { return employeeName.get(); }
    public void setEmployeeName(String s) { this.employeeName.set(s); }

    public String getStatus() { return status.get(); }
    public void setStatus(String s) { this.status.set(s); }

    // 🌟 增加对时间的 Getter/Setter 和格式化支持
    @JsonFormat(pattern = "H:m:s") // 兼容后端可能传来的非补零格式
    public LocalTime getClockInTime() { return clockInTime.get(); }
    public void setClockInTime(LocalTime time) { this.clockInTime.set(time); }

    @JsonFormat(pattern = "H:m:s")
    public LocalTime getClockOutTime() { return clockOutTime.get(); }
    public void setClockOutTime(LocalTime time) { this.clockOutTime.set(time); }

    // =========================================================
    // 2. Property Accessors (用于 JavaFX TableView 绑定)
    // =========================================================

    public IntegerProperty scheduleIdProperty() { return scheduleId; }
    public IntegerProperty empIdProperty() { return empId; }
    public IntegerProperty shiftRuleIdProperty() { return shiftRuleId; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public StringProperty employeeNameProperty() { return employeeName; }
    public StringProperty shiftNameProperty() { return shiftName; }
    public StringProperty statusProperty() { return status; }
}