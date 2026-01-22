package org.example.hellofx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.example.hellofx.dba.LayoutDAO;
import org.example.hellofx.dataClasses.FactoryLayout;

import java.io.IOException;

public class Scene2Controller {

    @FXML
    private TextField layoutNameField;

    @FXML
    private Button saveButton;

    @FXML
    private GridPane gridPane;

    @FXML
    private Label statusLabel;

    @FXML
    private ToggleGroup toolToggleGroup;

    @FXML
    private RadioButton startToolButton;

    @FXML
    private RadioButton exitToolButton;

    @FXML
    private RadioButton obstacleToolButton;

    @FXML
    private RadioButton eraseToolButton;

    private static final int GRID_SIZE = 10;
    private static final int CELL_SIZE = 40;

    // Grid state: 0 = empty, 1 = obstacle, 2 = start, 3 = exit
    private int[][] gridState = new int[GRID_SIZE][GRID_SIZE];
    private Region[][] cellRegions = new Region[GRID_SIZE][GRID_SIZE];

    @FXML
    public void initialize() {
        createGrid();
        updateStatusLabel();
    }

    private void createGrid() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Region cell = new Region();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1;");

                final int r = row;
                final int c = col;

                // Add click handler
                cell.setOnMouseClicked(event -> handleCellClick(r, c));

                gridPane.add(cell, col, row);
                cellRegions[row][col] = cell;
            }
        }
    }

    private void handleCellClick(int row, int col) {
        RadioButton selectedTool = (RadioButton) toolToggleGroup.getSelectedToggle();

        if (selectedTool == startToolButton) {
            // Remove any existing start position
            clearCellType(2);
            gridState[row][col] = 2;
            updateCellAppearance(row, col);
            statusLabel.setText("Robot start set at (" + row + ", " + col + ")");
        } else if (selectedTool == exitToolButton) {
            // Remove any existing exit position
            clearCellType(3);
            gridState[row][col] = 3;
            updateCellAppearance(row, col);
            statusLabel.setText("Exit/Goal set at (" + row + ", " + col + ")");
        } else if (selectedTool == obstacleToolButton) {
            if (gridState[row][col] == 1) {
                // Toggle off if already obstacle
                gridState[row][col] = 0;
                statusLabel.setText("Obstacle removed at (" + row + ", " + col + ")");
            } else {
                gridState[row][col] = 1;
                statusLabel.setText("Obstacle placed at (" + row + ", " + col + ")");
            }
            updateCellAppearance(row, col);
        } else if (selectedTool == eraseToolButton) {
            gridState[row][col] = 0;
            updateCellAppearance(row, col);
            statusLabel.setText("Cell cleared at (" + row + ", " + col + ")");
        }
    }

    private void clearCellType(int type) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (gridState[row][col] == type) {
                    gridState[row][col] = 0;
                    updateCellAppearance(row, col);
                }
            }
        }
    }

    private void updateCellAppearance(int row, int col) {
        Region cell = cellRegions[row][col];
        String color = switch (gridState[row][col]) {
            case 1 -> "#424242"; // Obstacle - dark gray
            case 2 -> "#4CAF50"; // Start - green
            case 3 -> "#2196F3"; // Exit - blue
            default -> "white";  // Empty
        };
        cell.setStyle("-fx-background-color: " + color + "; -fx-border-color: #ccc; -fx-border-width: 1;");
    }

    @FXML
    public void clearGrid() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                gridState[row][col] = 0;
                updateCellAppearance(row, col);
            }
        }
        statusLabel.setText("Grid cleared");
    }

    @FXML
    public void saveLayout() {
        String layoutName = layoutNameField.getText().trim();

        if (layoutName.isEmpty()) {
            showAlert("Error", "Please enter a layout name", Alert.AlertType.ERROR);
            return;
        }

        // Check if start and exit are set
        boolean hasStart = false;
        boolean hasExit = false;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (gridState[row][col] == 2) hasStart = true;
                if (gridState[row][col] == 3) hasExit = true;
            }
        }

        if (!hasStart || !hasExit) {
            showAlert("Error", "Please set both robot start position and exit/goal", Alert.AlertType.ERROR);
            return;
        }

        try {
            FactoryLayout layout = new FactoryLayout();
            layout.setName(layoutName);
            layout.setGridState(gridState);

            LayoutDAO layoutDAO = new LayoutDAO(DatabaseConfig.getConnection());
            layoutDAO.saveLayout(layout);

            showAlert("Success", "Layout '" + layoutName + "' saved successfully!", Alert.AlertType.INFORMATION);
            statusLabel.setText("Layout saved to database");
            layoutNameField.clear();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save layout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateStatusLabel() {
        RadioButton selected = (RadioButton) toolToggleGroup.getSelectedToggle();
        if (selected != null) {
            statusLabel.setText("Tool selected: " + selected.getText());
        }
    }

    @FXML
    public void switchToSummary(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Summary.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}