package com.events.cdc.service.publisher;

import com.events.cdc.service.config.others.EventsCdcProperties;
import com.events.cdc.publisher.producer.wrappers.kafka.KafkaCdcProducer;
import com.events.messaging.kafka.properties.KafkaProperties;
import com.events.messaging.kafka.producer.KafkaMessageProducer;
import com.events.messaging.kafka.config.KafkaMessageProducerConfiguration;
import com.events.messaging.kafka.properties.KafkaMessageProducerProperties;
import com.events.messaging.kafka.config.KafkaConfiguration;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = KafkaCdcProducerTest.TestConfiguration.class)
@ActiveProfiles("${SPRING_PROFILES_ACTIVE:postgresql}")
public class KafkaCdcProducerTest {

  @Autowired private KafkaProperties kafkaProperties;

  @Autowired private KafkaMessageProducerProperties kafkaMessageProducerProperties;

  @Autowired private EventsCdcProperties eventsCdcProperties;

  @Test
  public void shouldProduceMessage() {
    KafkaCdcProducer kafkaCdcProducer =
        new KafkaCdcProducer(
            new KafkaMessageProducer(
                kafkaProperties.getBootstrapServers(), kafkaMessageProducerProperties),
            eventsCdcProperties.isEnableBatchProcessing(),
            eventsCdcProperties.getMaxBatchSize(),
            new LoggingMeterRegistry());

    String topic = "test";
    String key = "eventsKafkaCdcProducerWrapper";
    String message = "eventsKafkaCdcProducerWrapper";
    kafkaCdcProducer.send(topic, key, message);
  }

  @Configuration
  @EnableAutoConfiguration
  @Import({KafkaConfiguration.class, KafkaMessageProducerConfiguration.class})
  public static class TestConfiguration {
    @Bean
    public EventsCdcProperties eventsCdcConfigurationProperties() {
      return new EventsCdcProperties();
    }
  }
}
