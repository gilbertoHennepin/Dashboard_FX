module org.example.hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires com.google.genai;

    opens org.example.hellofx to javafx.fxml;
    exports org.example.hellofx;
    exports org.example.hellofx.dataClasses;  
    exports org.example.hellofx.dba;      
    
}
