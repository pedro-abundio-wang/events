package com.events.cdc.service.rest;

import com.events.cdc.reader.provider.CdcReaderProvider;
import com.events.cdc.reader.status.CdcReaderProcessingStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/readers")
public class CdcReaderController {

  private CdcReaderProvider cdcReaderProvider;

  public CdcReaderController(CdcReaderProvider cdcReaderProvider) {
    this.cdcReaderProvider = cdcReaderProvider;
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity getCdcReaderNames() {
    List<String> cdcReaderNames =
        cdcReaderProvider.getAll().stream()
            .map(cdcReaderLeadership -> cdcReaderLeadership.getCdcReader().getReaderName())
            .collect(Collectors.toList());
    return new ResponseEntity<>(cdcReaderNames, HttpStatus.OK);
  }

  @RequestMapping(value = "/{reader}/finished", method = RequestMethod.GET)
  public ResponseEntity isCdcReaderFinished(@PathVariable("reader") String reader) {
    return getStatus(reader)
        .map(
            status ->
                new ResponseEntity<>(
                    status.isCdcProcessingFinished()
                        ? HttpStatus.OK
                        : HttpStatus.SERVICE_UNAVAILABLE))
        .orElse(ResponseEntity.notFound().build());
  }

  @RequestMapping(value = "{reader}/status", method = RequestMethod.GET)
  public ResponseEntity<CdcReaderProcessingStatus> getCdcReaderStatus(
      @PathVariable("reader") String reader) {
    return getStatus(reader)
        .map(s -> new ResponseEntity<>(s, HttpStatus.OK))
        .orElse(ResponseEntity.notFound().build());
  }

  private Optional<CdcReaderProcessingStatus> getStatus(String readerName) {
    return Optional.ofNullable(cdcReaderProvider.get(readerName))
        .map(
            readerLeadership ->
                readerLeadership.getCdcReader().getCdcProcessingStatusService().getCurrentStatus());
  }
}
