package org.example.hellofx.dba;

import org.example.hellofx.dba.DashboardDAO;
import org.example.hellofx.dataClasses.DashboardSummary;
import org.example.hellofx.dataClasses.RecentSimulationRun;
import org.example.hellofx.DatabaseConfig;
import java.sql.Connection;


public class TestDatabaseConnection {
    public static void main(String[] args) {
        System.out.println("Testing Database Connection...\n");

        try {
            // Get database connection
            Connection conn = DatabaseConfig.getConnection();
            System.out.println("✓ Database connected successfully!\n");

            // Create DAO
            DashboardDAO dao = new DashboardDAO(conn);

            // Fetch dashboard summary
            DashboardSummary summary = dao.getDashboardSummary();

            // Display results
            System.out.println("=== DASHBOARD SUMMARY ===");
            System.out.println("Total Layouts: " + summary.getTotalLayouts());
            System.out.println("Total Rulesets: " + summary.getTotalRulesets());
            System.out.println("\n=== RECENT SIMULATION RUNS ===");

            for (RecentSimulationRun run : summary.getRecentRuns()) {
                System.out.println("\n" + run.getTimestamp() + " - " + run.getOutcome());
                System.out.println("  Layout: " + run.getLayoutName());
                System.out.println("  Ruleset: " + run.getRulesetName());
                System.out.println("  Details: " + run.getDetails());
            }

            System.out.println("\n✓ Test completed successfully!");

            // Close connection
            DatabaseConfig.closeConnection();

        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}