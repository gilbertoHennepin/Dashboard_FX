package org.example.hellofx.dba;

import org.example.hellofx.dataClasses.Layout;
import org.example.hellofx.dataClasses.Ruleset;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimulationDAO {
    private Connection connection;

    public SimulationDAO(Connection connection) {
        this.connection = connection;
    }

    // Get all layouts
    public List<Layout> getAllLayouts() throws SQLException {
        String sql = "SELECT id, name, grid_data, created_at, updated_at FROM layouts ORDER BY name";
        List<Layout> layouts = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Layout layout = new Layout();
                layout.setId(rs.getInt("id"));
                layout.setName(rs.getString("name"));
                layout.setGridData(rs.getString("grid_data"));
                
                Timestamp created = rs.getTimestamp("created_at");
                if (created != null) {
                    layout.setCreatedAt(created.toLocalDateTime());
                }
                
                Timestamp updated = rs.getTimestamp("updated_at");
                if (updated != null) {
                    layout.setUpdatedAt(updated.toLocalDateTime());
                }
                
                layouts.add(layout);
            }
        }

        return layouts;
    }

    // Get all rulesets
    public List<Ruleset> getAllRulesets() throws SQLException {
        String sql = "SELECT id, name, rules_json, created_at, updated_at FROM rulesets ORDER BY name";
        List<Ruleset> rulesets = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Ruleset ruleset = new Ruleset();
                ruleset.setId(rs.getInt("id"));
                ruleset.setName(rs.getString("name"));
                ruleset.setRulesJson(rs.getString("rules_json"));
                
                Timestamp created = rs.getTimestamp("created_at");
                if (created != null) {
                    ruleset.setCreatedAt(created.toLocalDateTime());
                }
                
                Timestamp updated = rs.getTimestamp("updated_at");
                if (updated != null) {
                    ruleset.setUpdatedAt(updated.toLocalDateTime());
                }
                
                rulesets.add(ruleset);
            }
        }

        return rulesets;
    }

    // Save simulation run result
    public void saveSimulationRun(int layoutId, int rulesetId, String outcome, String details) throws SQLException {
        String sql = "INSERT INTO simulation_runs (layout_id, ruleset_id, outcome, details) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, layoutId);
            pstmt.setInt(2, rulesetId);
            pstmt.setString(3, outcome);
            pstmt.setString(4, details);
            pstmt.executeUpdate();
        }
    }
}