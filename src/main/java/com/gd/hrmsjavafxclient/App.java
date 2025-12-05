package com.gd.hrmsjavafxclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("LoginView.fxml"));
        // 这里的 "LoginView.fxml" 路径要和你的文件位置匹配！
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("人事管理系统 - 登录");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}