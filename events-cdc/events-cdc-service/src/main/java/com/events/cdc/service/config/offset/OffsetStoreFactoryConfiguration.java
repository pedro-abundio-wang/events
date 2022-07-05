package com.events.cdc.service.config.offset;

import org.springframework.context.annotation.Configuration;

@Configuration
// @EnableConfigurationProperties(EventsChannelProperties.class)
public class OffsetStoreFactoryConfiguration {

  //  @Bean
  //  @Conditional(ActiveMQOrRabbitMQOrRedisCondition.class)
  //  public DebeziumOffsetStoreFactory emptyDebeziumOffsetStoreFactory() {
  //
  //    return () ->
  //        new DebeziumBinlogOffsetKafkaStore(null, null) {
  //          @Override
  //          public Optional<TransactionLogFileOffset> getLastTransactionlogFileOffset() {
  //            return Optional.empty();
  //          }
  //        };
  //  }

  //  @Bean
  //  @Conditional(ActiveMQOrRabbitMQOrRedisCondition.class)
  //  public OffsetStoreFactory postgresWalJdbcOffsetStoreFactory() {
  //
  //    return (properties, dataSource, eventsSchema, clientName) ->
  //        new JdbcTransactionLogFileOffsetStore(clientName, new JdbcTemplate(dataSource),
  // eventsSchema);
  //  }
}
