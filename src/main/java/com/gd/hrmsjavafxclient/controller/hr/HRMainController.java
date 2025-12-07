package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.MainController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

/**
 * 角色ID=2：人事管理员主界面控制器
 */
public class HRMainController implements MainController {

    @FXML
    private Text userInfoText;
    @FXML
    private Label roleTitle;

    @Override
    public void setUserInfo(CurrentUserInfo userInfo) {
        // 设置界面标题
        roleTitle.setText(userInfo.getRoleName());

        // 显示聚合的用户信息
        String info = String.format(
                "用户名: %s (UserID: %d)\n角色: %s (ID: %d)\n员工姓名: %s\n职位名称: %s",
                userInfo.getUsername(),
                userInfo.getUserId(),
                userInfo.getRoleName(),
                userInfo.getRoleId(),
                userInfo.getEmployeeName(),
                userInfo.getPositionName()
        );
        userInfoText.setText(info);
    }
}