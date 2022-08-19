package io.eventuate.tram.sagas.orchestration;

import com.events.core.messaging.message.Message;

public interface SagaDefinition<Data> {

  SagaActions<Data> start(Data sagaData);

  SagaActions<Data> handleReply(String currentState, Data sagaData, Message message);
}
