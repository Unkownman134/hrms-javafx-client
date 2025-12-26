package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.model.CurrentUserInfo;

public interface MainController {
    /**
     * 设置并显示登录用户的所有信息和认证Token，所有主界面的Controller都需要实现此方法
     * @param userInfo 登录用户的聚合信息
     * @param authToken 用户的认证Token
     */
    void setUserInfo(CurrentUserInfo userInfo, String authToken);
}