package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.User;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import java.util.Optional;


public class ChangePasswordService {


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