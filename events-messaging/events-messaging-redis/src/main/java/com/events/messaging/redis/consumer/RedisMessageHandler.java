package com.events.messaging.redis.consumer;

import java.util.function.Consumer;

public interface RedisMessageHandler extends Consumer<RedisMessage> {}
