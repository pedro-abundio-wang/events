-- transactional message

CREATE SEQUENCE events.message_table_id_sequence START 1;

select setval('events.message_table_id_sequence', (ROUND(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000))::BIGINT);

CREATE TABLE events.upgrade_message (
  db_id BIGINT NOT NULL DEFAULT nextval('events.message_table_id_sequence') PRIMARY KEY,
  id VARCHAR(1000),
  destination TEXT NOT NULL,
  headers TEXT NOT NULL,
  payload TEXT NOT NULL,
  published SMALLINT DEFAULT 0,
  creation_time BIGINT
);

ALTER SEQUENCE events.message_table_id_sequence OWNED BY events.upgrade_message.db_id;

INSERT INTO events.upgrade_message (id, destination, headers, payload, published, creation_time)
    SELECT id, destination, headers, payload, published, creation_time FROM events.message ORDER BY id;

DROP TABLE events.message;

ALTER TABLE events.upgrade_message RENAME TO message;

CREATE INDEX message_published_idx ON events.message(published, db_id);

-- event source

CREATE SEQUENCE events.events_table_id_sequence START 1;

select setval('events.events_table_id_sequence', (ROUND(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000))::BIGINT);

CREATE TABLE events.upgrade_events (
  db_id BIGINT NOT NULL DEFAULT nextval('events.events_table_id_sequence') PRIMARY KEY,
  event_id VARCHAR(1000),
  event_type VARCHAR(1000),
  event_data VARCHAR(1000) NOT NULL,
  entity_type VARCHAR(1000) NOT NULL,
  entity_id VARCHAR(1000) NOT NULL,
  triggering_event VARCHAR(1000),
  metadata VARCHAR(1000),
  published SMALLINT DEFAULT 0
);

ALTER SEQUENCE events.events_table_id_sequence OWNED BY events.upgrade_events.db_id;

INSERT INTO events.upgrade_events (event_id, event_type, event_data, entity_type, entity_id, triggering_event, metadata, published)
    SELECT event_id, event_type, event_data, entity_type, entity_id, triggering_event, metadata, published FROM events.events ORDER BY event_id;

DROP TABLE events.events;

ALTER TABLE events.upgrade_events RENAME TO events;

CREATE INDEX events_idx ON events.events(entity_type, entity_id, db_id);
CREATE INDEX events_published_idx ON events.events(published, db_id);
