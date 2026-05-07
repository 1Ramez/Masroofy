module masroofy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires org.xerial.sqlitejdbc;

    opens masroofy to javafx.fxml;
    opens masroofy.view to javafx.fxml;
    opens masroofy.controller to javafx.fxml;
    opens masroofy.model to javafx.base;
    opens masroofy.data to javafx.base;

    exports masroofy;
        exports masroofy.view;
        exports masroofy.controller;
    exports masroofy.model;
    exports masroofy.data;
}
