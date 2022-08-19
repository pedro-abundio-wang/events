package com.events.messaging.kafka.consumer;

import com.events.messaging.kafka.consumer.basic.MessageConsumerBacklog;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface KafkaMessageConsumerHandler
    extends BiFunction<
        ConsumerRecord<String, byte[]>, BiConsumer<Void, Throwable>, MessageConsumerBacklog> {}
