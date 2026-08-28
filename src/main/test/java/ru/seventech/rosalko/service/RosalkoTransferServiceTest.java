package ru.seventech.rosalko.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.function.Consumer;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;
import ru.seventech.rosalko.dto.docstore.DocStoreResponseDTO;
import org.springframework.http.HttpHeaders;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RosalkoTransferServiceTest {

    @Mock
    private BaseSecurityHelper securityHelper;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private RosalkoTransferService service;

    @BeforeEach
    void setUp() {
        service = spy(new RosalkoTransferService(securityHelper, webClient));
        ReflectionTestUtils.setField(service, "rosalkoHtmlPageUrl", "https://fsrar.gov.ru/test");
        ReflectionTestUtils.setField(service, "docStoreUrl", "https://docstore.test");
        ReflectionTestUtils.setField(service,"downloadUrl","/api/v1/files");
    }

    @Test
    void getHtmlPage_shouldReturnHtml() {
        String html = "<html><body>test</body></html>";

        doReturn(responseSpec)
                .when(service)
                .getByUrl(
                        eq("https://fsrar.gov.ru/test"),
                        argThat(headers -> headers != null)
                );

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(html));
        String result = service.getHtmlPage();
        assertEquals(html, result);
    }

    @Test
    void getHtmlPage_shouldThrowException_whenRequestFails() {
        RuntimeException exception =
                new RuntimeException("Connection error");

        doThrow(exception)
                .when(service)
                .getByUrl(
                        eq("https://fsrar.gov.ru/test"),
                        argThat(headers -> headers != null)
                );

        CustomMessageException result = assertThrows(CustomMessageException.class, () -> service.getHtmlPage());
        assertEquals("Error getting html page", result.getMessage());
        assertSame(exception, result.getCause());
    }

    @Test
    void transferFile_shouldReturnResponse() {
        String fileUrl = "https://fsrar.gov.ru/opendata/test.zip";

        DocStoreResponseDTO responseDTO = mock(DocStoreResponseDTO.class);
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap("test file".getBytes());
        WebClient.ResponseSpec downloadResponseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(downloadResponseSpec).when(service).getByUrl(eq(fileUrl), any());
        when(downloadResponseSpec.bodyToFlux(DataBuffer.class)).thenReturn(Flux.just(dataBuffer));

        doReturn(responseSpec).when(service)
                .postByUrl(eq("https://docstore.test/api/v1/files"),
                any(BodyInserter.class), any());

        when(responseSpec.bodyToMono(DocStoreResponseDTO.class)).thenReturn(Mono.just(responseDTO));
        when(responseDTO.getFile_uuid()).thenReturn(UUID.fromString("8d27ca8b-2268-4142-8f41-e5165468965a"));

        DocStoreResponseDTO result = service.transferFile(fileUrl, ".zip");

        assertSame(responseDTO, result);
        verify(service).getByUrl(eq(fileUrl), any());
        verify(downloadResponseSpec).bodyToFlux(DataBuffer.class);
        verify(service).postByUrl(eq("https://docstore.test/api/v1/files"), any(BodyInserter.class), any());
        verify(responseSpec).bodyToMono(DocStoreResponseDTO.class);
    }

    @Test
    void transferFile_shouldThrowException_whenRequestFails() {
        String fileUrl = "https://fsrar.gov.ru/opendata/test.zip";
        RuntimeException exception = new RuntimeException("Download error");

        doThrow(exception).when(service).getByUrl(eq(fileUrl), any());

        CustomMessageException result = assertThrows(CustomMessageException.class, () -> service.transferFile(fileUrl, ".zip"));

        assertEquals("Error while transfer file by url: " + fileUrl, result.getMessage());
        assertSame(exception, result.getCause());
        verify(service).getByUrl(eq(fileUrl), any());
        verify(service, never()).postByUrl(anyString(), any(), any());
    }
}
