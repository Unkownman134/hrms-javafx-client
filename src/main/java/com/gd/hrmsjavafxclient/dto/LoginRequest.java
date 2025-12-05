package com.gd.hrmsjavafxclient.dto;

// JavaFX 客户端需要自己的 DTO 来将对象转为 JSON
public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest() {} // 必须有无参构造函数

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Jackson 依赖 Getter 将对象转为 JSON
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}