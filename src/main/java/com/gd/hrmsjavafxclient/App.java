package com.gd.hrmsjavafxclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;

    private static final String APP_ICON_PATH = "images/icon.jpg";

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        try (InputStream is = App.class.getResourceAsStream(APP_ICON_PATH)) {
            if (is != null) {
                Image appIcon = new Image(is);
                primaryStage.getIcons().add(appIcon);
            } else {
                System.err.println("应用程序图标文件未找到: " + APP_ICON_PATH);
            }
        } catch (Exception e) {
            System.err.println("应用程序图标加载失败。");
            e.printStackTrace();
        }

        loadLoginView(primaryStage);
    }

    public static void logout() {
        if (primaryStage != null) {
            try {
                System.out.println("用户已登出，清除 Token...");
                loadLoginView(primaryStage);
            } catch (IOException e) {
                System.err.println("登出时无法加载登录视图：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void loadLoginView(Stage stage) throws IOException {
        Parent root = loadFXML("LoginView");
        Scene scene = new Scene(root);
        stage.setTitle("人事管理系统 - 登录");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}