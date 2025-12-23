package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.model.SalaryStandard;
import com.gd.hrmsjavafxclient.service.hr.HRDataService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PositionController implements HRSubController {

    @FXML private TableView<Position> positionTable;
    @FXML private TableColumn<Position, String> posNameCol;
    @FXML private TableColumn<Position, String> levelCol;
    @FXML private TableColumn<Position, String> salaryStandardNameCol; // 🌟 显示名称而非 ID
    @FXML private TableColumn<Position, Void> actionCol;

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    // 数据缓存
    private List<SalaryStandard> salaryStandardList = new ArrayList<>();
    private Map<Integer, String> salaryMap = new HashMap<>();

    @FXML
    public void initialize() {
        // 绑定基础列
        posNameCol.setCellValueFactory(new PropertyValueFactory<>("posName"));
        levelCol.setCellValueFactory(new PropertyValueFactory<>("posLevel"));

        // 🌟 薪资等级 ID 转名称显示
        salaryStandardNameCol.setCellValueFactory(cellData -> {
            Integer stdId = cellData.getValue().getBaseSalaryLevel();
            return new SimpleStringProperty(salaryMap.getOrDefault(stdId, "未设置 (" + stdId + ")"));
        });

        setupActionColumn();
    }

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadData();
    }

    private void loadData() {
        if (authToken == null) return;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 1. 先加载薪资标准映射
                salaryStandardList = hrDataService.getAllSalaryStandards(authToken);
                salaryMap = salaryStandardList.stream()
                        .collect(Collectors.toMap(SalaryStandard::getStdId, SalaryStandard::getStandardName, (v1, v2) -> v1));

                // 2. 加载职位列表
                List<Position> positions = hrDataService.getAllPositions(authToken);

                Platform.runLater(() -> {
                    positionTable.setItems(FXCollections.observableArrayList(positions));
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button deleteBtn = new Button("删除");
            private final HBox container = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("action-button-edit");
                deleteBtn.getStyleClass().add("action-button-delete");
                container.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadData();
    }

    @FXML
    private void handleNewPosition(ActionEvent event) {
        showEditDialog(new Position());
    }

    private void showEditDialog(Position p) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(p.getPosId() == null ? "✨ 新增职位" : "📝 编辑职位");

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(25));

        TextField nameIn = new TextField(p.getPosName());
        TextField levelIn = new TextField(p.getPosLevel());

        // 🌟 薪资标准下拉框
        ComboBox<SalaryStandard> salaryComboBox = new ComboBox<>(FXCollections.observableArrayList(salaryStandardList));
        salaryComboBox.setPromptText("请选择薪酬标准");
        salaryComboBox.setMinWidth(200);
        salaryComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(SalaryStandard s) { return s == null ? "" : s.getStandardName(); }
            @Override public SalaryStandard fromString(String s) { return null; }
        });

        // 回显选中
        if (p.getBaseSalaryLevel() != null) {
            salaryStandardList.stream()
                    .filter(s -> s.getStdId().equals(p.getBaseSalaryLevel()))
                    .findFirst()
                    .ifPresent(salaryComboBox::setValue);
        }

        grid.add(new Label("职位名称:"), 0, 0); grid.add(nameIn, 1, 0);
        grid.add(new Label("职级代码:"), 0, 1); grid.add(levelIn, 1, 1);
        grid.add(new Label("薪酬体系:"), 0, 2); grid.add(salaryComboBox, 1, 2);

        Button save = new Button("保存提交");
        save.getStyleClass().add("action-button-primary");
        save.setOnAction(e -> {
            p.setPosName(nameIn.getText());
            p.setPosLevel(levelIn.getText());
            if (salaryComboBox.getValue() != null) {
                p.setBaseSalaryLevel(salaryComboBox.getValue().getStdId());
            }

            new Thread(() -> {
                boolean ok = (p.getPosId() == null) ?
                        hrDataService.addPosition(p, authToken) :
                        hrDataService.updatePosition(p, authToken);
                Platform.runLater(() -> {
                    if (ok) { stage.close(); loadData(); }
                    else { new Alert(Alert.AlertType.ERROR, "职位保存失败").show(); }
                });
            }).start();
        });

        VBox root = new VBox(20, grid, save);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(10, 10, 30, 10));
        stage.setScene(new Scene(root, 400, 350));
        stage.show();
    }

    private void handleDelete(Position p) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "确认删除岗位 [" + p.getPosName() + "] 吗？");
        a.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                new Thread(() -> {
                    boolean ok = hrDataService.deletePosition(p.getPosId(), authToken);
                    Platform.runLater(() -> { if(ok) loadData(); });
                }).start();
            }
        });
    }
}