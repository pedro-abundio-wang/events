package com.events.cdc.service.main;

import com.events.cdc.service.config.EventsCdcServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableAutoConfiguration(
    exclude = {
      DataSourceAutoConfiguration.class,
      DataSourceTransactionManagerAutoConfiguration.class,
      RedisAutoConfiguration.class,
      RabbitAutoConfiguration.class,
      ActiveMQAutoConfiguration.class,
      KafkaAutoConfiguration.class
    })
@Import(EventsCdcServiceConfiguration.class)
public class EventsCdcServiceMain {

  public static void main(String[] args) {
    SpringApplication.run(EventsCdcServiceMain.class, args);
  }
}
