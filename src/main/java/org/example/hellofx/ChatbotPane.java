package org.example.hellofx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class ChatbotPane extends VBox {
    
    private ListView<String> chatListView;
    private TextField messageInput;
    private Button sendButton;
    private ChatBotService chatBotService;
    
    public ChatbotPane() {
        chatBotService = new ChatBotService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Header
        Label headerLabel = new Label("🤖 AI Assistant");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Close button
        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        closeButton.setOnAction(e -> this.setVisible(false));
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #5d6bff; -fx-background-radius: 10 10 0 0;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(headerLabel, spacer, closeButton);
        
        // Chat area
        chatListView = new ListView<>();
        chatListView.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: transparent;");
        chatListView.setPrefHeight(400);
        VBox.setVgrow(chatListView, Priority.ALWAYS);
        
        // Add welcome message
        chatListView.getItems().add("Bot: Hello! I'm your AI assistant. How can I help you today?");
        
        // Input area
        messageInput = new TextField();
        messageInput.setPromptText("Type your message...");
        messageInput.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        
        sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #5d6bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setStyle("-fx-background-color: white;");
        inputBox.getChildren().addAll(messageInput, sendButton);
        
        // Main layout
        this.getChildren().addAll(header, chatListView, inputBox);
    }
    
    private void setupLayout() {
        this.setPrefWidth(350);
        this.setMaxWidth(350);
        this.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
        this.setVisible(false); // Hidden by default
    }
    
    private void setupEventHandlers() {
        // Send message on button click
        sendButton.setOnAction(e -> sendMessage());
        
        // Send message on Enter key
        messageInput.setOnAction(e -> sendMessage());
    }
    
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            // Add user message
            chatListView.getItems().add("You: " + message);
            messageInput.clear();
            
            // Simulate bot response (you'll replace this with actual AI later)
            simulateBotResponse(message);
            
            // Auto-scroll to bottom
            chatListView.scrollTo(chatListView.getItems().size() - 1);
        }
    }
    
    private void simulateBotResponse(String userMessage) {
        // Send message to ChatBotService asynchronously
        sendButton.setDisable(true);
        messageInput.setDisable(true);
        
        chatBotService.requestCommand("{}", userMessage)
            .thenAccept(command -> {
                javafx.application.Platform.runLater(() -> {
                    String response = "Bot: " + command.explanation;
                    if (!command.action.equals("noop")) {
                        response += " [Action: " + command.action;
                        if (command.direction != null) {
                            response += ", Direction: " + command.direction;
                        }
                        if (command.distance > 0) {
                            response += ", Distance: " + command.distance;
                        }
                        response += "]";
                    }
                    chatListView.getItems().add(response);
                    chatListView.scrollTo(chatListView.getItems().size() - 1);
                    sendButton.setDisable(false);
                    messageInput.setDisable(false);
                    messageInput.requestFocus();
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    chatListView.getItems().add("Bot: Sorry, I encountered an error. Please try again.");
                    chatListView.scrollTo(chatListView.getItems().size() - 1);
                    sendButton.setDisable(false);
                    messageInput.setDisable(false);
                    messageInput.requestFocus();
                });
                return null;
            });
    }
    
    public void show() {
        this.setVisible(true);
    }
    
    public void hide() {
        this.setVisible(false);
    }
    
    public void toggle() {
        this.setVisible(!this.isVisible());
    }
    
    public void shutdown() {
        if (chatBotService != null) {
            chatBotService.shutdown();
        }
    }
}