package com.gd.hrmsjavafxclient.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 审批流程配置实体类
 */
public class ApprovalConfig {
    private final IntegerProperty configId = new SimpleIntegerProperty();
    private final StringProperty processType = new SimpleStringProperty();
    private final IntegerProperty deptId = new SimpleIntegerProperty();
    private final IntegerProperty approverPositionId = new SimpleIntegerProperty();

    private final StringProperty deptName = new SimpleStringProperty();
    private final StringProperty positionName = new SimpleStringProperty();

    public ApprovalConfig() {}

    public int getConfigId() { return configId.get(); }
    public IntegerProperty configIdProperty() { return configId; }
    public void setConfigId(int configId) { this.configId.set(configId); }

    public String getProcessType() { return processType.get(); }
    public StringProperty processTypeProperty() { return processType; }
    public void setProcessType(String processType) { this.processType.set(processType); }

    public int getDeptId() { return deptId.get(); }
    public IntegerProperty deptIdProperty() { return deptId; }
    public void setDeptId(int deptId) { this.deptId.set(deptId); }

    public int getApproverPositionId() { return approverPositionId.get(); }
    public IntegerProperty approverPositionIdProperty() { return approverPositionId; }
    public void setApproverPositionId(int approverPositionId) { this.approverPositionId.set(approverPositionId); }

    public String getDeptName() { return deptName.get(); }
    public StringProperty deptNameProperty() { return deptName; }
    public void setDeptName(String deptName) { this.deptName.set(deptName); }

    public String getPositionName() { return positionName.get(); }
    public StringProperty positionNameProperty() { return positionName; }
    public void setPositionName(String positionName) { this.positionName.set(positionName); }
}