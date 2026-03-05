ALTER TABLE placement_attempts
    ADD COLUMN answers_json JSON NULL AFTER paper_json;
