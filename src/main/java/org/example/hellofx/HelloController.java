package org.example.hellofx; // Make sure this matches your package structure

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

// Make sure the class name matches the one in fx:controller
public class HelloController {

    private Stage stage;
    private Scene scene;

    // Ensure the method name matches exactly what's in the FXML (switchScene2)
    public void switchScene(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml")); // Loading the same scene again, perhaps for a different purpose?
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // Ensure the method name matches exactly what's in the FXML (switchScene2)
    public void switchScene2(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Scene2.fxml")); // Ensure Scene2.fxml exists
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}