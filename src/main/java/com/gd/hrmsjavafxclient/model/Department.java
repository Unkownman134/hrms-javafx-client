package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * R1: 部门信息 Model
 * 对应后端 T_Department 表结构：DeptID, DeptName, DeptDesc
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Department {
    private Integer deptId;
    private String deptName;
    private String deptDesc;

    public Department() {}

    // --- Getter 和 Setter ---

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptDesc() {
        return deptDesc;
    }

    public void setDeptDesc(String deptDesc) {
        this.deptDesc = deptDesc;
    }

    // 重写 toString，方便在调试或组合框中使用
    @Override
    public String toString() {
        return deptName + " (ID:" + deptId + ")";
    }
}