package org.example.hellofx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main JavaFX Application with persistent chatbot panel on the right side.
 * Uses a single Scene with BorderPane layout for efficient content switching.
 */
public class Main extends Application {

    private StackPane contentArea;  // Center area for switching pages
    private VBox chatPanel;         // Persistent chatbot panel on the right
    private VBox chatMessagesContainer;  // Container for chat messages
    private TextField chatInput;    // Input field for chat messages
    private ScrollPane chatScrollPane;  // Scrollable area for messages

    @Override
    public void start(Stage primaryStage) {
        // Create root BorderPane
        BorderPane root = new BorderPane();

        // Create and setup the persistent chatbot panel
        chatPanel = createChatPanel();
        root.setRight(chatPanel);

        // Create the center content area (StackPane for easy content switching)
        contentArea = new StackPane();
        root.setCenter(contentArea);

        // Create top menu bar for navigation
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // Set initial content (Home page)
        switchContent(createHomePage());

        // Create main scene
        Scene scene = new Scene(root, 1100, 700);
        
        // Apply some basic styling (if stylesheet exists)
        try {
            var stylesheet = getClass().getResource("/style.css");
            if (stylesheet != null) {
                scene.getStylesheets().add(stylesheet.toExternalForm());
            }
        } catch (Exception e) {
            // Stylesheet is optional, continue without it
        }
        
        // Setup stage
        primaryStage.setTitle("Dashboard FX - AI Chatbot");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    /**
     * Creates the persistent chatbot panel that stays on the right side.
     */
    private VBox createChatPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(330);
        panel.setMinWidth(280);
        panel.setMaxWidth(400);
        
        // Styling
        panel.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 0 0 0 1; " +
            "-fx-padding: 15;"
        );

        // Title label
        Label titleLabel = new Label("AI Chatbot");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #212529;");
        VBox.setMargin(titleLabel, new Insets(0, 0, 10, 0));

        // Chat messages area (ScrollPane with VBox inside)
        chatScrollPane = new ScrollPane();
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4;"
        );
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        chatMessagesContainer = new VBox(8);
        chatMessagesContainer.setPadding(new Insets(10));
        chatMessagesContainer.setStyle("-fx-background-color: white;");
        chatScrollPane.setContent(chatMessagesContainer);

        // Add welcome message
        addChatMessage("AI", "Welcome! Ask me anything...", false);

        // Input area at bottom
        HBox inputBox = new HBox(8);
        inputBox.setAlignment(Pos.CENTER_LEFT);
        inputBox.setPadding(new Insets(10, 0, 0, 0));

        chatInput = new TextField();
        chatInput.setPromptText("Type your message...");
        chatInput.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-radius: 4; " +
            "-fx-padding: 8;"
        );
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.setStyle(
            "-fx-background-color: #007bff; " +
            "-fx-text-fill: white; " +
            "-fx-border-radius: 4; " +
            "-fx-padding: 8 16; " +
            "-fx-cursor: hand;"
        );
        sendButton.setOnAction(e -> handleSendMessage());

        // Handle Enter key in text field
        chatInput.setOnAction(e -> handleSendMessage());

        inputBox.getChildren().addAll(chatInput, sendButton);

        // Assemble panel
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);
        panel.getChildren().addAll(titleLabel, chatScrollPane, inputBox);

        return panel;
    }

    /**
     * Adds a message to the chat panel.
     * @param sender "User" or "AI"
     * @param message The message text
     * @param isUser True if message is from user, false if from AI
     */
    private void addChatMessage(String sender, String message, boolean isUser) {
        VBox messageBubble = new VBox(4);
        messageBubble.setPadding(new Insets(8, 12, 8, 12));
        messageBubble.setMaxWidth(280);

        // Sender label
        Label senderLabel = new Label(sender);
        senderLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        senderLabel.setStyle("-fx-text-fill: #6c757d;");

        // Message text
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("System", 13));
        messageLabel.setStyle("-fx-text-fill: #212529;");

        messageBubble.getChildren().addAll(senderLabel, messageLabel);

        // Style based on sender
        if (isUser) {
            messageBubble.setAlignment(Pos.TOP_RIGHT);
            messageBubble.setStyle(
                "-fx-background-color: #e7f3ff; " +
                "-fx-background-radius: 8 8 2 8; " +
                "-fx-alignment: top-right;"
            );
            HBox wrapper = new HBox();
            wrapper.setAlignment(Pos.CENTER_RIGHT);
            wrapper.getChildren().add(messageBubble);
            chatMessagesContainer.getChildren().add(wrapper);
        } else {
            messageBubble.setAlignment(Pos.TOP_LEFT);
            messageBubble.setStyle(
                "-fx-background-color: #f1f3f5; " +
                "-fx-background-radius: 8 8 8 2; " +
                "-fx-alignment: top-left;"
            );
            HBox wrapper = new HBox();
            wrapper.setAlignment(Pos.CENTER_LEFT);
            wrapper.getChildren().add(messageBubble);
            chatMessagesContainer.getChildren().add(wrapper);
        }

        // Auto-scroll to bottom
        chatScrollPane.setVvalue(1.0);
    }

    /**
     * Handles sending a message from the chat input.
     */
    private void handleSendMessage() {
        String text = chatInput.getText().trim();
        if (!text.isEmpty()) {
            addChatMessage("You", text, true);
            chatInput.clear();
            
            // For now, just echo back (will be replaced with real AI later)
            addChatMessage("AI", "You said: " + text, false);
        }
    }

    /**
     * Creates the top menu bar for navigation.
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #ffffff;");

        Menu navigationMenu = new Menu("Navigation");
        
        MenuItem homeItem = new MenuItem("Home");
        homeItem.setOnAction(e -> switchContent(createHomePage()));
        
        MenuItem settingsItem = new MenuItem("Settings");
        settingsItem.setOnAction(e -> switchContent(createSettingsPage()));
        
        MenuItem dataViewerItem = new MenuItem("Data Viewer");
        dataViewerItem.setOnAction(e -> switchContent(createDataViewerPage()));

        navigationMenu.getItems().addAll(homeItem, settingsItem, dataViewerItem);
        menuBar.getMenus().add(navigationMenu);

        return menuBar;
    }

    /**
     * Switches the content in the center area.
     * This is the key method for single-scene navigation.
     */
    private void switchContent(Parent newContent) {
        contentArea.getChildren().setAll(newContent);
    }

    /**
     * Creates the Home / Dashboard page.
     */
    private Parent createHomePage() {
        VBox homePage = new VBox(20);
        homePage.setAlignment(Pos.CENTER);
        homePage.setPadding(new Insets(40));
        homePage.setStyle("-fx-background-color: #ffffff;");

        Label titleLabel = new Label("Welcome to Dashboard FX");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: #212529;");

        Label subtitleLabel = new Label("Your AI-powered dashboard application");
        subtitleLabel.setFont(Font.font("System", 16));
        subtitleLabel.setStyle("-fx-text-fill: #6c757d;");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button settingsBtn = new Button("Settings");
        settingsBtn.setStyle(
            "-fx-background-color: #007bff; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 20; " +
            "-fx-font-size: 14; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 4;"
        );
        settingsBtn.setOnAction(e -> switchContent(createSettingsPage()));

        Button dataViewerBtn = new Button("Data Viewer");
        dataViewerBtn.setStyle(
            "-fx-background-color: #28a745; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 20; " +
            "-fx-font-size: 14; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 4;"
        );
        dataViewerBtn.setOnAction(e -> switchContent(createDataViewerPage()));

        buttonBox.getChildren().addAll(settingsBtn, dataViewerBtn);

        Label infoLabel = new Label(
            "Use the menu bar or buttons above to navigate.\n" +
            "The chatbot on the right is always available for assistance."
        );
        infoLabel.setFont(Font.font("System", 13));
        infoLabel.setStyle("-fx-text-fill: #6c757d;");
        infoLabel.setAlignment(Pos.CENTER);

        homePage.getChildren().addAll(titleLabel, subtitleLabel, buttonBox, infoLabel);

        return homePage;
    }

    /**
     * Creates the Settings page.
     */
    private Parent createSettingsPage() {
        VBox settingsPage = new VBox(20);
        settingsPage.setAlignment(Pos.TOP_LEFT);
        settingsPage.setPadding(new Insets(30));
        settingsPage.setStyle("-fx-background-color: #ffffff;");

        Label titleLabel = new Label("Settings");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #212529;");

        // Settings sections
        VBox generalSection = new VBox(10);
        generalSection.setPadding(new Insets(15));
        generalSection.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4;"
        );

        Label generalLabel = new Label("General Settings");
        generalLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        CheckBox autoSaveCheckBox = new CheckBox("Enable auto-save");
        autoSaveCheckBox.setSelected(true);

        CheckBox notificationsCheckBox = new CheckBox("Enable notifications");
        notificationsCheckBox.setSelected(true);

        generalSection.getChildren().addAll(generalLabel, autoSaveCheckBox, notificationsCheckBox);

        // Appearance section
        VBox appearanceSection = new VBox(10);
        appearanceSection.setPadding(new Insets(15));
        appearanceSection.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4;"
        );

        Label appearanceLabel = new Label("Appearance");
        appearanceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label themeLabel = new Label("Theme:");
        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("Light", "Dark", "Auto");
        themeCombo.setValue("Light");

        HBox themeBox = new HBox(10);
        themeBox.getChildren().addAll(themeLabel, themeCombo);

        appearanceSection.getChildren().addAll(appearanceLabel, themeBox);

        // Text field example
        VBox otherSection = new VBox(10);
        otherSection.setPadding(new Insets(15));
        otherSection.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4;"
        );

        Label otherLabel = new Label("Other");
        otherLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label nameLabel = new Label("User Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");
        nameField.setPrefWidth(250);

        VBox nameBox = new VBox(5);
        nameBox.getChildren().addAll(nameLabel, nameField);

        otherSection.getChildren().addAll(otherLabel, nameBox);

        settingsPage.getChildren().addAll(
            titleLabel, generalSection, appearanceSection, otherSection
        );

        return settingsPage;
    }

    /**
     * Creates the Data Viewer page with a table example.
     */
    private Parent createDataViewerPage() {
        VBox dataViewerPage = new VBox(20);
        dataViewerPage.setAlignment(Pos.TOP_LEFT);
        dataViewerPage.setPadding(new Insets(30));
        dataViewerPage.setStyle("-fx-background-color: #ffffff;");

        Label titleLabel = new Label("Data Viewer");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #212529;");

        // Create a simple table
        TableView<String[]> table = new TableView<>();
        table.setPrefHeight(400);
        table.setStyle(
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 1;"
        );

        // Sample columns
        TableColumn<String[], String> col1 = new TableColumn<>("ID");
        col1.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[0]));
        col1.setPrefWidth(100);

        TableColumn<String[], String> col2 = new TableColumn<>("Name");
        col2.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[1]));
        col2.setPrefWidth(200);

        TableColumn<String[], String> col3 = new TableColumn<>("Status");
        col3.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[2]));
        col3.setPrefWidth(150);

        TableColumn<String[], String> col4 = new TableColumn<>("Date");
        col4.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[3]));
        col4.setPrefWidth(150);

        table.getColumns().add(col1);
        table.getColumns().add(col2);
        table.getColumns().add(col3);
        table.getColumns().add(col4);

        // Sample data
        table.getItems().add(new String[]{"1", "Sample Item 1", "Active", "2024-01-15"});
        table.getItems().add(new String[]{"2", "Sample Item 2", "Inactive", "2024-01-16"});
        table.getItems().add(new String[]{"3", "Sample Item 3", "Active", "2024-01-17"});
        table.getItems().add(new String[]{"4", "Sample Item 4", "Pending", "2024-01-18"});

        Label infoLabel = new Label("This is a placeholder table. Connect to your data source to display real data.");
        infoLabel.setFont(Font.font("System", 12));
        infoLabel.setStyle("-fx-text-fill: #6c757d;");

        VBox.setVgrow(table, Priority.ALWAYS);
        dataViewerPage.getChildren().addAll(titleLabel, table, infoLabel);

        return dataViewerPage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
