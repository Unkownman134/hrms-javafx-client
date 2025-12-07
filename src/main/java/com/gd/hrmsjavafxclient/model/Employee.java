package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

/**
 * R10: 员工档案 Model
 * 对应后端 T_Employee 表结构：EmpID, EmpName, Gender, Phone, Email, JoinDate, Status, DeptID, PosID, ManagerID
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {
    private Integer empId;
    private String empName;
    private String gender; // '男', '女'
    private String phone;
    private String email;
    private LocalDate joinDate; // 🌟 注意：这里使用 LocalDate 对应后端数据库的 Date 类型
    private String status; // '在职', '离职', '休假'
    private Integer deptId; // 关联部门ID
    private Integer posId; // 关联职位ID
    private Integer managerId; // 关联上级经理ID

    public Employee() {}

    // --- Getter 和 Setter ---

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public Integer getPosId() {
        return posId;
    }

    public void setPosId(Integer posId) {
        this.posId = posId;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }
}