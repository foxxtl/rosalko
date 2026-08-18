package ru.seventech.rosalko.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.rosalko.dto.docstore.DocStoreResponseDTO;
import ru.seventech.rosalko.rabbit.RabbitProducer;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RosalkoLicenseeServiceTest {
    @Mock
    private DocStoreService docStoreService;

    @Mock
    private RosalkoConnectorService rosalkoConnectorService;

    @Mock
    private RabbitProducer rabbitProducer;

    @InjectMocks
    private RosalkoLicenseeService service;

    @Test
    void refresh_shouldDownloadSaveAndSendFile() {
        String html = """
                <html>
                    <a href="https://fsrar.gov.ru/opendata/licensees.zip">
                </html>
                """;

        String zipUrl = "https://fsrar.gov.ru/opendata/licensees.zip";

        File zipFile = new File("test.zip");

        DocStoreResponseDTO response = new DocStoreResponseDTO();

        when(rosalkoConnectorService.getHtmlPage()).thenReturn(html);
        when(rosalkoConnectorService.downloadFile(zipUrl, ".zip")).thenReturn(zipFile);
        when(docStoreService.saveFile(zipFile)).thenReturn(response);

        service.refresh();

        InOrder inOrder = Mockito.inOrder(
                rosalkoConnectorService,
                docStoreService,
                rabbitProducer
        );

        inOrder.verify(rosalkoConnectorService).getHtmlPage();
        inOrder.verify(rosalkoConnectorService).downloadFile(zipUrl, ".zip");
        inOrder.verify(docStoreService).saveFile(zipFile);
        inOrder.verify(rabbitProducer).sendTransformerMessage(response);
        verifyNoMoreInteractions(rabbitProducer, docStoreService);
    }

    @Test

    void refresh_shouldThrowException_whenZipUrlNotFound() {
        String html = """
            no zip here
            """;

        when(rosalkoConnectorService.getHtmlPage()).thenReturn(html);

        CustomMessageException exception = assertThrows(CustomMessageException.class, () -> service.refresh());

        assertEquals("Can't find url by regex", exception.getMessage());

        verify(rosalkoConnectorService).getHtmlPage();
        verifyNoInteractions(docStoreService, rabbitProducer);
    }


    @Test
    void refresh_shouldPropagateException_whenDownloadFailed() {
        String html = "https://fsrar.gov.ru/opendata/licensees.zip";

        when(rosalkoConnectorService.getHtmlPage()).thenReturn(html);
        when(rosalkoConnectorService.downloadFile(anyString(), eq(".zip"))).thenThrow(new RuntimeException("download error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.refresh());

        assertEquals("download error", exception.getMessage());

        verify(docStoreService, never()).saveFile(any());
        verify(rabbitProducer, never()).sendTransformerMessage(any());
    }

    @Test
    void refresh_shouldPropagateException_whenDocStoreFailed() {
        String html = "https://fsrar.gov.ru/opendata/licensees.zip";

        File file = new File("test.zip");

        when(rosalkoConnectorService.getHtmlPage()).thenReturn(html);
        when(rosalkoConnectorService.downloadFile(anyString(), eq(".zip"))).thenReturn(file);
        when(docStoreService.saveFile(file)).thenThrow(new RuntimeException("docstore error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.refresh());

        assertEquals("docstore error", exception.getMessage());

        verify(rabbitProducer, never()).sendTransformerMessage(any());
    }

    @Test
    void refresh_shouldNotSendMessage_whenSaveReturnsNull() {
        String html = "https://fsrar.gov.ru/opendata/licensees.zip";

        File file = new File("test.zip");

        when(rosalkoConnectorService.getHtmlPage()).thenReturn(html);
        when(rosalkoConnectorService.downloadFile(anyString(), eq(".zip"))).thenReturn(file);
        when(docStoreService.saveFile(file)).thenReturn(null);

        service.refresh();

        verify(rabbitProducer).sendTransformerMessage(null);
    }

}
