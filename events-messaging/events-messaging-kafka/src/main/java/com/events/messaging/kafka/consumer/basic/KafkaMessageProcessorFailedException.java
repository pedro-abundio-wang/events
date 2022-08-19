package com.events.messaging.kafka.consumer.basic;

public class KafkaMessageProcessorFailedException extends RuntimeException {
    public KafkaMessageProcessorFailedException(Throwable t) {
        super(t);
    }
}
