package com.events.core.messaging.subscriber;

import com.events.common.jdbc.exception.EventsDuplicateKeyException;
import com.events.common.jdbc.executor.EventsJdbcStatementExecutor;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.transaction.EventsTransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlTableBasedDuplicateMessageDetector implements DuplicateMessageDetector {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final EventsSchema eventsSchema;
  private final String currentTimeInMillisecondsSql;
  private final EventsJdbcStatementExecutor eventsJdbcStatementExecutor;
  private final EventsTransactionTemplate eventsTransactionTemplate;

  public SqlTableBasedDuplicateMessageDetector(
      EventsSchema eventsSchema,
      String currentTimeInMillisecondsSql,
      EventsJdbcStatementExecutor eventsJdbcStatementExecutor,
      EventsTransactionTemplate eventsTransactionTemplate) {
    this.eventsSchema = eventsSchema;
    this.currentTimeInMillisecondsSql = currentTimeInMillisecondsSql;
    this.eventsJdbcStatementExecutor = eventsJdbcStatementExecutor;
    this.eventsTransactionTemplate = eventsTransactionTemplate;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    try {
      String table = eventsSchema.qualifyTable("received_messages");

      eventsJdbcStatementExecutor.update(
          String.format(
              "insert into %s(consumer_id, message_id, creation_time) values(?, ?, %s)",
              table, currentTimeInMillisecondsSql),
          consumerId,
          messageId);

      return false;
    } catch (EventsDuplicateKeyException e) {
      logger.info("Message duplicate: consumerId = {}, messageId = {}", consumerId, messageId);
      return true;
    }
  }

  @Override
  public void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback) {
    eventsTransactionTemplate.executeInTransaction(
        () -> {
          if (!isDuplicate(
              subscriberIdAndMessage.getSubscriberId(),
              subscriberIdAndMessage.getMessage().getId())) callback.run();
          return null;
        });
  }
}
