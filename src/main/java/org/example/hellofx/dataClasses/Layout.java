package org.example.hellofx.dataClasses;

import java.time.LocalDateTime;

public class Layout {
    private int id;
    private String name;
    private String gridData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Layout() {}

    public Layout(int id, String name, String gridData) {
        this.id = id;
        this.name = name;
        this.gridData = gridData;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGridData() {
        return gridData;
    }

    public void setGridData(String gridData) {
        this.gridData = gridData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return name; // For display in ComboBox
    }
}