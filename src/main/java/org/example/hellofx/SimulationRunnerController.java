package org.example.hellofx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.hellofx.dba.SimulationDAO;
import org.example.hellofx.dataClasses.Layout;
import org.example.hellofx.dataClasses.Ruleset;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

public class SimulationRunnerController {

    @FXML
    private ComboBox<Layout> layoutComboBox;

    @FXML
    private ComboBox<Ruleset> rulesetComboBox;

    @FXML
    private Spinner<Integer> maxAttemptsSpinner;

    @FXML
    private GridPane factoryGrid;

    @FXML
    private ListView<String> moveListView;

    @FXML
    private Button runButton;

    private SimulationDAO simulationDAO;

    @FXML
    public void initialize() {
        // Initialize spinner with default values
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10);
        maxAttemptsSpinner.setValueFactory(valueFactory);

        // Listen for layout selection changes
        layoutComboBox.setOnAction(e -> {
            Layout selected = layoutComboBox.getValue();
            if (selected != null) {
                displayLayout(selected);
            }
        });

        // Load data from database
        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        new Thread(() -> {
            try {
                Connection conn = DatabaseConfig.getConnection();
                simulationDAO = new SimulationDAO(conn);

                // Fetch layouts and rulesets
                List<Layout> layouts = simulationDAO.getAllLayouts();
                List<Ruleset> rulesets = simulationDAO.getAllRulesets();

                // Update UI on JavaFX Application Thread
                Platform.runLater(() -> {
                    // Debug: Print what we got
                    System.out.println("Loaded " + layouts.size() + " layouts:");
                    for (Layout layout : layouts) {
                        System.out.println("  - " + layout.getName());
                    }
                    System.out.println("Loaded " + rulesets.size() + " rulesets:");
                    for (Ruleset ruleset : rulesets) {
                        System.out.println("  - " + ruleset.getName());
                    }

                    layoutComboBox.setItems(FXCollections.observableArrayList(layouts));
                    rulesetComboBox.setItems(FXCollections.observableArrayList(rulesets));

                    // Auto-select first items if available
                    if (!layouts.isEmpty()) {
                        layoutComboBox.getSelectionModel().selectFirst();
                        displayLayout(layouts.get(0)); // Display first layout
                    }
                    if (!rulesets.isEmpty()) {
                        rulesetComboBox.getSelectionModel().selectFirst();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Database Error");
                    alert.setHeaderText("Failed to load data");
                    alert.setContentText("Could not connect to database: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void handleRun() {
        Layout selectedLayout = layoutComboBox.getValue();
        Ruleset selectedRuleset = rulesetComboBox.getValue();
        Integer maxAttempts = maxAttemptsSpinner.getValue();

        // Validate selections
        if (selectedLayout == null) {
            showAlert("Please select a layout");
            return;
        }
        if (selectedRuleset == null) {
            showAlert("Please select a ruleset");
            return;
        }

        // Clear previous log
        moveListView.getItems().clear();
        
        // Add log messages
        moveListView.getItems().add("▶ Starting simulation...");
        moveListView.getItems().add("Layout: " + selectedLayout.getName());
        moveListView.getItems().add("Ruleset: " + selectedRuleset.getName());
        moveListView.getItems().add("Max Attempts: " + maxAttempts);
        moveListView.getItems().add("─────────────────");

        // Run simulation in background thread
        new Thread(() -> {
            try {
                // Simulate running (replace with actual simulation logic)
                Thread.sleep(1000);
                
                // Simulate random outcome for demo
                String outcome = Math.random() > 0.5 ? "Success" : "Fail";
                String details = outcome.equals("Success") 
                    ? "Robot completed course successfully" 
                    : "Robot encountered obstacle";

                // Save to database
                simulationDAO.saveSimulationRun(
                    selectedLayout.getId(),
                    selectedRuleset.getId(),
                    outcome,
                    details
                );

                // Update UI
                Platform.runLater(() -> {
                    moveListView.getItems().add("✓ Simulation complete!");
                    moveListView.getItems().add("Outcome: " + outcome);
                    moveListView.getItems().add(details);
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Simulation Complete");
                    alert.setHeaderText("Simulation finished!");
                    alert.setContentText("Result: " + outcome + "\n" + details);
                    alert.showAndWait();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    moveListView.getItems().add("✗ Error: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void displayLayout(Layout layout) {
        // Clear the grid first
        factoryGrid.getChildren().clear();

        // Parse grid size and obstacles from JSON
        // Example gridData: {"size": "10x10", "obstacles": [[2,3], [4,5]]}
        String gridData = layout.getGridData();
        
        // DEBUG: Print the actual gridData
        System.out.println("=== Layout: " + layout.getName() + " ===");
        System.out.println("GridData: " + gridData);
        
        java.util.List<int[]> obstacles = new java.util.ArrayList<>();
        
        // Simple JSON parsing - handles both formats
        try {
            if (gridData.contains("obstacles")) {
                // Find obstacles array
                int obstaclesStart = gridData.indexOf("\"obstacles\":");
                if (obstaclesStart != -1) {
                    String afterObstacles = gridData.substring(obstaclesStart + 12);
                    int arrayStart = afterObstacles.indexOf("[");
                    int arrayEnd = afterObstacles.indexOf("]");
                    
                    if (arrayStart != -1 && arrayEnd != -1) {
                        String obstaclesContent = afterObstacles.substring(arrayStart + 1, arrayEnd);
                        
                        if (!obstaclesContent.trim().isEmpty()) {
                            // Handle {"x":2,"y":3} format
                            if (obstaclesContent.contains("\"x\"")) {
                                String[] obstacleObjects = obstaclesContent.split("\\},\\{");
                                for (String obj : obstacleObjects) {
                                    obj = obj.replace("{", "").replace("}", "");
                                    String[] parts = obj.split(",");
                                    
                                    int x = -1, y = -1;
                                    for (String part : parts) {
                                        if (part.contains("\"x\"")) {
                                            x = Integer.parseInt(part.split(":")[1].trim());
                                        } else if (part.contains("\"y\"")) {
                                            y = Integer.parseInt(part.split(":")[1].trim());
                                        }
                                    }
                                    if (x != -1 && y != -1) {
                                        obstacles.add(new int[]{x, y});
                                    }
                                }
                            }
                            // Handle [[2,3],[4,5]] format
                            else if (obstaclesContent.contains("[")) {
                                obstaclesContent = obstaclesContent.replace("[", "").replace("]", "");
                                String[] pairs = obstaclesContent.split(",");
                                for (int i = 0; i < pairs.length - 1; i += 2) {
                                    int x = Integer.parseInt(pairs[i].trim());
                                    int y = Integer.parseInt(pairs[i + 1].trim());
                                    obstacles.add(new int[]{x, y});
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing gridData: " + e.getMessage());
            e.printStackTrace();
        }
        
        // DEBUG: Print parsed obstacles
        System.out.println("Found " + obstacles.size() + " obstacles:");
        for (int[] obs : obstacles) {
            System.out.println("  Position: [" + obs[0] + ", " + obs[1] + "]");
        }
        System.out.println("================\n");

        // Create 10x10 grid
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                javafx.scene.layout.StackPane cell = new javafx.scene.layout.StackPane();
                cell.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
                cell.setPrefSize(40, 40);
                
                // Check if this cell is an obstacle
                final int currentRow = row;
                final int currentCol = col;
                boolean isObstacle = obstacles.stream()
                    .anyMatch(obs -> obs[0] == currentCol && obs[1] == currentRow); // x=col, y=row
                
                if (isObstacle) {
                    cell.setStyle("-fx-background-color: #F44336; -fx-border-color: #ddd;");
                    Label obstacleLabel = new Label("🚧");
                    obstacleLabel.setStyle("-fx-font-size: 20px;");
                    cell.getChildren().add(obstacleLabel);
                }
                
                factoryGrid.add(cell, col, row);
            }
        }

        // Add robot at starting position (0,0)
        javafx.scene.layout.StackPane startCell = new javafx.scene.layout.StackPane();
        startCell.setStyle("-fx-background-color: #2196F3; -fx-border-color: #ddd;");
        startCell.setPrefSize(40, 40);
        
        Label robotLabel = new Label("🤖");
        robotLabel.setStyle("-fx-font-size: 20px;");
        startCell.getChildren().add(robotLabel);
        
        factoryGrid.add(startCell, 0, 0);

        // Add goal position at (9,9)
        javafx.scene.layout.StackPane goalCell = new javafx.scene.layout.StackPane();
        goalCell.setStyle("-fx-background-color: #4CAF50; -fx-border-color: #ddd;");
        goalCell.setPrefSize(40, 40);
        Label goalLabel = new Label("🎯");
        goalLabel.setStyle("-fx-font-size: 20px;");
        goalCell.getChildren().add(goalLabel);
        factoryGrid.add(goalCell, 9, 9);
    }
}