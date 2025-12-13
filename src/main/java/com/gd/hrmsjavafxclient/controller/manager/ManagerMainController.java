package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.controller.MainController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

/**
 * 角色ID=4：部门经理主界面控制器
 * 🌟 修正：实现带有 authToken 的 setUserInfo 方法。
 */
public class ManagerMainController implements MainController {

    @FXML
    private Text userInfoText;
    @FXML
    private Label roleTitle;

    // --- 新增字段 ---
    private String authToken; // 🌟 缓存认证 Token

    /**
     * 实现 MainController 接口
     * 🌟 修正：接受并存储 authToken
     */
    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) { // 👈 修正方法签名
        // 存储 Token
        this.authToken = authToken;

        // 显示用户信息逻辑不变
        roleTitle.setText(userInfo.getRoleName());

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

        // 可以在这里调用 loadView 方法来加载默认视图
    }
}