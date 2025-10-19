module org.example.hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    

    opens org.example.hellofx to javafx.fxml;
    exports org.example.hellofx;
}
