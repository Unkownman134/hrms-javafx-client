package com.gd.hrmsjavafxclient.model;


public class CurrentUserInfo {

    private final Integer userId;
    private final String username;
    private final Integer roleId;
    private final Integer empId;
    private final String employeeName;
    private final String positionName;
    private final Integer deptId;
    private final String departmentName;

    public CurrentUserInfo(Integer userId, String username, Integer roleId, Integer empId, String employeeName, String positionName, Integer deptId, String departmentName) {
        this.userId = userId;
        this.username = username;
        this.roleId = roleId;
        this.empId = empId;
        this.employeeName = employeeName != null ? employeeName : "N/A";
        this.positionName = positionName != null ? positionName : "N/A";
        this.deptId = deptId;
        this.departmentName = departmentName != null ? departmentName : "N/A";
    }

    public String getRoleName() {
        return switch (roleId) {
            case 1 -> "超级管理员";
            case 2 -> "人事管理员";
            case 3 -> "财务管理员";
            case 4 -> "部门经理";
            default -> "普通员工";
        };
    }

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