package com.gd.hrmsjavafxclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App 启动类 (P1)
 */
public class App extends Application {

    // 静态方法，用于获取 FXML 资源的便捷路径
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // 应用程序启动时，首先加载登录界面
        Parent root = loadFXML("LoginView");
        Scene scene = new Scene(root, 400, 300); // 登录界面大小示例

        stage.setTitle("人事管理系统 - 登录");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}