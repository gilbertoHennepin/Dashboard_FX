package org.example.hellofx;

import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.io.IOException;

public class HelloController {

    @FXML
    private VBox createCard;
    
    @FXML
    private VBox summaryCard;
    
    @FXML
    private VBox simulationCard;
    
    @FXML
    private VBox quickAccessCard;
    
    // Store original styles for hover effects
    private String createCardOriginalStyle;
    private String summaryCardOriginalStyle;
    private String simulationCardOriginalStyle;
    private String quickAccessCardOriginalStyle;
    
    @FXML
    public void initialize() {
        // Store original styles
        if (createCard != null) {
            createCardOriginalStyle = createCard.getStyle();
            setupHoverEffect(createCard, createCardOriginalStyle);
        }
        if (summaryCard != null) {
            summaryCardOriginalStyle = summaryCard.getStyle();
            setupHoverEffect(summaryCard, summaryCardOriginalStyle);
        }
        if (simulationCard != null) {
            simulationCardOriginalStyle = simulationCard.getStyle();
            setupHoverEffect(simulationCard, simulationCardOriginalStyle);
        }
        if (quickAccessCard != null) {
            quickAccessCardOriginalStyle = quickAccessCard.getStyle();
            setupHoverEffect(quickAccessCard, quickAccessCardOriginalStyle);
        }
    }
    
    private void setupHoverEffect(VBox card, String originalStyle) {
        // Create scale transitions for smooth animation
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
        scaleUp.setToX(1.04);
        scaleUp.setToY(1.04);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        
        card.setOnMouseEntered(e -> {
            // Update style with hover effects
            card.setStyle(originalStyle + 
                " -fx-border-color: #a855f7; " +
                "-fx-effect: dropshadow(gaussian, #a855f7a0, 25, 0.6, 0, 6);");
            scaleUp.play();
        });
        
        card.setOnMouseExited(e -> {
            // Restore original style
            card.setStyle(originalStyle);
            scaleDown.play();
        });
    }

    // Helper method to switch scenes
    private void loadScene(String fxmlFile) throws IOException {
        try {
            // Try relative path first (same package)
            java.net.URL resource = getClass().getResource(fxmlFile);
            
            // If not found, try absolute path
            if (resource == null) {
                resource = getClass().getResource("/org/example/hellofx/" + fxmlFile);
            }
            
            if (resource == null) {
                System.err.println("ERROR: Could not find resource: " + fxmlFile);
                System.err.println("Tried: " + fxmlFile + " and /org/example/hellofx/" + fxmlFile);
                System.err.println("Package: " + getClass().getPackage().getName());
                return;
            }
            
            System.out.println("Loading FXML from: " + resource);
            Parent root = FXMLLoader.load(resource);
            
            if (root == null) {
                System.err.println("ERROR: Failed to load FXML: " + fxmlFile);
                return;
            }
            
            System.out.println("FXML loaded successfully, switching scene...");
            HelloApplication.switchScene(root);
        } catch (Exception e) {
            System.err.println("ERROR loading scene: " + fxmlFile);
            e.printStackTrace();
            throw new IOException("Failed to load " + fxmlFile, e);
        }
    }
    
    // Helper method to switch scenes and refresh controller
    private void loadSceneWithRefresh(String fxmlFile) throws IOException {
        try {
            // Try relative path first (same package)
            java.net.URL resource = getClass().getResource(fxmlFile);
            
            // If not found, try absolute path
            if (resource == null) {
                resource = getClass().getResource("/org/example/hellofx/" + fxmlFile);
            }
            
            if (resource == null) {
                System.err.println("ERROR: Could not find resource: " + fxmlFile);
                return;
            }
            
            System.out.println("Loading FXML with refresh from: " + resource);
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            
            // Special handling for Summary page
            if (fxmlFile.equals("Summary.fxml")) {
                DashboardController controller = loader.getController();
                if (controller != null) {
                    controller.refreshData();
                }
            }
            
            HelloApplication.switchScene(root);
        } catch (Exception e) {
            System.err.println("ERROR loading scene with refresh: " + fxmlFile);
            e.printStackTrace();
            throw new IOException("Failed to load " + fxmlFile, e);
        }
    }

    // DASHBOARD/SUMMARY
    public void switchScene6(ActionEvent event) {
        try {
            loadSceneWithRefresh("Summary.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CREATE
    public void switchScene2(ActionEvent event) {
        try {
            loadScene("Scene2.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // REPORTS
    public void switchScene3(ActionEvent event) {
        try {
            loadScene("Scene3.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // FACTORY LAYOUT
    public void switchScene4(ActionEvent event) {
        try {
            loadScene("Scene4.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // RULESET
    public void switchScene5(ActionEvent event) {
        try {
            loadScene("Scene5.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // SIMULATION RUNNER
    public void SimulationRunner(ActionEvent event) {
        try {
            loadScene("SimulationRunner.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}