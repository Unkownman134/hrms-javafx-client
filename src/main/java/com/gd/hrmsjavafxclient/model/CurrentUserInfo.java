package com.gd.hrmsjavafxclient.model;

public class CurrentUserInfo {

    private final Integer userId;
    private final String username;
    private final Integer roleId;
    private final String employeeName;
    private final String positionName;

    public CurrentUserInfo(Integer userId, String username, Integer roleId, String employeeName, String positionName) {
        this.userId = userId;
        this.username = username;
        this.roleId = roleId;
        // 如果查询失败，给个默认值
        this.employeeName = employeeName != null ? employeeName : "N/A";
        this.positionName = positionName != null ? positionName : "N/A";
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

    // Getter (只保留关键的，其他的请补充)
    public Integer getRoleId() { return roleId; }
    public String getUsername() { return username; }
    public String getEmployeeName() { return employeeName; }
    public String getPositionName() { return positionName; }
    public Integer getUserId() { return userId; }
}