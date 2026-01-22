module org.example.hellofx {
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.desktop;
    requires transitive java.sql;
    opens org.example.hellofx to javafx.fxml;
    exports org.example.hellofx;
    exports org.example.hellofx.dataClasses;  
    exports org.example.hellofx.dba;
}
