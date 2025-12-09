package org.example.hellofx.dba;

import org.example.hellofx.dataClasses.FactoryLayout;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LayoutDAO {
    private Connection connection;

    public LayoutDAO(Connection connection) {
        this.connection = connection;
    }

    // Save a new layout to database
    public void saveLayout(FactoryLayout layout) throws SQLException {
        String sql = "INSERT INTO layouts (name, grid_data) VALUES (?, ?) " +
                     "ON CONFLICT (name) DO UPDATE SET grid_data = EXCLUDED.grid_data, updated_at = CURRENT_TIMESTAMP";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, layout.getName());
            pstmt.setString(2, layout.gridToJson());
            pstmt.executeUpdate();
        }
    }

    // Get all layouts
    public List<FactoryLayout> getAllLayouts() throws SQLException {
        String sql = "SELECT id, name, grid_data FROM layouts ORDER BY created_at DESC";
        List<FactoryLayout> layouts = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                FactoryLayout layout = new FactoryLayout();
                layout.setId(rs.getInt("id"));
                layout.setName(rs.getString("name"));
                // Parse grid_data JSON if needed for loading layouts
                layouts.add(layout);
            }
        }

        return layouts;
    }

    // Get layout by ID
    public FactoryLayout getLayoutById(int id) throws SQLException {
        String sql = "SELECT id, name, grid_data FROM layouts WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    FactoryLayout layout = new FactoryLayout();
                    layout.setId(rs.getInt("id"));
                    layout.setName(rs.getString("name"));
                    // Parse grid_data JSON if needed
                    return layout;
                }
            }
        }

        return null;
    }

    // Delete layout
    public void deleteLayout(int id) throws SQLException {
        String sql = "DELETE FROM layouts WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}