package com.gd.hrmsjavafxclient.model;

/**
 * 登录用户的聚合信息 Model
 * 🌟 核心修正：增加 empId, deptId, departmentName，用于员工和部门经理服务 API 调用。
 */
public class CurrentUserInfo {

    private final Integer userId;
    private final String username;
    private final Integer roleId;
    private final Integer empId; // 关键修正：新增员工ID
    private final String employeeName;
    private final String positionName;
    private final Integer deptId; // 新增部门ID
    private final String departmentName; // 新增部门名称

    public CurrentUserInfo(Integer userId, String username, Integer roleId, Integer empId, String employeeName, String positionName, Integer deptId, String departmentName) {
        this.userId = userId;
        this.username = username;
        this.roleId = roleId;
        this.empId = empId;
        // 如果查询失败，给个默认值
        this.employeeName = employeeName != null ? employeeName : "N/A";
        this.positionName = positionName != null ? positionName : "N/A";
        this.deptId = deptId;
        this.departmentName = departmentName != null ? departmentName : "N/A";
    }

    // 辅助方法：将 RoleID 转换为角色名
    public String getRoleName() {
        return switch (roleId) {
            case 1 -> "超级管理员";
            case 2 -> "人事管理员";
            case 3 -> "财务管理员";
            case 4 -> "部门经理";
            default -> "普通员工";
        };
    }

    // --- Getters ---
    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public Integer getRoleId() { return roleId; }
    public Integer getEmpId() { return empId; }
    public String getEmployeeName() { return employeeName; }
    public String getPositionName() { return positionName; }
    public Integer getDeptId() { return deptId; }
    public String getDepartmentName() { return departmentName; }

    @Override
    public String toString() {
        return "CurrentUserInfo{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", roleId=" + roleId +
                ", empId=" + empId +
                ", employeeName='" + employeeName + '\'' +
                ", positionName='" + positionName + '\'' +
                ", deptId=" + deptId +
                ", departmentName='" + departmentName + '\'' +
                '}';
    }
}