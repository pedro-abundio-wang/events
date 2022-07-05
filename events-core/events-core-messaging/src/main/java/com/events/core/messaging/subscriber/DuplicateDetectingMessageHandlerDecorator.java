package com.events.core.messaging.subscriber;

public class DuplicateDetectingMessageHandlerDecorator implements MessageHandlerDecorator {

  private final DuplicateMessageDetector duplicateMessageDetector;

  public DuplicateDetectingMessageHandlerDecorator(
      DuplicateMessageDetector duplicateMessageDetector) {
    this.duplicateMessageDetector = duplicateMessageDetector;
  }

  @Override
  public void accept(
      SubscriberIdAndMessage subscriberIdAndMessage,
      MessageHandlerDecoratorChain messageHandlerDecoratorChain) {
    duplicateMessageDetector.doWithMessage(
        subscriberIdAndMessage,
        () -> messageHandlerDecoratorChain.invokeNext(subscriberIdAndMessage));
  }

  @Override
  public int getOrder() {
    return BuiltInMessageHandlerDecoratorOrder.DUPLICATE_DETECTING_MESSAGE_HANDLER_DECORATOR;
  }
}
