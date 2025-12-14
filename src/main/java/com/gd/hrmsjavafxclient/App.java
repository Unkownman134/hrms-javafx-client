package com.gd.hrmsjavafxclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App 启动类
 */
public class App extends Application {

    // 🌟 修正 1: 存储主 Stage 实例，用于全局控制，例如登出切换视图
    private static Stage primaryStage;

    // 静态方法，用于获取 FXML 资源的便捷路径
    private static Parent loadFXML(String fxml) throws IOException {
        // 假设 FXML 路径是 resources/com/gd/hrmsjavafxclient/fxml/
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // 修正 2: 在 start 方法中保存 Stage 实例
        primaryStage = stage;

        // -------------------------------------------------------------------
        // 应用程序启动时，首先加载登录界面
        // -------------------------------------------------------------------

        loadLoginView(primaryStage); // 委托给新方法加载登录页
    }

    // 🌟 修正 3: 新增静态的登出方法，解决 ManagerMainController 的编译错误
    /**
     * 执行登出操作，返回登录界面。
     */
    public static void logout() {
        if (primaryStage != null) {
            try {
                // 1. 清除客户端存储的 Token 和用户信息 (实际项目中应在此处添加清除逻辑)
                // TokenManager.clearToken();
                System.out.println("用户已登出，清除 Token...");

                // 2. 切换回登录界面
                loadLoginView(primaryStage);
            } catch (IOException e) {
                System.err.println("登出时无法加载登录视图：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 内部方法：加载并显示 LoginView
     */
    private static void loadLoginView(Stage stage) throws IOException {
        Parent root = loadFXML("LoginView");

        // LoginView.fxml 已经通过 stylesheets=\"@hrms-styles.css\" 加载了样式表
        // 重新创建 Scene，确保 LoginController 重新初始化
        Scene scene = new Scene(root, 500, 500);

        stage.setTitle("人事管理系统 - 登录");
        stage.setScene(scene);
        stage.show();
        // 确保窗口居中，如果需要的话
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch();
    }
}