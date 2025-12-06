package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 🌟 修复 Jackson "passwordHash" 报错！
public class User {
    private Integer userId;
    private String username;
    private Integer roleId; // 权限判断依据
    private Integer empId;  // 员工信息查询依据

    // Lombok 不在 JavaFX 客户端中，需要手动写 Getter/Setter/Constructor
    public User() {}

    // Getter and Setter
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }
}