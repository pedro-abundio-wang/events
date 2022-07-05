package com.events.cdc.service.config.others;

import com.events.cdc.connector.db.transaction.log.messaging.TransactionLogFileOffset;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.json.mapper.JsonMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

public class JdbcTransactionLogFileOffsetStore implements TransactionLogFileOffsetStore {

  private JdbcTemplate jdbcTemplate;
  private EventsSchema eventsSchema;
  private String clientName;
  private String tableName;

  public JdbcTransactionLogFileOffsetStore(
      String clientName, JdbcTemplate jdbcTemplate, EventsSchema eventsSchema) {
    this.clientName = clientName;
    this.jdbcTemplate = jdbcTemplate;
    this.eventsSchema = eventsSchema;

    init();
  }

  private void init() {
    tableName = eventsSchema.qualifyTable("offset_store");

    String selectAllByClientNameQuery =
        String.format("select * from %s where client_name = ?", tableName);

    if (jdbcTemplate.queryForList(selectAllByClientNameQuery, clientName).isEmpty()) {

      String insertNullOffsetForClientNameQuery =
          String.format(
              "insert into %s (client_name, serialized_offset) VALUES (?, NULL)", tableName);

      jdbcTemplate.update(insertNullOffsetForClientNameQuery, clientName);
    }
  }

  @Override
  public Optional<TransactionLogFileOffset> getLastTransactionlogFileOffset() {
    String selectOffsetByClientNameQuery =
        String.format("select serialized_offset from %s where client_name = ?", tableName);

    String offset =
        jdbcTemplate.queryForObject(selectOffsetByClientNameQuery, String.class, clientName);
    return Optional.ofNullable(offset)
        .map(o -> JsonMapper.fromJson(o, TransactionLogFileOffset.class));
  }

  @Override
  public void save(TransactionLogFileOffset transactionLogFileOffset) {
    String updateOffsetByClientNameQuery =
        String.format("update %s set serialized_offset = ?", tableName);
    jdbcTemplate.update(updateOffsetByClientNameQuery, JsonMapper.toJson(transactionLogFileOffset));
  }
}
