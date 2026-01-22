package org.example.hellofx.dataClasses;

import java.util.List;

public class DashboardSummary {
    private int totalLayouts;
    private int totalRulesets;
    private List<RecentSimulationRun> recentRuns;

    // Constructors
    public DashboardSummary() {}

    public DashboardSummary(int totalLayouts, int totalRulesets, List<RecentSimulationRun> recentRuns) {
        this.totalLayouts = totalLayouts;
        this.totalRulesets = totalRulesets;
        this.recentRuns = recentRuns;
    }

    // Getters and Setters
    public int getTotalLayouts() {
        return totalLayouts;
    }

    public void setTotalLayouts(int totalLayouts) {
        this.totalLayouts = totalLayouts;
    }

    public int getTotalRulesets() {
        return totalRulesets;
    }

    public void setTotalRulesets(int totalRulesets) {
        this.totalRulesets = totalRulesets;
    }

    public List<RecentSimulationRun> getRecentRuns() {
        return recentRuns;
    }

    public void setRecentRuns(List<RecentSimulationRun> recentRuns) {
        this.recentRuns = recentRuns;
    }

    @Override
    public String toString() {
        return "DashboardSummary{" +
                "totalLayouts=" + totalLayouts +
                ", totalRulesets=" + totalRulesets +
                ", recentRunsCount=" + (recentRuns != null ? recentRuns.size() : 0) +
                '}';
    }
}