package org.example.hellofx.dataClasses;

public class FactoryLayout {
    private int id;
    private String name;
    private int[][] gridState; // 10x10 grid: 0=empty, 1=obstacle, 2=start, 3=exit

    public FactoryLayout() {
        this.gridState = new int[10][10];
    }

    public FactoryLayout(int id, String name, int[][] gridState) {
        this.id = id;
        this.name = name;
        this.gridState = gridState;
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

    public int[][] getGridState() {
        return gridState;
    }

    public void setGridState(int[][] gridState) {
        this.gridState = gridState;
    }

    // Convert grid to JSON string for database storage
    public String gridToJson() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"size\":\"10x10\",");
        json.append("\"robotStart\":");
        
        // Find robot start (row = y, col = x)
        int startRow = -1, startCol = -1;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (gridState[row][col] == 2) {
                    startRow = row;
                    startCol = col;
                    break;
                }
            }
        }
        json.append("{\"x\":").append(startCol).append(",\"y\":").append(startRow).append("},");
        
        // Find exit (row = y, col = x)
        json.append("\"exit\":");
        int exitRow = -1, exitCol = -1;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (gridState[row][col] == 3) {
                    exitRow = row;
                    exitCol = col;
                    break;
                }
            }
        }
        json.append("{\"x\":").append(exitCol).append(",\"y\":").append(exitRow).append("},");
        
        // Find obstacles (row = y, col = x)
        json.append("\"obstacles\":[");
        boolean first = true;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (gridState[row][col] == 1) {
                    if (!first) json.append(",");
                    json.append("{\"x\":").append(col).append(",\"y\":").append(row).append("}");
                    first = false;
                }
            }
        }
        json.append("]}");
        
        return json.toString();
    }

    @Override
    public String toString() {
        return name;
    }
}