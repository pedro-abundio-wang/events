package io.eventuate.tram.sagas.orchestration;

import com.events.common.id.Int128;
import com.events.core.commands.common.CommandReplyMessageHeaders;
import com.events.core.commands.common.outcome.CommandReplyOutcome;
import com.events.core.commands.publisher.CommandPublisher;
import com.events.core.commands.subscriber.CommandWithDestination;
import com.events.core.messaging.channel.MessageChannelMapping;
import com.events.core.messaging.message.Message;
import com.events.core.messaging.message.MessageBuilder;
import com.events.core.messaging.subscriber.MessageHandler;
import com.events.core.messaging.subscriber.MessageSubscriber;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.common.SagaReplyHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SagaManagerImplTest {

  @Mock private SagaManagerImpl<TestSagaData> sm;

  @Mock private SagaInstanceRepository sagaInstanceRepository;

  @Mock private CommandPublisher commandPublisher;

  @Mock private MessageSubscriber messageSubscriber;

  @Mock private MessageChannelMapping channelMapping;

  @Mock private SagaLockManager sagaLockManager;

  @Mock private SagaCommandProducer sagaCommandProducer;

  private TestSagaData initialSagaData;
  private TestSagaData sagaDataUpdatedByStartingHandler;
  private TestSagaData sagaDataUpdatedByReplyHandler;

  @Mock private TestSaga testSaga;

  @Mock private SagaDefinition<TestSagaData> sagaDefinition;

  private String testResource = "SomeResource";
  private String sagaType = "MySagaType";
  private String sagaId = "MySagaId";
  private String sagaReplyChannel = sagaType + "-reply";

  private String participantChannel1 = "myChannel";
  private String participantChannel2 = "myChannel2";

  private TestCommand command1 = new TestCommand();
  private TestCommand command2 = new TestCommand();

  private CommandWithDestination commandForParticipant1 =
      new CommandWithDestination(
          participantChannel1, testResource, SagaManagerImplTest.this.command1);

  private CommandWithDestination commandForParticipant2 =
      new CommandWithDestination(
          participantChannel2, testResource, SagaManagerImplTest.this.command2);

  private SagaInstance sagaInstance;

  private Int128 requestId1 = new Int128(0, 1);
  private Int128 requestId2 = new Int128(0, 2);

  private MessageHandler sagaMessageHandler;

  private Message replyFromParticipant1 =
      MessageBuilder.createInstance()
          .withPayload("{}")
          .withHeader(SagaReplyHeaders.REPLY_SAGA_TYPE, sagaType)
          .withHeader(SagaReplyHeaders.REPLY_SAGA_ID, sagaId)
          .withHeader(CommandReplyMessageHeaders.REPLY_OUTCOME, CommandReplyOutcome.SUCCESS.name())
          .build();

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Before
  public void setUp() {

    sm =
        new SagaManagerImpl<>(
            testSaga,
            sagaInstanceRepository,
            commandPublisher,
            messageSubscriber,
            sagaLockManager,
            sagaCommandProducer);

    initialSagaData = new TestSagaData("initialSagaData");
    sagaDataUpdatedByStartingHandler = new TestSagaData("sagaDataUpdatedByStartingHandler");
    sagaDataUpdatedByReplyHandler =
        new TestSagaData("sagaDataUpdatedByStartingHandlersagaDataUpdatedByReplyHandler");

    when(testSaga.getSagaType()).thenReturn(sagaType);
    when(testSaga.getSagaDefinition()).thenReturn(sagaDefinition);

    when(channelMapping.transform(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  public void shouldExecuteSagaSuccessfully() {

    initializeSagaManager();

    startSaga();

    reset(sagaInstanceRepository, sagaCommandProducer);

    handleReply(false);

    verify(testSaga).onSagaCompletedSuccessfully(eq(sagaId), any(TestSagaData.class));

    reset(sagaInstanceRepository, sagaCommandProducer);
  }

  @Test
  public void shouldExecuteSagaRolledBack() {

    initializeSagaManager();

    startSaga();

    reset(sagaInstanceRepository, sagaCommandProducer);

    handleReply(true);

    verify(testSaga).onSagaRolledBack(eq(sagaId), any(TestSagaData.class));

    reset(sagaInstanceRepository, sagaCommandProducer);
  }

  private void startSaga() {
    when(sagaDefinition.start(initialSagaData)).thenReturn(makeFirstSagaActions());

    when(sagaCommandProducer.sendCommands(anyString(), anyString(), anyList(), anyString()))
        .thenReturn(requestId1.asString());

    doAnswer(this::assignSagaIdWhenSaved)
        .when(sagaInstanceRepository)
        .save(any(SagaInstance.class));

    sagaInstance = sm.create(initialSagaData);

    SagaInstance expectedSagaInstanceAfterFirstStep = makeExpectedSagaInstanceAfterFirstStep();

    assertSagaInstanceEquals(expectedSagaInstanceAfterFirstStep, sagaInstance);

    verify(sagaCommandProducer)
        .sendCommands(
            sagaType, sagaId, Collections.singletonList(commandForParticipant1), sagaReplyChannel);

    verify(sagaInstanceRepository).save(any(SagaInstance.class));

    verify(sagaInstanceRepository)
        .update(
            argThat(
                sagaInstance ->
                    sagaInstanceEquals(expectedSagaInstanceAfterFirstStep, sagaInstance)));

    assertSagaInstanceEquals(expectedSagaInstanceAfterFirstStep, sagaInstance);

    verifyNoMoreInteractions(sagaInstanceRepository, sagaCommandProducer);
  }

  private SagaInstance makeExpectedSagaInstanceAfterFirstStep() {
    return new SagaInstance(
        sagaType,
        sagaId,
        "state-A",
        requestId1.asString(),
        SagaDataSerde.serializeSagaData(sagaDataUpdatedByStartingHandler),
        Collections.emptySet());
  }

  private void handleReply(boolean compensating) {
    SagaInstance expectedSagaInstanceAfterSecondStep = makeExpectedSagaInstanceAfterSecondStep();

    when(sagaInstanceRepository.find(sagaType, sagaId)).thenReturn(sagaInstance);

    when(sagaDefinition.handleReply(anyString(), any(TestSagaData.class), any(Message.class)))
        .thenReturn(makeSecondSagaActions(compensating));

    when(sagaCommandProducer.sendCommands(anyString(), anyString(), anyList(), anyString()))
        .thenReturn(requestId2.asString());

    sagaMessageHandler.accept(replyFromParticipant1);

    verify(sagaInstanceRepository).find(sagaType, sagaId);

    verify(sagaCommandProducer)
        .sendCommands(
            sagaType, sagaId, Collections.singletonList(commandForParticipant2), sagaReplyChannel);

    verify(sagaInstanceRepository)
        .update(
            argThat(
                sagaInstance ->
                    sagaInstanceEquals(expectedSagaInstanceAfterSecondStep, sagaInstance)));

    assertSagaInstanceEquals(expectedSagaInstanceAfterSecondStep, sagaInstance);

    verifyNoMoreInteractions(sagaInstanceRepository, sagaCommandProducer);
  }

  private SagaInstance makeExpectedSagaInstanceAfterSecondStep() {
    return new SagaInstance(
        sagaType,
        sagaId,
        "state-B",
        requestId2.asString(),
        SagaDataSerde.serializeSagaData(sagaDataUpdatedByReplyHandler),
        Collections.emptySet());
  }

  private boolean sagaInstanceEquals(
      SagaInstance expectedSagaInstanceAfterFirstStep, SagaInstance sagaInstance) {
    assertSagaInstanceEquals(expectedSagaInstanceAfterFirstStep, sagaInstance);
    return true;
  }

  private void assertSagaInstanceEquals(
      SagaInstance expectedSagaInstance, SagaInstance sagaInstance) {
    assertEquals(expectedSagaInstance.getSagaType(), sagaInstance.getSagaType());
    assertEquals(expectedSagaInstance.getId(), sagaInstance.getId());
    assertEquals(expectedSagaInstance.getStateName(), sagaInstance.getStateName());
    assertEquals(expectedSagaInstance.getLastRequestId(), sagaInstance.getLastRequestId());
    assertEquals(
        expectedSagaInstance.getSerializedSagaData().getSagaDataType(),
        sagaInstance.getSerializedSagaData().getSagaDataType());
    assertEquals(
        expectedSagaInstance.getSerializedSagaData().getSagaDataJSON(),
        sagaInstance.getSerializedSagaData().getSagaDataJSON());
  }

  private SagaActions<TestSagaData> makeFirstSagaActions() {
    return SagaActions.<TestSagaData>builder()
        .withUpdatedSagaData(sagaDataUpdatedByStartingHandler)
        .withCommand(commandForParticipant1)
        .withUpdatedState("state-A")
        .build();
  }

  private SagaActions<TestSagaData> makeSecondSagaActions(boolean compensating) {
    return SagaActions.<TestSagaData>builder()
        .withCommand(commandForParticipant2)
        .withUpdatedState("state-B")
        .withUpdatedSagaData(sagaDataUpdatedByReplyHandler)
        .withIsEndState(true)
        .withIsCompensating(compensating)
        .build();
  }

  private Object assignSagaIdWhenSaved(InvocationOnMock invocation) {
    SagaInstance sagaInstance = invocation.getArgument(0);
    sagaInstance.setId(sagaId);
    return null;
  }

  private void initializeSagaManager() {

    sm.subscribeToReplyChannel();

    ArgumentCaptor<MessageHandler> messageHandlerArgumentCaptor =
        ArgumentCaptor.forClass(MessageHandler.class);
    verify(messageSubscriber)
        .subscribe(
            anyString(),
            Mockito.eq(Collections.singleton(sagaReplyChannel)),
            messageHandlerArgumentCaptor.capture());

    sagaMessageHandler = messageHandlerArgumentCaptor.getValue();
  }
}
