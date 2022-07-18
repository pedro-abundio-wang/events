package com.events.cdc.service.reader;

import com.events.cdc.connector.db.transaction.log.messaging.MessageWithDestination;
import com.events.cdc.reader.CdcReader;
import com.events.cdc.service.helper.TestHelper;
import com.events.cdc.service.reader.assertion.MessageWithDestinationAssertOperationBuilder;
import com.events.cdc.service.reader.assertion.TransactionLogMessageAssert;
import com.events.common.id.IdGenerator;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class AbstractCdcReaderTest {

  @Autowired protected IdGenerator idGenerator;

  @Autowired protected TestHelper testHelper;

  @Autowired protected CdcReader cdcReader;

  @Test
  public void testMessageHandled() {

    String payload = testHelper.generateRandomPayload();
    String destination = testHelper.generateRandomDestination();
    Map<String, String> headers = new HashMap<>();

    String messageId = testHelper.saveMessage(idGenerator, payload, destination, headers);

    TransactionLogMessageAssert<MessageWithDestination> transactionLogMessageAssert =
        testHelper.prepareTransactionLogMessageAssertion(cdcReader);

    testHelper.runInSeparateThread(cdcReader::start);

    transactionLogMessageAssert.assertMessaageReceived(
        MessageWithDestinationAssertOperationBuilder.assertion()
            .withId(messageId)
            .withDestination(destination)
            .withPayload(payload)
            .withHeaders(headers)
            .build());

    cdcReader.stop();
  }
}
