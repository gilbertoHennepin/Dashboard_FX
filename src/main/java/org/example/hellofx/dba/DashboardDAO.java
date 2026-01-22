package org.example.hellofx.dba;

import org.example.hellofx.dataClasses.RecentSimulationRun;
import org.example.hellofx.dataClasses.DashboardSummary;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {
    private Connection connection;

    public DashboardDAO(Connection connection) {
        this.connection = connection;
    }

    // Get total count of layouts
    public int getTotalLayouts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM layouts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // Get total count of rulesets
    public int getTotalRulesets() throws SQLException {
        String sql = "SELECT COUNT(*) FROM rulesets";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // Get the 5 most recent simulation runs
    public List<RecentSimulationRun> getRecentSimulationRuns(int limit) throws SQLException {
        String sql = """
            SELECT 
                sr.id,
                l.name as layout_name,
                r.name as ruleset_name,
                sr.outcome,
                sr.run_timestamp,
                sr.details
            FROM simulation_runs sr
            JOIN layouts l ON sr.layout_id = l.id
            JOIN rulesets r ON sr.ruleset_id = r.id
            ORDER BY sr.run_timestamp DESC
            LIMIT ?
        """;

        List<RecentSimulationRun> runs = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    RecentSimulationRun run = new RecentSimulationRun();
                    run.setId(rs.getInt("id"));
                    run.setLayoutName(rs.getString("layout_name"));
                    run.setRulesetName(rs.getString("ruleset_name"));
                    run.setOutcome(rs.getString("outcome"));
                    
                    Timestamp timestamp = rs.getTimestamp("run_timestamp");
                    run.setTimestamp(timestamp.toLocalDateTime());
                    
                    run.setDetails(rs.getString("details"));
                    runs.add(run);
                }
            }
        }

        return runs;
    }

    // Get complete dashboard summary
    public DashboardSummary getDashboardSummary() throws SQLException {
        DashboardSummary summary = new DashboardSummary();
        summary.setTotalLayouts(getTotalLayouts());
        summary.setTotalRulesets(getTotalRulesets());
        summary.setRecentRuns(getRecentSimulationRuns(5));
        return summary;
    }
}