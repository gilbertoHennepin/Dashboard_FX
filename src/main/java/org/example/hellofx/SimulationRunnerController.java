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


// CLASS DECLARATIONS & AND FIELDS
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


    // SIM VARIABLES
    private int robotRow = 0; // ROBOT STARTS AT POSITION (0,0)
    private int robotCol = 0; // ROBOT STARTS AT POSITION (0,0)
    private int exitRow = 2;  // PINK SQUARE'S POSITION
    private int exitCol = 5;  // PINK SQUARE'S POSITION
    private String robotDirection = "EAST"; // ROBOT FACES EAST

    //GRID
    private int[][] layout = new int[10][10];  // 10x10 grid layout WHERE  (0 = open, 1 = wall)


    // INITIALIZE METHOD | RUNS AUTOMATICALLY WHEN THE FXML LOADS
    @FXML
    private void initialize() {
        System.out.println("SimulationRunner initialized!");

        // SETUP SPINNER | ALLOWS USER TO SELECT HOW MANY MOVES THE ROBOT CAN TRY
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100);
        maxAttemptsSpinner.setValueFactory(valueFactory);

        // ADDS OPTIONS FOR DROPDOWN MENUS
        // "selectFirst()" AUTOMATICALLY SELECTS THE TEST & RULESET 1
        //TODO  *****WILL LATER CONNECT THIS TO THE DATA FROM DATABASE*****

        layoutComboBox.setItems(FXCollections.observableArrayList(
                "Test Layout 1", "Test Layout 2"
        ));
        layoutComboBox.getSelectionModel().selectFirst();

        rulesetComboBox.setItems(FXCollections.observableArrayList(
                "Test Ruleset 1", "Test Ruleset 2"
        ));
        rulesetComboBox.getSelectionModel().selectFirst();

        // INITIALIZE THE GRID
        initializeSampleLayout(); // SETS UP WHICH CELLS ARE WALLS AND WHICH ARE OPEN

        // DRAW'S ALL THE CELLS ON SCREEN
        drawGrid();
    }

    // INITIALIZE SAMPLE LAYOUT
    private void initializeSampleLayout() {
        // Create a sample layout with some walls
        // LOOPS THROUGH THE 100 CELLS * SETS THEM TO 0 (OPEN/WHITE)
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                layout[i][j] = 0;
            }
        }

        // Add some walls | (pink/wall area)
        layout[0][5] = 1;
        layout[1][5] = 1;
        layout[3][5] = 1;
        layout[4][5] = 1;
        layout[0][6] = 1;
        layout[5][6] = 1;
        layout[4][7] = 1;
        layout[3][7] = 1;
        layout[2][7] = 1;


        // Set starting position
        robotRow = 0;
        robotCol = 0;

        // Set exit position
        exitRow = 4;
        exitCol = 6;
    }

    private void drawGrid() {
        // Clear existing grid || REMOVES OLD CELLS (IF REDRAWING)
        factoryGrid.getChildren().clear();

        // Set grid gap || 1 PIXEL BETWEEN CELLS
        factoryGrid.setHgap(1);
        factoryGrid.setVgap(1);

        // Create 10x10 grid of cells
        for (int row = 0; row < 10; row++) { // LOOPS THROUGH ALL 100 POSITIONS
            for (int col = 0; col < 10; col++) {
                StackPane cell = createCell(row, col); // CREATES A CELL FOR EACH POSITION
                factoryGrid.add(cell, col, row); // ADDS CELL TO GRIDPANE (COLUMN, ROW)
            }
        }
    }

    //CREATE CELL (FOR EACH BOX)
    private StackPane createCell(int row, int col) {
        StackPane cell = new StackPane();
        cell.setPrefSize(35, 35); // Size of each cell
        cell.setMinSize(35, 35);
        cell.setMaxSize(35, 35);


        // Default style
        // LIGHT GRAY BORDER
        String style = "-fx-border-color: #cccccc; -fx-border-width: 1;";

        // CHECK IF THE POSITION IS A WALL (= 1) IF SO MAKES IT GRAY
        if (layout[row][col] == 1) {
            // Wall - gray
            style += "-fx-background-color: #808080;";

            // CHECKS WHERE THE ROBOT IS, IF YES IT MAKES IT GREEN
        } else if (row == robotRow && col == robotCol) {
            // Robot - green
            style += "-fx-background-color: #90EE90;";

            // ADDS ARROW INSIDE CELL DEPENDING WHERE THE ROBOT IS FACING
            Label arrow = new Label(getDirectionArrow());
            arrow.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
            cell.getChildren().add(arrow);

            // CHECKS FOR EXIT POSITION, IF YES MAKES IT PINK/RED
        } else if (row == exitRow && col == exitCol) {
            // Exit - pink
            style += "-fx-background-color: #FFB6C1;";

            // IF NOT A WALL ROBOT OR EXIT MAKES IT WHITE (OPEN SPACE)
        } else {
            // Open space - white
            style += "-fx-background-color: white;";
        }

        cell.setStyle(style);
        return cell;
    }

    //GET DIRECTION ARROW & RETURNS WHERE ITS FACING
    private String getDirectionArrow() {
        switch (robotDirection) {
            case "NORTH": return "▲";
            case "SOUTH": return "▼";
            case "EAST": return "►";
            case "WEST": return "◄";
            default: return "●";
        }
    }

    // HANDLE RUN BUTTON
    // GETS VALUES FORM THE DROPWDOWNS AND SPINNER
    //CHECKS IF USER HAS SELECTED A LAYOUT AND SPINNER
    // IF NOT SHOWS AN ERROR AND STOPS
    @FXML
    private void handleRun() {
        String selectedLayout = layoutComboBox.getValue();
        String selectedRuleset = rulesetComboBox.getValue();
        int maxAttempts = maxAttemptsSpinner.getValue();

        if (selectedLayout == null || selectedRuleset == null) {
            showAlert("Please select both a layout and ruleset!");
            return;
        }

        // Clear previous moves from previous runs
        moveListView.getItems().clear();

        // Reset robot position
        robotRow = 0;
        robotCol = 0;
        robotDirection = "EAST";
        drawGrid();

        // Start simulation
        runSimulation(maxAttempts);
    }

    // RUN SIMULATION
    private void runSimulation(int maxAttempts) {
        Timeline timeline = new Timeline();

        // We'll calculate moves dynamically based on walls
        for (int i = 0; i < maxAttempts; i++) {
            final int step = i;

            KeyFrame frame = new KeyFrame(
                    Duration.millis(800 * (i + 1)),
                    event -> {
                        // Check if exit is in any adjacent cell
                        String exitDirection = findExitDirection();

                        if (exitDirection != null) {
                            // Exit is nearby! Go directly to it
                            turnToFace(exitDirection);
                            moveForward();
                            moveListView.getItems().add((step + 1) + ". Move toward EXIT!");
                        } else {
                            // No exit nearby, follow wall-following algorithm

                            // Check if we can move forward
                            turnRight();
                            if (canMoveForward()) {
                                moveForward();
                                moveListView.getItems().add((step + 1) + ". Turn Right & Move Forward");
                            } else {
                                // Wall ahead! Turn left
                                turnLeft();

                                if (canMoveForward()) {
                                    moveForward();
                                    moveListView.getItems().add((step + 1) + ". Move Forward");
                                } else {
                                    turnLeft();
                                    if (canMoveForward()) {
                                        moveForward();
                                        moveListView.getItems().add((step + 1) + ". Turn Left & Move Forward");
                                    } else {
                                        turnLeft();
                                        if (canMoveForward()) {
                                            moveForward();
                                            moveListView.getItems().add((step + 1) + ". Turn Around & Move Forward");
                                        } else {
                                            moveListView.getItems().add((step + 1) + ". STUCK!");
                                        }
                                    }
                                }
                            }
                        }

                        // Update the grid
                        drawGrid();

                        // Auto-scroll
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

    // Find exit direction (returns direction if exit is adjacent, null otherwise)
    private String findExitDirection() {
        // Check NORTH
        if (robotRow - 1 >= 0 && robotRow - 1 == exitRow && robotCol == exitCol) {
            return "NORTH";
        }

        // Check SOUTH
        if (robotRow + 1 < 10 && robotRow + 1 == exitRow && robotCol == exitCol) {
            return "SOUTH";
        }

        // Check EAST
        if (robotCol + 1 < 10 && robotRow == exitRow && robotCol + 1 == exitCol) {
            return "EAST";
        }

        // Check WEST
        if (robotCol - 1 >= 0 && robotRow == exitRow && robotCol - 1 == exitCol) {
            return "WEST";
        }

        return null; // Exit not adjacent
    }

    // Turn to face a specific direction
    private void turnToFace(String targetDirection) {
        while (!robotDirection.equals(targetDirection)) {
            turnRight();
        }
    }

    // Check if robot can move forward
    private boolean canMoveForward() {
        int newRow = robotRow;
        int newCol = robotCol;

        // Calculate where we'd move to
        switch (robotDirection) {
            case "NORTH": newRow--; break;
            case "SOUTH": newRow++; break;
            case "EAST": newCol++; break;
            case "WEST": newCol--; break;
        }

        // Check if the move is valid
        // Valid if: within bounds AND not a wall
        if (newRow < 0 || newRow >= 10 || newCol < 0 || newCol >= 10) {
            return false;  // Out of bounds
        }

        if (layout[newRow][newCol] == 1) {
            return false;  // Wall ahead
        }

        return true;  // Clear path!
    }


    // CALCULATES WHERE THE ROBOT MOVES BASED ON DIRECTION
    //NORTH → row - 1 (move up)
    //SOUTH → row + 1 (move down)
    //EAST → col + 1 (move right)
    //WEST → col - 1 (move left)


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

    // TUNS ROBOT 90* COUNTER CLOCKWISE
    private void turnLeft() {
        switch (robotDirection) {
            case "NORTH": robotDirection = "WEST"; break;
            case "WEST": robotDirection = "SOUTH"; break;
            case "SOUTH": robotDirection = "EAST"; break;
            case "EAST": robotDirection = "NORTH"; break;
        }
    }

    // TURNS ROBOT 90* CLOCKWISE
    private void turnRight() {
        switch (robotDirection) {
            case "NORTH": robotDirection = "EAST"; break;
            case "EAST": robotDirection = "SOUTH"; break;
            case "SOUTH": robotDirection = "WEST"; break;
            case "WEST": robotDirection = "NORTH"; break;
        }
    }

    // TURNS ROBOT AROUND 180*
    private void turnAround() {
        switch (robotDirection) {
            case "NORTH": robotDirection = "SOUTH"; break;
            case "SOUTH": robotDirection = "NORTH"; break;
            case "EAST": robotDirection = "WEST"; break;
            case "WEST": robotDirection = "EAST"; break;
        }
    }


    // LOADS THE SCENE
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