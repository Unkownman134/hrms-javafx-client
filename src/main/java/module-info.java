module com.gd.hrmsjavafxclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires javafx.base;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;

    opens com.gd.hrmsjavafxclient to javafx.fxml;
    exports com.gd.hrmsjavafxclient;
    opens com.gd.hrmsjavafxclient.controller to javafx.fxml;
    exports com.gd.hrmsjavafxclient.controller;

    opens com.gd.hrmsjavafxclient.model to com.fasterxml.jackson.databind, javafx.base;
}