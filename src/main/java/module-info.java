module org.example.checkers {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;


    opens org.example.checkers to javafx.fxml;
    exports com.example.checkers;
    opens com.example.checkers to javafx.fxml;
}