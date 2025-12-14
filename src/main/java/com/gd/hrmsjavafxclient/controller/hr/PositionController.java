package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.service.HRDataService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

/**
 * 职位管理子视图控制器 (加载并显示职位列表)
 */
public class PositionController implements HRSubController {

    @FXML private TableView<Position> positionTable;
    // 🌟 Table Columns (fx:id 必须匹配 FXML)
    @FXML private TableColumn<Position, Integer> posIdCol;
    @FXML private TableColumn<Position, String> posNameCol;
    @FXML private TableColumn<Position, String> posLevelCol;
    @FXML private TableColumn<Position, Integer> baseSalaryLevelCol; // 对应 BaseSalaryLevel

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    @FXML
    public void initialize() {
        setupTableColumns();
    }

    /**
     * 配置 TableView 的列与 Position Model 的属性绑定。
     */
    private void setupTableColumns() {
        // 绑定 Position.java 中的属性名
        posIdCol.setCellValueFactory(new PropertyValueFactory<>("posId"));
        posNameCol.setCellValueFactory(new PropertyValueFactory<>("posName"));
        posLevelCol.setCellValueFactory(new PropertyValueFactory<>("posLevel"));
        baseSalaryLevelCol.setCellValueFactory(new PropertyValueFactory<>("baseSalaryLevel"));

        positionTable.setPlaceholder(new Label("正在加载职位数据..."));
    }

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadPositionData();
    }

    /**
     * 在后台线程中调用 API 获取职位数据。
     */
    private void loadPositionData() {
        positionTable.getItems().clear();
        positionTable.setPlaceholder(new Label("正在从 API 加载数据，请稍候..."));

        Task<List<Position>> loadTask = new Task<>() {
            @Override
            protected List<Position> call() throws Exception {
                // 🌟 调用 Service 获取真实数据
                return hrDataService.getAllPositions(authToken);
            }

            @Override
            protected void succeeded() {
                List<Position> result = getValue();
                Platform.runLater(() -> {
                    if (result.isEmpty()) {
                        positionTable.setPlaceholder(new Label("暂无职位数据 🙅‍♀️ 或 API 调用失败。"));
                    } else {
                        positionTable.setItems(FXCollections.observableArrayList(result));
                        positionTable.setPlaceholder(new Label(""));
                    }
                    System.out.println("职位列表加载成功，共 " + result.size() + " 条记录。");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("加载失败 ❌", "无法加载职位数据，请检查网络或后端服务。");
                    positionTable.setPlaceholder(new Label("数据加载失败。"));
                });
                getException().printStackTrace();
            }
        };
        new Thread(loadTask).start();
    }

    @FXML
    private void handleAddPosition() {
        showAlert("提示 💡", "新增职位功能待实现。");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}