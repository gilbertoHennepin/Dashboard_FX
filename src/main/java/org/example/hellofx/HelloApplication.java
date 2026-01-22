package org.example.hellofx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    
    private static ChatbotPane chatbot;
    private static BorderPane rootLayout;
    private static StackPane contentArea;
    
    @Override
    public void start(Stage stage) throws IOException {
        // Create root layout
        rootLayout = new BorderPane();
        
        // Create chatbot (hidden by default)
        chatbot = new ChatbotPane();
        
        // Create content area for scenes
        contentArea = new StackPane();
        contentArea.setAlignment(Pos.TOP_LEFT);
        
        // Load initial scene (dashboard)
        Parent initialScene = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
        contentArea.getChildren().add(initialScene);
        
        // Create chatbot toggle button (floating button)
        Button chatbotToggle = new Button("💬");
        chatbotToggle.setStyle(
            "-fx-background-color: #5d6bff; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 24px; " +
            "-fx-background-radius: 50; " +
            "-fx-min-width: 60px; " +
            "-fx-min-height: 60px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );
        chatbotToggle.setOnAction(e -> chatbot.toggle());
        
        // Position toggle button
        StackPane.setAlignment(chatbotToggle, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatbotToggle, new Insets(0, 30, 30, 0));
        
        // Position chatbot
        StackPane.setAlignment(chatbot, Pos.CENTER_RIGHT);
        StackPane.setMargin(chatbot, new Insets(20, 20, 20, 0));
        
        // Combine content and chatbot in a StackPane
        StackPane mainStack = new StackPane();
        mainStack.getChildren().addAll(contentArea, chatbot, chatbotToggle);
        
        rootLayout.setCenter(mainStack);
        
        // Create scene
        Scene scene = new Scene(rootLayout, 1200, 800);
        stage.setTitle("Robot Simulator Dashboard");
        stage.setScene(scene);
        stage.show();
    }
    
    // Static method to switch scenes while keeping chatbot
    public static void switchScene(Parent newScene) {
        if (contentArea == null) {
            System.err.println("ERROR: contentArea is null! Cannot switch scene.");
            return;
        }
        if (newScene == null) {
            System.err.println("ERROR: newScene is null! Cannot switch scene.");
            return;
        }
        try {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newScene);
            System.out.println("Scene switched successfully");
        } catch (Exception e) {
            System.err.println("ERROR switching scene:");
            e.printStackTrace();
        }
    }
    
    // Static method to get chatbot instance
    public static ChatbotPane getChatbot() {
        return chatbot;
    }

    public static void main(String[] args) {
        launch();
    }
}