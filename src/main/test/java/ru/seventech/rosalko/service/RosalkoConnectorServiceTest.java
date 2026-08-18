package ru.seventech.rosalko.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;

import java.io.File;
import java.nio.file.Files;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RosalkoConnectorServiceTest {

    @Mock
    private BaseSecurityHelper securityHelper;

    @Mock
    private RestTemplate restTemplate;

    private RosalkoConnectorService service;

    @BeforeEach
    void setUp() {
        service = new RosalkoConnectorService(securityHelper, restTemplate);
        ReflectionTestUtils.setField(service, "rosalkoHtmlPageUrl", "https://fsrar.gov.ru/page");
    }



    @Test
    void getHtmlPage_shouldReturnHtml() {
        String html = "<html>test</html>";

        ResponseEntity<String> response = ResponseEntity.ok(html);

        when(restTemplate.exchange(
                (RequestEntity<?>) any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        String result = service.getHtmlPage();

        assertEquals(html, result);
        verify(restTemplate).exchange((RequestEntity<?>) any(HttpEntity.class), eq(String.class));
    }


    @Test
    void getHtmlPage_shouldThrowException_whenRestTemplateFails() {

        when(restTemplate.exchange(
                (RequestEntity<?>) any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("connection error"));

        CustomMessageException exception = assertThrows(CustomMessageException.class, () -> service.getHtmlPage());
        assertEquals("Error getting html page", exception.getMessage());
    }


    @Test
    void downloadFile_shouldDownloadAndCreateFile() throws Exception {
        byte[] content = "test zip content".getBytes();
        ResponseEntity<byte[]> response = ResponseEntity.ok(content);

        when(restTemplate.exchange(
                (RequestEntity<?>) any(HttpEntity.class),
                eq(byte[].class)
        )).thenReturn(response);

        File result = service.downloadFile("https://fsrar.gov.ru/file.zip", ".zip");

        assertNotNull(result);
        assertTrue(result.exists());
        assertArrayEquals(content, Files.readAllBytes(result.toPath()));
        verify(restTemplate).exchange((RequestEntity<?>) any(HttpEntity.class), eq(byte[].class));
        result.delete();
    }


    @Test
    void downloadFile_shouldThrowException_whenResponseBodyIsNull() {

        ResponseEntity<byte[]> response = ResponseEntity.ok(null);

        when(restTemplate.exchange(
                (RequestEntity<?>) any(HttpEntity.class),
                eq(byte[].class)
        )).thenReturn(response);

        CustomMessageException exception = assertThrows(
                CustomMessageException.class,
                () -> service.downloadFile(
                        "https://fsrar.gov.ru/file.zip",
                        ".zip"
                )
        );

        assertTrue(exception.getMessage().contains("Error while download file"));
        verify(restTemplate).exchange(
                (RequestEntity<?>) any(HttpEntity.class),
                eq(byte[].class)
        );
    }


    @Test
    void downloadFile_shouldWrapException_whenRestTemplateFails() {
        when(restTemplate.exchange(
                (RequestEntity<?>) any(HttpEntity.class),
                eq(byte[].class)
        )).thenThrow(new RuntimeException("timeout"));

        CustomMessageException exception = assertThrows(
                CustomMessageException.class,
                () -> service.downloadFile(
                        "https://fsrar.gov.ru/file.zip",
                        ".zip"
                )
        );

        assertEquals("Error while download file by url: https://fsrar.gov.ru/file.zip", exception.getMessage());

        verify(restTemplate).exchange(
                (RequestEntity<?>) any(HttpEntity.class),
                eq(byte[].class)
        );
    }
}
