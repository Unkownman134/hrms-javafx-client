package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 角色ID=1：超级管理员的主界面控制器
 */
public class AdminMainController implements MainController {

    @FXML private Text userInfoText;
    @FXML private StackPane contentPane; // 动态内容加载容器

    // 缓存已加载的视图，避免重复加载 FXML
    private final Map<String, Parent> viewCache = new HashMap<>();

    /**
     * 实现 MainController 接口，接收并显示数据
     */
    @Override
    public void setUserInfo(CurrentUserInfo userInfo) {
        String info = String.format(
                "当前登录人: %s | 身份: %s (RoleID: %d) | 职位: %s",
                userInfo.getEmployeeName(),
                userInfo.getRoleName(),
                userInfo.getRoleId(),
                userInfo.getPositionName()
        );
        userInfoText.setText(info);

        // 登录成功后，自动显示仪表盘
        showDashboardView();
    }

    // --- 动态视图加载核心方法 ---
    private void loadView(String fxmlFileName) {
        if (viewCache.containsKey(fxmlFileName)) {
            // 从缓存加载
            contentPane.getChildren().setAll(viewCache.get(fxmlFileName));
            return;
        }

        try {
            // 加载新的 FXML
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlFileName + ".fxml"));
            Parent view = loader.load();

            // 缓存视图
            viewCache.put(fxmlFileName, view);

            // 显示视图
            contentPane.getChildren().setAll(view);

            // ⚠️ 可选：如果子视图也需要 CurrentUserInfo，在这里调用 setControllerData

        } catch (IOException e) {
            System.err.println("无法加载视图: " + fxmlFileName);
            e.printStackTrace();
            contentPane.getChildren().setAll(new Label("加载视图失败: " + fxmlFileName));
        }
    }

    // --- 菜单点击事件：全部接口调用入口 (P1) ---

    @FXML
    public void showDashboardView() {
        // 首页仪表盘（可以简单用一个 Label FXML 代替）
        loadView("AdminDashboardView");
    }

    @FXML
    public void showUserView() {
        // R11: 系统用户账号信息管理
        loadView("UserManagementView");
    }

    @FXML
    public void showEmployeeView() {
        // R10: 员工基本档案信息管理
        loadView("EmployeeManagementView");
    }

    @FXML
    public void showPositionView() {
        // R9: 职位信息管理
        loadView("PositionManagementView");
    }

    @FXML
    public void showDepartmentView() {
        // R5: 部门信息管理 (R5 API: /api/departments)
        loadView("DepartmentManagementView");
    }

    @FXML
    public void showSalaryView() {
        // R8: 薪酬标准配置 (R8 API: /api/salary-standards)
        loadView("SalaryStandardManagementView");
    }
}