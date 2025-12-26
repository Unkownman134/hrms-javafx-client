package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.*;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftRule {

    private final IntegerProperty ruleId = new SimpleIntegerProperty();
    private final StringProperty ruleName = new SimpleStringProperty();
    private final StringProperty workStartTime = new SimpleStringProperty();
    private final StringProperty workEndTime = new SimpleStringProperty();
    private final IntegerProperty lateToleranceMin = new SimpleIntegerProperty();

    public ShiftRule() {}


    public Integer getRuleId() { return ruleId.get(); }
    public void setRuleId(Integer ruleId) { this.ruleId.set(ruleId); }

    public String getRuleName() { return ruleName.get(); }
    public void setRuleName(String ruleName) { this.ruleName.set(ruleName); }

    public String getWorkStartTime() { return workStartTime.get(); }
    public void setWorkStartTime(String workStartTime) { this.workStartTime.set(workStartTime); }

    public String getWorkEndTime() { return workEndTime.get(); }
    public void setWorkEndTime(String workEndTime) { this.workEndTime.set(workEndTime); }

    public Integer getLateToleranceMin() { return lateToleranceMin.get(); }
    public void setLateToleranceMin(Integer lateToleranceMin) { this.lateToleranceMin.set(lateToleranceMin); }


    public IntegerProperty ruleIdProperty() { return ruleId; }
    public StringProperty ruleNameProperty() { return ruleName; }
    public StringProperty workStartTimeProperty() { return workStartTime; }
    public StringProperty workEndTimeProperty() { return workEndTime; }
    public IntegerProperty lateToleranceMinProperty() { return lateToleranceMin; }
}