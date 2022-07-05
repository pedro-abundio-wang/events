package com.events.cdc.service.cleaners;

import com.events.cdc.service.properties.MessagePurgeProperties;
import com.events.common.jdbc.schema.EventsSchema;
import com.events.common.jdbc.sql.dialect.EventsSqlDialect;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Timer;
import java.util.TimerTask;

public class MessageCleaner {

  private EventsSqlDialect eventsSqlDialect;
  private EventsSchema eventsSchema;
  private MessagePurgeProperties messagePurgeProperties;

  private Timer timer;
  private JdbcTemplate jdbcTemplate;

  public MessageCleaner(
      EventsSqlDialect eventsSqlDialect,
      DataSource dataSource,
      EventsSchema eventsSchema,
      MessagePurgeProperties messagePurgeProperties) {
    this.eventsSqlDialect = eventsSqlDialect;
    this.eventsSchema = eventsSchema;
    this.messagePurgeProperties = messagePurgeProperties;

    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public void start() {
    if (messagePurgeProperties.isMessagesEnabled()
        || messagePurgeProperties.isReceivedMessagesEnabled()) {
      timer = new Timer();

      timer.scheduleAtFixedRate(
          new TimerTask() {
            @Override
            public void run() {
              cleanTables();
            }
          },
          0,
          messagePurgeProperties.getIntervalInSeconds() * 1000);
    }
  }

  public void stop() {
    if (timer != null) {
      timer.cancel();
    }
  }

  private void cleanTables() {
    if (messagePurgeProperties.isMessagesEnabled()) {
      cleanMessages();
    }

    if (messagePurgeProperties.isReceivedMessagesEnabled()) {
      cleanReceivedMessages();
    }
  }

  private void cleanMessages() {
    String table = eventsSchema.qualifyTable("message");

    String sql =
        String.format(
            "delete from %s where %s - creation_time > ?",
            table, eventsSqlDialect.getCurrentTimeInMillisecondsExpression());

    jdbcTemplate.update(sql, messagePurgeProperties.getMessagesMaxAgeInSeconds() * 1000);
  }

  private void cleanReceivedMessages() {
    String table = eventsSchema.qualifyTable("received_messages");

    String sql =
        String.format(
            "delete from %s where %s - creation_time > ?",
            table, eventsSqlDialect.getCurrentTimeInMillisecondsExpression());

    jdbcTemplate.update(sql, messagePurgeProperties.getReceivedMessagesMaxAgeInSeconds() * 1000);
  }
}
