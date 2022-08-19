package io.eventuate.tram.sagas.common;

import com.events.core.messaging.message.Message;

import java.util.Optional;

public interface SagaLockManager {

  boolean claimLock(String sagaType, String sagaId, String target);

  void stashMessage(String sagaType, String sagaId, String target, Message message);

  Optional<Message> unlock(String sagaId, String target);
}
