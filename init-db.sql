-- Create tables
CREATE TABLE IF NOT EXISTS layouts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    grid_data TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rulesets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    rules_json TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS simulation_runs (
    id SERIAL PRIMARY KEY,
    layout_id INTEGER REFERENCES layouts(id) ON DELETE CASCADE,
    ruleset_id INTEGER REFERENCES rulesets(id) ON DELETE CASCADE,
    outcome VARCHAR(50) NOT NULL CHECK (outcome IN ('Success', 'Fail')),
    run_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT
);

-- Insert sample layouts
INSERT INTO layouts (name, grid_data) VALUES
('Simple 5x5 Grid', '{"size": "5x5", "obstacles": []}'),
('Warehouse Layout', '{"size": "10x10", "obstacles": [[2,3], [4,5]]}'),
('Complex Maze', '{"size": "8x8", "obstacles": [[1,1], [2,2], [3,3]]}');

-- Insert sample rulesets
INSERT INTO rulesets (name, rules_json) VALUES
('Basic Movement', '{"rules": ["FORWARD", "LEFT", "RIGHT"]}'),
('Advanced Navigation', '{"rules": ["FORWARD", "LEFT", "RIGHT", "BACKWARD"]}'),
('Smart Pathfinding', '{"rules": ["AUTO_NAVIGATE", "AVOID_OBSTACLES"]}');

-- Insert sample simulation runs (recent activity)
INSERT INTO simulation_runs (layout_id, ruleset_id, outcome, run_timestamp, details) VALUES
(1, 1, 'Success', NOW() - INTERVAL '1 hour', 'Robot completed course successfully'),
(2, 2, 'Fail', NOW() - INTERVAL '2 hours', 'Robot hit obstacle at position (4,5)'),
(1, 3, 'Success', NOW() - INTERVAL '3 hours', 'Smart pathfinding completed'),
(3, 2, 'Success', NOW() - INTERVAL '5 hours', 'Navigated complex maze'),
(2, 1, 'Fail', NOW() - INTERVAL '6 hours', 'Robot went out of bounds'),
(1, 2, 'Success', NOW() - INTERVAL '1 day', 'Basic movement test passed'),
(3, 3, 'Success', NOW() - INTERVAL '2 days', 'Advanced maze solving');

-- Create indexes for better query performance
CREATE INDEX idx_simulation_runs_timestamp ON simulation_runs(run_timestamp DESC);
CREATE INDEX idx_layouts_name ON layouts(name);
CREATE INDEX idx_rulesets_name ON rulesets(name);

INSERT INTO layouts (name, grid_data) 
VALUES ('Test Layout with Obstacles', '{"size": "10x10", "obstacles": [[3,3], [5,5], [7,7]]}');