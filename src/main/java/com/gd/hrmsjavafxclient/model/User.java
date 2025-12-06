package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// 导入 lombok.Data 或手动实现所有 Getter/Setter (这里假设你没有使用 lombok)

/**
 * 客户端的用户 Model
 * 对应后端 /api/users 接口返回的数据结构
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private Integer userId;
    private String username;
    private Integer roleId;
    private Integer empId;

    // 🌟 重点修正部分 (新增字段和方法) 🌟
    // 用于在客户端创建或更新时，发送明文密码给后端
    private String rawPassword;
    // 🌟 修正结束 🌟

    public User() {}

    // --- Getter 和 Setter ---

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    // 🌟 编译错误修复点 🌟
    public String getRawPassword() {
        return rawPassword;
    }

    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword; // 修复后的方法
    }
}