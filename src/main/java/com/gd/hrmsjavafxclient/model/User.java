package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// 这个类用来接收后端 /api/auth/login 成功后返回的 JSON
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private Integer userId; // 对应后端的 UserID
    private String username;
    private Integer roleId; // 🌟 关键：对应后端的 RoleID (Integer)

    // 还需要 EmpID 等，这里只列出关键字段
    private Integer empId;

    public User() {} // Jackson 反序列化需要无参构造函数

    // 必须有 Getter 和 Setter 才能让 Jackson 正常解析/构建对象

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }
}