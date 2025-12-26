package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.*;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Candidate {

    @JsonProperty("candId")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final IntegerProperty candID = new SimpleIntegerProperty();

    private final StringProperty name = new SimpleStringProperty();

    private final StringProperty gender = new SimpleStringProperty();

    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();

    @JsonProperty("applyPositionId")
    private final IntegerProperty applyPositionID = new SimpleIntegerProperty();

    private final ObjectProperty<LocalDate> interviewDate = new SimpleObjectProperty<>();
    private final StringProperty result = new SimpleStringProperty();

    private final StringProperty applyPositionName = new SimpleStringProperty();

    public Candidate() {}


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