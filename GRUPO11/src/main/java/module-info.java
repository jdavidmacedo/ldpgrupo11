module domino {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    opens domino to javafx.fxml;
    exports domino;
}
