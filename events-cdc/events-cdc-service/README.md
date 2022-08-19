# multi db instance

```yaml
events:
  kafka:
    bootstrap:
      servers: ${KAFKA_IP}:${KAFKA_PORT}
  zookeeper:
    connection:
      string: ${ZK_IP}:${ZK_PORT}
  cdc:
    pipeline:
      postgres-pipeline:
        type: transactional-messaging
        reader: postgres-instance
        eventsDatabaseSchema: events
    reader:
      postgres-instance:
        type: postgres-wal
        dataSourceUrl: jdbc:postgresql://${DB_IP}:${DB_PORT}/${DB_INSTANCE}
        dataSourceUserName: ${DB_USER}
        dataSourcePassword: ${DB_PASS}
        dataSourceDriverClassName: org.postgresql.Driver
        leadershipLockPath: /events/cdc/leader/postgres-instance
        outboxId: 1
      mysql-instance:
        type: mysql-binlog
        dataSourceUrl: jdbc:mysql://${DB_IP}:${DB_PORT}/${DB_INSTANCE}
        dataSourceUserName: ${DB_USER}
        dataSourcePassword: ${DB_PASS}
        dataSourceDriverClassName: com.mysql.jdbc.Driver
        leadershipLockPath: /events/cdc/leader/mysql-instance
        outboxId: 2
      oracle-instance:
        type: polling
        dataSourceUrl: jdbc:oracle:thin://${DB_IP}:${DB_PORT}/${DB_INSTANCE}
        dataSourceUserName: ${DB_USER}
        dataSourcePassword: ${DB_PASS}
        dataSourceDriverClassName: oracle.jdbc.driver.OracleDriver
        leadershipLockPath: /events/cdc/leader/oracle-instance
        outboxId: 3
```

# multi db schema

```yaml
events:
  kafka:
    bootstrap:
      servers: ${KAFKA_IP}:${KAFKA_PORT}
  zookeeper:
    connection:
      string: ${ZK_IP}:${ZK_PORT}
  cdc:
    pipeline:
      events:
        type: transactional-messaging
        reader: postgres-instance
        eventsDatabaseSchema: events
    reader:
      postgres-instance:
        type: postgres-wal
        dataSourceUrl: jdbc:postgresql://${DB_IP}:${DB_PORT}/${DB_INSTANCE}
        dataSourceUserName: ${DB_USER}
        dataSourcePassword: ${DB_PASS}
        dataSourceDriverClassName: org.postgresql.Driver
        leadershipLockPath: /events/cdc/leader/postgres-instance
        outboxId: 1
```

