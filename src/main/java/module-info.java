module com.gd.hrmsjavafxclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.gd.hrmsjavafxclient to javafx.fxml;
    exports com.gd.hrmsjavafxclient;
}