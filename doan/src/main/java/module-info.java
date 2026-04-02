module com.example {
    requires javafx.controls;
    requires java.sql;

    opens com.example to javafx.base;
    opens com.example.controller to javafx.base;
    opens com.example.controller.admin to javafx.base;
    opens com.example.model to javafx.base;
    
    exports com.example;
    exports com.example.controller;
    exports com.example.controller.admin;
    exports com.example.model;
    exports com.example.exception;
}
