# MessageProducer Hows-to

```java
public interface MessageProducer {
    CompletableFuture<?> send(String channel, String key, String message);
}
```

|          | Channel        | Key (Consumer Groups) |
| -------- | -------------- | --------------------- |
| ActiveMQ | Topic/Queue    | JMSXGroupID           |
| RabbitMQ | Fanout         | AMQP.BasicProperties  |
| Kafka    | TopicPartition | TopicPartition        |
| Redis    | StreamKey      | StreamRecords         |

ActiveMQ Message Group: https://activemq.apache.org/message-groups

RabbitMQ:

Kafka:

Redis Stream Consumer Groups: https://redis.io/docs/manual/data-types/streams/

# MessageConsumer Hows-to



# ActiveMQ Hows-to

# RabbitMQ Hows-to 

## Creating Consumer Groups in RabbitMQ with Rebalanser

# Kafka Hows-to

# Redis Hows-to

