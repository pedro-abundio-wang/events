ALTER TABLE events.message DROP COLUMN payload;
ALTER TABLE events.message ADD COLUMN payload JSON;

ALTER TABLE events.message DROP COLUMN headers;
ALTER TABLE events.message ADD COLUMN headers JSON;
