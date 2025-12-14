package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty; // 确保导入这个包
import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * 对应数据库 t_candidate 表的实体类 (招聘流程中候选人信息)。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Candidate {

    // 🌟 修正点：明确指定 JSON 字段名，避免 ID 无法反序列化而默认为 0
    // 假设后端返回的字段名为 "candId"
    @JsonProperty("candId")
    private final IntegerProperty candID = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty gender = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();

    // 必须与后端 API 的 JSON 字段名匹配
    @JsonProperty("applyPositionId")
    private final IntegerProperty applyPositionID = new SimpleIntegerProperty();

    private final ObjectProperty<LocalDate> interviewDate = new SimpleObjectProperty<>();
    private final StringProperty result = new SimpleStringProperty();

    // 辅助属性：用于在表格中显示职位名称，不参与 API 传输 (忽略 @JsonProperty)
    private final StringProperty applyPositionName = new SimpleStringProperty();

    // 构造函数
    public Candidate() {}

    // --- 属性 Getter, Setter, Property ---

    public int getCandID() { return candID.get(); }
    public IntegerProperty candIDProperty() { return candID; }
    public void setCandID(int candID) { this.candID.set(candID); }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name); }

    public String getGender() { return gender.get(); }
    public StringProperty genderProperty() { return gender; }
    public void setGender(String gender) { this.gender.set(gender); }

    public String getPhone() { return phone.get(); }
    public StringProperty phoneProperty() { return phone; }
    public void setPhone(String phone) { this.phone.set(phone); }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }
    public void setEmail(String email) { this.email.set(email); }

    // 使用 @JsonProperty 确保序列化和反序列化时与后端字段名一致
    public int getApplyPositionId() { return applyPositionID.get(); }
    public IntegerProperty applyPositionIDProperty() { return applyPositionID; }
    public void setApplyPositionId(int applyPositionID) { this.applyPositionID.set(applyPositionID); }

    public LocalDate getInterviewDate() { return interviewDate.get(); }
    public ObjectProperty<LocalDate> interviewDateProperty() { return interviewDate; }
    public void setInterviewDate(LocalDate interviewDate) { this.interviewDate.set(interviewDate); }

    public String getResult() { return result.get(); }
    public StringProperty resultProperty() { return result; }
    public void setResult(String result) { this.result.set(result); }

    public String getApplyPositionName() { return applyPositionName.get(); }
    public StringProperty applyPositionNameProperty() { return applyPositionName; }
    public void setApplyPositionName(String applyPositionName) { this.applyPositionName.set(applyPositionName); }
}