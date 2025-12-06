package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {
    private Integer empId;
    private String empName; // 🌟 需要获取
    private Integer posId;  // 🌟 需要获取

    public Employee() {}

    // Getter and Setter
    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }
    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }
    public Integer getPosId() { return posId; }
    public void setPosId(Integer posId) { this.posId = posId; }
}