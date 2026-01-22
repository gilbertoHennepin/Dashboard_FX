package org.example.hellofx.dataClasses; 

import java.time.LocalDateTime;

public class RecentSimulationRun {
    private int id;
    private String layoutName;
    private String rulesetName;
    private String outcome;
    private LocalDateTime timestamp;
    private String details;

    // Constructors
    public RecentSimulationRun() {}

    public RecentSimulationRun(int id, String layoutName, String rulesetName, 
                               String outcome, LocalDateTime timestamp, String details) {
        this.id = id;
        this.layoutName = layoutName;
        this.rulesetName = rulesetName;
        this.outcome = outcome;
        this.timestamp = timestamp;
        this.details = details;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    public String getRulesetName() {
        return rulesetName;
    }

    public void setRulesetName(String rulesetName) {
        this.rulesetName = rulesetName;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "RecentSimulationRun{" +
                "id=" + id +
                ", layoutName='" + layoutName + '\'' +
                ", rulesetName='" + rulesetName + '\'' +
                ", outcome='" + outcome + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}