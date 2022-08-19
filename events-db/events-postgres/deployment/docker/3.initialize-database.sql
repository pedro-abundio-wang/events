SELECT * FROM pg_create_logical_replication_slot('events_slot', 'wal2json');
-- SELECT * FROM pg_replication_slots;
-- SELECT * FROM pg_drop_replication_slot('events_slot');
