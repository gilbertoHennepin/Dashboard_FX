package org.example.hellofx.dataClasses;

import java.time.LocalDateTime;

public class Ruleset {
    private int id;
    private String name;
    private String rulesJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Ruleset() {}

    public Ruleset(int id, String name, String rulesJson) {
        this.id = id;
        this.name = name;
        this.rulesJson = rulesJson;
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

    public String getRulesJson() {
        return rulesJson;
    }

    public void setRulesJson(String rulesJson) {
        this.rulesJson = rulesJson;
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