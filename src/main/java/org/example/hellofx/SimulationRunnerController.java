package org.example.hellofx;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


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

    @FXML
    private TextArea botResponseArea;

    @FXML
    private TextField commandInput;

    @FXML
    private Button sendCommandButton;


    // SIM VARIABLES
    private int robotRow = 0; // ROBOT STARTS AT POSITION (0,0)
    private int robotCol = 0; // ROBOT STARTS AT POSITION (0,0)
    private int exitRow = 2;  // PINK SQUARE'S POSITION
    private int exitCol = 5;  // PINK SQUARE'S POSITION
    private String robotDirection = "EAST"; // ROBOT FACES EAST

    //GRID
    private int[][] layout = new int[10][10];  // 10x10 grid layout WHERE  (0 = open, 1 = wall)

    // GEMINI AI CLIENT
    private Client geminiClient;
    private String systemInstructionText;
    private boolean simulationActive = false;
    private ScheduledExecutorService executorService;
    private int moveCount = 0;


    // INITIALIZE METHOD | RUNS AUTOMATICALLY WHEN THE FXML LOADS
    @FXML
    private void initialize() {
        System.out.println("SimulationRunner initialized!");

        // SETUP SPINNER | ALLOWS USER TO SELECT HOW MANY MOVES THE ROBOT CAN TRY
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100);
        maxAttemptsSpinner.setValueFactory(valueFactory);

        // ADDS OPTIONS FOR DROPDOWN MENUS
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

        // INITIALIZE GEMINI CLIENT
        initializeGemini();

        // Make command input handle Enter key
        commandInput.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleSendCommand();
            }
        });

        botResponseArea.setText("Robot ready! Give me commands to move around the factory.\nExample: 'turn right'");
        runButton.setText("Reset");
        runButton.setOnAction(event -> handleReset());
    }

    // INITIALIZE GEMINI CLIENT
    private void initializeGemini() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            botResponseArea.setText("Error: GEMINI_API_KEY environment variable not set!");
            sendCommandButton.setDisable(true);
            return;
        }

        try {
            geminiClient = Client.builder().apiKey(apiKey).build();

            // Read System Instruction from file
            try {
                systemInstructionText = new String(
                        SimulationRunnerController.class.getClassLoader()
                                .getResourceAsStream("system_instructions.txt").readAllBytes()
                );
            } catch (Exception e) {
                systemInstructionText = "You are a helpful robot navigator.";
            }

            executorService = Executors.newSingleThreadScheduledExecutor();
            simulationActive = true;
        } catch (Exception e) {
            botResponseArea.setText("Error initializing Gemini: " + e.getMessage());
            sendCommandButton.setDisable(true);
        }
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
            
        // Add some walls | (gray/wall area)
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

        // Set exit position || Pink Square
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

    // HANDLE RUN BUTTON (NOW RESET)
    @FXML
    private void handleReset() {
        robotRow = 0;
        robotCol = 0;
        robotDirection = "EAST";
        moveCount = 0;
        drawGrid();
        moveListView.getItems().clear();
        botResponseArea.setText("Robot reset to starting position (0,0) facing EAST.\nReady for new commands!");
        commandInput.clear();
        commandInput.requestFocus();
    }

    // HANDLE SEND COMMAND BUTTON
    @FXML
    private void handleSendCommand() {
        String userCommand = commandInput.getText().trim();
        if (userCommand.isEmpty()) {
            return;
        }

        commandInput.clear();
        commandInput.setDisable(true);
        sendCommandButton.setDisable(true);

        if (userCommand.equalsIgnoreCase("exit")) {
            simulationActive = false;
            botResponseArea.appendText("\nRobot shutting down. Goodbye!");
            commandInput.setDisable(true);
            sendCommandButton.setDisable(true);
            return;
        }

        // Send command to Gemini in background thread
        executorService.execute(() -> {
            try {
                String response = queryGemini(userCommand);
                Platform.runLater(() -> {
                    botResponseArea.appendText("\nYou: " + userCommand + "\n");
                    botResponseArea.appendText("Robot: " + response + "\n");
                    botResponseArea.setScrollTop(Double.MAX_VALUE); // Auto-scroll to bottom

                    // Execute the robot movement based on Gemini's response
                    executeCommand(userCommand.toLowerCase());

                    commandInput.setDisable(false);
                    sendCommandButton.setDisable(false);
                    commandInput.requestFocus();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    botResponseArea.appendText("\nError: " + e.getMessage() + "\n");
                    commandInput.setDisable(false);
                    sendCommandButton.setDisable(false);
                    commandInput.requestFocus();
                });
            }
        });
    }

    // QUERY GEMINI WITH USER COMMAND
    private String queryGemini(String userPrompt) {
        try {
            Content systemInstruction = Content.builder()
                    .parts(Part.builder()
                            .text(systemInstructionText)
                            .build())
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .temperature(0.3F)
                    .build();

            GenerateContentResponse response = geminiClient.models.generateContent(
                    "gemini-2.5-flash",
                    userPrompt,
                    config
            );

            return response.text();
        } catch (Exception e) {
            return "My apologies I can't complete this.";
        }
    }

    // EXECUTE ROBOT MOVEMENT BASED ON COMMAND
    private void executeCommand(String command) {
        command = command.toLowerCase();

        // Simple command parsing
        if (command.contains("forward") || command.contains("move forward") || command.contains("go forward")) {
            if (canMoveForward()) {
                moveForward();
                moveCount++;
                moveListView.getItems().add(moveCount + ". Move Forward - Now at (" + robotRow + "," + robotCol + ")");
            }
        } else if (command.contains("left") || command.contains("turn left")) {
            turnLeft();
            moveListView.getItems().add(moveCount + ". Turn Left - Now facing " + robotDirection);
        } else if (command.contains("right") || command.contains("turn right")) {
            turnRight();
            moveListView.getItems().add(moveCount + ". Turn Right - Now facing " + robotDirection);
        } else if (command.contains("backward") || command.contains("back")) {
            turnAround();
            if (canMoveForward()) {
                moveForward();
                moveCount++;
                moveListView.getItems().add(moveCount + ". Move Backward - Now at (" + robotRow + "," + robotCol + ")");
            }
            turnAround();
        }

        drawGrid();
        moveListView.scrollTo(moveListView.getItems().size() - 1);

        // Check if reached exit
        if (robotRow == exitRow && robotCol == exitCol) {
            botResponseArea.appendText("\n🎉 EXIT FOUND in " + moveCount + " moves! 🎉\n");
            moveListView.getItems().add("*** EXIT FOUND! ***");
        }
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
        // Clean up resources
        simulationActive = false;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }}