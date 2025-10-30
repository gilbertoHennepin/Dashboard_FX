package Sim_runner;

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
import javafx.collections.FXCollections;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
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
    private int currentStep = 0;

    @FXML
    private void initialize() {
        // Setup spinner
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100);
        maxAttemptsSpinner.setValueFactory(valueFactory);

        // Load sample data (you'll replace this with actual data later)
        layoutComboBox.setItems(FXCollections.observableArrayList(
                "Test Layout 1", "Test Layout 2"
        ));
        layoutComboBox.getSelectionModel().selectFirst();

        rulesetComboBox.setItems(FXCollections.observableArrayList(
                "Test Ruleset 1", "Test Ruleset 2"
        ));
        rulesetComboBox.getSelectionModel().selectFirst();

        // Initialize the grid
        initializeGrid();
    }

    private void initializeGrid() {
        // Create a 4x4 grid
        factoryGrid.getChildren().clear();

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(80, 80);
                cell.setStyle("-fx-border-color: black; -fx-background-color: white;");
                factoryGrid.add(cell, col, row);
            }
        }

        // Place robot at starting position (example: 0,0)
        updateRobotPosition(0, 0);
    }

    private void updateRobotPosition(int row, int col) {
        // Clear all cells first
        factoryGrid.getChildren().forEach(node -> {
            if (node instanceof StackPane) {
                node.setStyle("-fx-border-color: black; -fx-background-color: white;");
            }
        });

        // Highlight robot position in green
        Node robotCell = getNodeFromGridPane(factoryGrid, col, row);
        if (robotCell != null) {
            robotCell.setStyle("-fx-border-color: black; -fx-background-color: #90EE90;");
        }

        robotRow = row;
        robotCol = col;
    }

    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            Integer nodeCol = GridPane.getColumnIndex(node);
            Integer nodeRow = GridPane.getRowIndex(node);

            if (nodeCol == null) nodeCol = 0;
            if (nodeRow == null) nodeRow = 0;

            if (nodeCol == col && nodeRow == row) {
                return node;
            }
        }
        return null;
    }

    @FXML
    private void handleRun() {
        String layout = layoutComboBox.getValue();
        String ruleset = rulesetComboBox.getValue();
        int maxAttempts = maxAttemptsSpinner.getValue();

        if (layout == null || ruleset == null) {
            showAlert("Please select both a layout and ruleset!");
            return;
        }

        // Clear previous moves
        moveListView.getItems().clear();
        currentStep = 0;

        // Start simulation (simple example - moves robot in a pattern)
        runSimulation(maxAttempts);
    }

    private void runSimulation(int maxAttempts) {
        Timeline timeline = new Timeline();

        // Example: Move robot in a simple pattern
        // You'll replace this with actual rule evaluation
        int[][] moves = {
                {0, 1}, // Move right
                {1, 1}, // Move down-right
                {1, 2}, // Move right
                {2, 2}  // Move down
        };

        for (int i = 0; i < Math.min(moves.length, maxAttempts); i++) {
            final int step = i;
            KeyFrame frame = new KeyFrame(
                    Duration.millis(1000 * (i + 1)),
                    event -> {
                        // Update robot position
                        updateRobotPosition(moves[step][0], moves[step][1]);

                        // Add move to list
                        moveListView.getItems().add((step + 1) + ". Move Forward");

                        // Check if done
                        if (step >= moves.length - 1) {
                            showAlert("Simulation complete!");
                        }
                    }
            );
            timeline.getKeyFrames().add(frame);
        }

        timeline.play();
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        // Go back to dashboard
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