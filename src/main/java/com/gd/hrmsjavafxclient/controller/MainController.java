package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.model.CurrentUserInfo;

public interface MainController {

    void setUserInfo(CurrentUserInfo userInfo, String authToken);
}