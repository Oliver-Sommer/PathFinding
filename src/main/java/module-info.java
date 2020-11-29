module at.oliver {
    requires javafx.controls;
    requires javafx.fxml;

    opens at.oliver to javafx.fxml;
    exports at.oliver;

    opens at.oliver.map to javafx.fxml;
    exports at.oliver.map;

    exports at.oliver.node;
}