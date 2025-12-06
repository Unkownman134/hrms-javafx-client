package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

/**
 * 角色ID=1：超级管理员的主界面控制器
 */
public class AdminMainController implements MainController {

    @FXML
    private Text userInfoText; // 对应 FXML 中的 fx:id="userInfoText"

    /**
     * 实现 MainController 接口，接收并显示数据
     */
    @Override
    public void setUserInfo(CurrentUserInfo userInfo) {
        String info = String.format(
                "用户名: %s (UserID: %d)\n角色ID: %d (%s)\n员工姓名: %s\n职位名称: %s",
                userInfo.getUsername(),
                userInfo.getUserId(),
                userInfo.getRoleId(),
                userInfo.getRoleName(),
                userInfo.getEmployeeName(),
                userInfo.getPositionName()
        );
        userInfoText.setText(info);
    }
}