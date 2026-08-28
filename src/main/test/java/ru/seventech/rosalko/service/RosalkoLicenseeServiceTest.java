package ru.seventech.rosalko.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.rosalko.dto.docstore.DocStoreResponseDTO;
import ru.seventech.rosalko.rabbit.RabbitProducer;

import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RosalkoLicenseeServiceTest {

    @Mock
    private RosalkoTransferService rosalkoTransferService;

    @Mock
    private RabbitProducer rabbitProducer;

    @Mock
    private DocStoreResponseDTO docStoreResponse;

    private RosalkoLicenseeService service;

    @BeforeEach
    void setUp() {
        service = new RosalkoLicenseeService(rosalkoTransferService, rabbitProducer);
    }

    @Test
    void refresh_success() {
        String html = "<html><a href=\"https://fsrar.gov.ru/opendata/test/licensees.zip\">download</a></html>";

        String archiveUrl = "https://fsrar.gov.ru/opendata/test/licensees.zip";

        when(rosalkoTransferService.getHtmlPage()).thenReturn(html);
        when(rosalkoTransferService.transferFile(archiveUrl, ".zip")).thenReturn(docStoreResponse);

        service.refresh();

        verify(rosalkoTransferService).getHtmlPage();
        verify(rosalkoTransferService).transferFile(archiveUrl, ".zip");
        verify(rabbitProducer).sendTransformerMessage(docStoreResponse);
        verifyNoMoreInteractions(rosalkoTransferService, rabbitProducer);
    }

    @Test
    void refresh_shouldThrowException_whenZipUrlNotFound() {
        String html = "<html><a href=\"https://fsrar.gov.ru/some-page\">download</a></html>";
        when(rosalkoTransferService.getHtmlPage()).thenReturn(html);

        assertThrows(CustomMessageException.class, () -> service.refresh());
        verify(rosalkoTransferService).getHtmlPage();
        verify(rosalkoTransferService, never()).transferFile(anyString(), anyString());
        verifyNoInteractions(rabbitProducer);
    }

    @Test
    void refresh_shouldSkip_whenAnotherRefreshIsRunning() throws Exception {
        var field = RosalkoLicenseeService.class.getDeclaredField("semaphore");
        field.setAccessible(true);
        Semaphore semaphore = (Semaphore) field.get(service);
        semaphore.acquire();

        try {
            service.refresh();
            verifyNoInteractions(rosalkoTransferService, rabbitProducer);
        } finally {
            semaphore.release();
        }
    }


}
