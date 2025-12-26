package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.User;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import java.util.Optional;

/**
 * 员工密码修改服务
 * 严格遵守不修改原有 ServiceUtil 的原则，直接调用通用 sendRequest 方法。
 */
public class ChangePasswordService {

    /**
     * 更新用户密码 (对应 API: PUT /api/users/{UserID})
     * @param user 包含 UserId 和 rawPassword 的对象
     * @param authToken 登录令牌
     * @return 是否修改成功
     * @throws Exception 抛出异常由 Controller 捕捉
     */
    public boolean updatePassword(User user, String authToken) throws Exception {
        String path = "/users/" + user.getUserId();

        System.out.println("ChangePasswordService: 准备调用通用接口更新用户密码...");

        Optional<User> result = ServiceUtil.sendRequest(
                path,
                authToken,
                user,
                "PUT",
                new TypeReference<User>() {}
        );

        return true;
    }
}