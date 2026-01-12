// Added `com.google.genai` so the project can compile against the
// Google GenAI client library used by `ChatBotService`.
module org.example.hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.genai;

    opens org.example.hellofx to javafx.fxml;
    exports org.example.hellofx;
    
}
