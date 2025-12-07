module com.gd.hrmsjavafxclient {
    requires java.net.http;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;

    requires org.controlsfx.controls;

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    opens com.gd.hrmsjavafxclient to javafx.fxml;
    exports com.gd.hrmsjavafxclient;
    opens com.gd.hrmsjavafxclient.controller to javafx.fxml;
    exports com.gd.hrmsjavafxclient.controller;

    opens com.gd.hrmsjavafxclient.model to com.fasterxml.jackson.databind, javafx.base;
    exports com.gd.hrmsjavafxclient.controller.admin;
    opens com.gd.hrmsjavafxclient.controller.admin to javafx.fxml;
    exports com.gd.hrmsjavafxclient.controller.employee;
    opens com.gd.hrmsjavafxclient.controller.employee to javafx.fxml;
    exports com.gd.hrmsjavafxclient.controller.hr;
    opens com.gd.hrmsjavafxclient.controller.hr to javafx.fxml;
    exports com.gd.hrmsjavafxclient.controller.finance;
    opens com.gd.hrmsjavafxclient.controller.finance to javafx.fxml;
    exports com.gd.hrmsjavafxclient.controller.manager;
    opens com.gd.hrmsjavafxclient.controller.manager to javafx.fxml;
}