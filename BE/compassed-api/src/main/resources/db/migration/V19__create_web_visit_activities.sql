CREATE TABLE IF NOT EXISTS web_visit_activities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    visitor_id VARCHAR(120) NOT NULL,
    visit_date DATE NOT NULL,
    page_path VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_web_visit_activities_visitor_date (visitor_id, visit_date),
    KEY idx_web_visit_activities_visit_date (visit_date)
);
