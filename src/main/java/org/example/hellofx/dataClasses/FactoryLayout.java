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

        // Find robot start
        int startX = -1, startY = -1;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (gridState[i][j] == 2) {
                    startX = i;
                    startY = j;
                    break;
                }
            }
        }
        json.append("{\"x\":").append(startX).append(",\"y\":").append(startY).append("},");

        // Find exit
        json.append("\"exit\":");
        int exitX = -1, exitY = -1;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (gridState[i][j] == 3) {
                    exitX = i;
                    exitY = j;
                    break;
                }
            }
        }
        json.append("{\"x\":").append(exitX).append(",\"y\":").append(exitY).append("},");

        // Find obstacles
        json.append("\"obstacles\":[");
        boolean first = true;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (gridState[i][j] == 1) {
                    if (!first) json.append(",");
                    json.append("{\"x\":").append(i).append(",\"y\":").append(j).append("}");
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