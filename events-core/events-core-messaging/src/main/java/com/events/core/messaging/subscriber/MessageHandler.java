package com.events.core.messaging.subscriber;

import com.events.core.messaging.message.Message;

import java.util.function.Consumer;

public interface MessageHandler extends Consumer<Message> {
}
