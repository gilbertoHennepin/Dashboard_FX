package org.example.hellofx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.hellofx.dba.DashboardDAO;
import org.example.hellofx.dataClasses.DashboardSummary;
import org.example.hellofx.dataClasses.RecentSimulationRun;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML
    private Label totalLayoutsLabel;

    @FXML
    private Label totalRulesetsLabel;

    @FXML
    private Label recentRunsCountLabel;

    @FXML
    private TableView<RecentSimulationRun> recentRunsTable;

    @FXML
    private TableColumn<RecentSimulationRun, String> timestampColumn;

    @FXML
    private TableColumn<RecentSimulationRun, String> layoutColumn;

    @FXML
    private TableColumn<RecentSimulationRun, String> rulesetColumn;

    @FXML
    private TableColumn<RecentSimulationRun, String> outcomeColumn;

    @FXML
    private TableColumn<RecentSimulationRun, String> detailsColumn;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        setupTableColumns();
        loadDashboardData();
    }
    // Add this method to your DashboardController class
public void refreshData() {
    loadDashboardData();
}

    private void setupTableColumns() {
        // Set up cell value factories for each column
        timestampColumn.setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getTimestamp();
            return new javafx.beans.property.SimpleStringProperty(
                timestamp != null ? timestamp.format(dateFormatter) : "N/A"
            );
        });

        layoutColumn.setCellValueFactory(new PropertyValueFactory<>("layoutName"));
        rulesetColumn.setCellValueFactory(new PropertyValueFactory<>("rulesetName"));
        outcomeColumn.setCellValueFactory(new PropertyValueFactory<>("outcome"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));

        // Style the outcome column based on Success/Fail
        outcomeColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Success")) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else if (item.equals("Fail")) {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void loadDashboardData() {
        // Load data in a background thread to avoid blocking UI
        new Thread(() -> {
            try {
                Connection conn = DatabaseConfig.getConnection();
                DashboardDAO dao = new DashboardDAO(conn);
                DashboardSummary summary = dao.getDashboardSummary();

                // Update UI on JavaFX Application Thread
                Platform.runLater(() -> {
                    totalLayoutsLabel.setText(String.valueOf(summary.getTotalLayouts()));
                    totalRulesetsLabel.setText(String.valueOf(summary.getTotalRulesets()));
                    recentRunsCountLabel.setText(String.valueOf(summary.getRecentRuns().size()));

                    ObservableList<RecentSimulationRun> runs = 
                        FXCollections.observableArrayList(summary.getRecentRuns());
                    recentRunsTable.setItems(runs);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    totalLayoutsLabel.setText("Error");
                    totalRulesetsLabel.setText("Error");
                    recentRunsCountLabel.setText("Error");
                });
            }
        }).start();
    }

    // Method to refresh dashboard data
    public void refreshDashboard() {
        loadDashboardData();
    }

    // Back button method (lowercase 's' to match FXML)
    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
        HelloApplication.switchScene(root);
    }
}