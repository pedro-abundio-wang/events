CREATE SCHEMA events;

DROP TABLE IF EXISTS events.message CASCADE;
DROP TABLE IF EXISTS events.received_messages CASCADE;
DROP TABLE IF EXISTS events.offset_store CASCADE;
DROP TABLE IF EXISTS events.cdc_monitoring CASCADE;

CREATE TABLE events.message (
  id VARCHAR(1000) PRIMARY KEY,
  destination TEXT NOT NULL,
  headers TEXT NOT NULL,
  payload TEXT NOT NULL,
  published SMALLINT DEFAULT 0,
  creation_time BIGINT
);

CREATE INDEX message_published_idx ON events.message(published, id);

CREATE TABLE events.received_messages (
  consumer_id VARCHAR(1000),
  message_id VARCHAR(1000),
  creation_time BIGINT,
  published SMALLINT DEFAULT 0,
  PRIMARY KEY(consumer_id, message_id)
);

CREATE TABLE events.offset_store(
  client_name VARCHAR(255) NOT NULL PRIMARY KEY,
  serialized_offset VARCHAR(255)
);

CREATE TABLE events.cdc_monitoring (
  reader_id VARCHAR(1000) PRIMARY KEY,
  last_time BIGINT
);