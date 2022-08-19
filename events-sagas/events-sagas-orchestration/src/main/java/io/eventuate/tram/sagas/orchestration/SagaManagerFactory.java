package io.eventuate.tram.sagas.orchestration;

import com.events.core.commands.publisher.CommandPublisher;
import com.events.core.messaging.subscriber.MessageSubscriber;
import io.eventuate.tram.sagas.common.SagaLockManager;

public class SagaManagerFactory {

  private final SagaInstanceRepository sagaInstanceRepository;
  private final CommandPublisher commandPublisher;
  private final MessageSubscriber messageSubscriber;
  private final SagaLockManager sagaLockManager;
  private final SagaCommandProducer sagaCommandProducer;

  public SagaManagerFactory(
      SagaInstanceRepository sagaInstanceRepository,
      CommandPublisher commandPublisher,
      MessageSubscriber messageSubscriber,
      SagaLockManager sagaLockManager,
      SagaCommandProducer sagaCommandProducer) {
    this.sagaInstanceRepository = sagaInstanceRepository;
    this.commandPublisher = commandPublisher;
    this.messageSubscriber = messageSubscriber;
    this.sagaLockManager = sagaLockManager;
    this.sagaCommandProducer = sagaCommandProducer;
  }

  public <SagaData> SagaManagerImpl<SagaData> make(Saga<SagaData> saga) {
    return new SagaManagerImpl<>(
        saga,
        sagaInstanceRepository,
        commandPublisher,
        messageSubscriber,
        sagaLockManager,
        sagaCommandProducer);
  }
}
