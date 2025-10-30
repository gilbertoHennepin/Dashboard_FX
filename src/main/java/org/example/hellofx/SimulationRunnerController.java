package org.example.hellofx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class SimulationRunnerController {

    @FXML
    private ComboBox<String> layoutComboBox;

    @FXML
    private ComboBox<String> rulesetComboBox;

    @FXML
    private Spinner<Integer> maxAttemptsSpinner;

    @FXML
    private Button runButton;

    @FXML
    private Button backButton;

    @FXML
    private GridPane factoryGrid;

    @FXML
    private ListView<String> moveListView;

    private int robotRow = 0;
    private int robotCol = 0;
    private int exitRow = 2;
    private int exitCol = 5;
    private String robotDirection = "EAST"; // NORTH, SOUTH, EAST, WEST

    // 10x10 grid layout (0 = open, 1 = wall)
    private int[][] layout = new int[10][10];

    @FXML
    private void initialize() {
        System.out.println("SimulationRunner initialized!");

        // Setup spinner
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100);
        maxAttemptsSpinner.setValueFactory(valueFactory);

        // Load sample data
        layoutComboBox.setItems(FXCollections.observableArrayList(
                "Test Layout 1", "Test Layout 2"
        ));
        layoutComboBox.getSelectionModel().selectFirst();

        rulesetComboBox.setItems(FXCollections.observableArrayList(
                "Test Ruleset 1", "Test Ruleset 2"
        ));
        rulesetComboBox.getSelectionModel().selectFirst();

        // Initialize the sample layout
        initializeSampleLayout();

        // Draw the initial grid
        drawGrid();
    }

    private void initializeSampleLayout() {
        // Create a sample layout with some walls
        // All cells start as 0 (open)
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                layout[i][j] = 0;
            }
        }

        // Add some walls (based on your screenshot)
        // Row 0, columns 5-6 (pink/wall area)
        layout[0][5] = 1;
        layout[0][6] = 1;

        // You can add more walls here to match your layout

        // Set starting position
        robotRow = 4;
        robotCol = 2;

        // Set exit position
        exitRow = 4;
        exitCol = 6;
    }

    private void drawGrid() {
        // Clear existing grid
        factoryGrid.getChildren().clear();

        // Set grid gap
        factoryGrid.setHgap(1);
        factoryGrid.setVgap(1);

        // Create 10x10 grid of cells
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                StackPane cell = createCell(row, col);
                factoryGrid.add(cell, col, row);
            }
        }
    }

    private StackPane createCell(int row, int col) {
        StackPane cell = new StackPane();
        cell.setPrefSize(35, 35); // Size of each cell
        cell.setMinSize(35, 35);
        cell.setMaxSize(35, 35);

        // Default style
        String style = "-fx-border-color: #cccccc; -fx-border-width: 1;";

        // Check what should be in this cell
        if (layout[row][col] == 1) {
            // Wall - gray
            style += "-fx-background-color: #808080;";
        } else if (row == robotRow && col == robotCol) {
            // Robot - green
            style += "-fx-background-color: #90EE90;";

            // Add direction indicator
            Label arrow = new Label(getDirectionArrow());
            arrow.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
            cell.getChildren().add(arrow);
        } else if (row == exitRow && col == exitCol) {
            // Exit - pink
            style += "-fx-background-color: #FFB6C1;";
        } else {
            // Open space - white
            style += "-fx-background-color: white;";
        }

        cell.setStyle(style);
        return cell;
    }

    private String getDirectionArrow() {
        switch (robotDirection) {
            case "NORTH": return "▲";
            case "SOUTH": return "▼";
            case "EAST": return "►";
            case "WEST": return "◄";
            default: return "●";
        }
    }

    @FXML
    private void handleRun() {
        String selectedLayout = layoutComboBox.getValue();
        String selectedRuleset = rulesetComboBox.getValue();
        int maxAttempts = maxAttemptsSpinner.getValue();

        if (selectedLayout == null || selectedRuleset == null) {
            showAlert("Please select both a layout and ruleset!");
            return;
        }

        // Clear previous moves
        moveListView.getItems().clear();

        // Reset robot position
        robotRow = 4;
        robotCol = 2;
        robotDirection = "EAST";
        drawGrid();

        // Start simulation
        runSimulation(maxAttempts);
    }

    private void runSimulation(int maxAttempts) {
        Timeline timeline = new Timeline();

        // Define a sequence of moves (this is where you'd apply your rules)
        String[] moves = {
                "FORWARD", "FORWARD", "FORWARD", "FORWARD",
                "LEFT", "FORWARD", "FORWARD"
        };

        for (int i = 0; i < Math.min(moves.length, maxAttempts); i++) {
            final int step = i;
            final String move = moves[i];

            KeyFrame frame = new KeyFrame(
                    Duration.millis(800 * (i + 1)), // 800ms between moves
                    event -> {
                        // Execute the move
                        executeMove(move);

                        // Update the grid
                        drawGrid();

                        // Add to move list
                        String moveDescription = getMoveDescription(move);
                        moveListView.getItems().add((step + 1) + ". " + moveDescription);

                        // Auto-scroll to bottom
                        moveListView.scrollTo(moveListView.getItems().size() - 1);

                        // Check if reached exit
                        if (robotRow == exitRow && robotCol == exitCol) {
                            timeline.stop();
                            moveListView.getItems().add("EXIT FOUND!");
                            showAlert("Exit found in " + (step + 1) + " moves!");
                        } else if (step >= maxAttempts - 1) {
                            showAlert("Max attempts reached. Exit not found.");
                        }
                    }
            );

            timeline.getKeyFrames().add(frame);
        }

        timeline.play();
    }

    private void executeMove(String move) {
        switch (move) {
            case "FORWARD":
                moveForward();
                break;
            case "LEFT":
                turnLeft();
                break;
            case "RIGHT":
                turnRight();
                break;
            case "BACK":
                turnAround();
                break;
        }
    }

    private void moveForward() {
        int newRow = robotRow;
        int newCol = robotCol;

        switch (robotDirection) {
            case "NORTH": newRow--; break;
            case "SOUTH": newRow++; break;
            case "EAST": newCol++; break;
            case "WEST": newCol--; break;
        }

        // Check if move is valid (not out of bounds and not a wall)
        if (newRow >= 0 && newRow < 10 && newCol >= 0 && newCol < 10 && layout[newRow][newCol] != 1) {
            robotRow = newRow;
            robotCol = newCol;
        }
    }

    private void turnLeft() {
        switch (robotDirection) {
            case "NORTH": robotDirection = "WEST"; break;
            case "WEST": robotDirection = "SOUTH"; break;
            case "SOUTH": robotDirection = "EAST"; break;
            case "EAST": robotDirection = "NORTH"; break;
        }
    }

    private void turnRight() {
        switch (robotDirection) {
            case "NORTH": robotDirection = "EAST"; break;
            case "EAST": robotDirection = "SOUTH"; break;
            case "SOUTH": robotDirection = "WEST"; break;
            case "WEST": robotDirection = "NORTH"; break;
        }
    }

    private void turnAround() {
        switch (robotDirection) {
            case "NORTH": robotDirection = "SOUTH"; break;
            case "SOUTH": robotDirection = "NORTH"; break;
            case "EAST": robotDirection = "WEST"; break;
            case "WEST": robotDirection = "EAST"; break;
        }
    }

    private String getMoveDescription(String move) {
        switch (move) {
            case "FORWARD": return "Move Forward";
            case "LEFT": return "Turn Left";
            case "RIGHT": return "Turn Right";
            case "BACK": return "Turn Around";
            default: return move;
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Simulation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}