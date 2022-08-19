package io.eventuate.tram.sagas.orchestration;

import com.events.core.commands.publisher.CommandPublisher;
import com.events.core.commands.subscriber.CommandWithDestination;
import io.eventuate.tram.sagas.common.SagaCommandHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SagaCommandProducer {

  private final CommandPublisher commandPublisher;

  public SagaCommandProducer(CommandPublisher commandPublisher) {
    this.commandPublisher = commandPublisher;
  }

  public String sendCommands(
      String sagaType,
      String sagaId,
      List<CommandWithDestination> commands,
      String sagaReplyChannel) {
    String messageId = null;
    for (CommandWithDestination command : commands) {
      Map<String, String> headers = new HashMap<>(command.getExtraHeaders());
      headers.put(SagaCommandHeaders.SAGA_TYPE, sagaType);
      headers.put(SagaCommandHeaders.SAGA_ID, sagaId);
      messageId =
          commandPublisher.publish(
              command.getDestinationChannel(),
              command.getResource(),
              command.getCommand(),
              sagaReplyChannel,
              headers);
    }
    return messageId;
  }
}
