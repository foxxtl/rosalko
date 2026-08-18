package ru.seventech.rosalko.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;
import ru.seventech.rosalko.dto.docstore.DocStoreResponseDTO;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocStoreServiceTest {

    @Mock
    private BaseSecurityHelper securityHelper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private DocStoreService service;

    @BeforeEach
    void setUp() {
        service = new DocStoreService(securityHelper, restTemplate, objectMapper);
        ReflectionTestUtils.setField(service, "docStoreUrl", "http://docstore/api/files");
    }

    @Test
    void saveFile_shouldUploadFileAndReturnResponse() throws Exception {
        File file = File.createTempFile("test-rosalko", ".zip");

        String json = """
                {
                  "uuid": "12345"
                }
                """;
        DocStoreResponseDTO dto = new DocStoreResponseDTO();
        ResponseEntity<String> response = ResponseEntity.ok(json);

        when(restTemplate.postForEntity(
                eq("http://docstore/api/files"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        when(objectMapper.readValue(json, DocStoreResponseDTO.class)).thenReturn(dto);
        DocStoreResponseDTO result = service.saveFile(file);
        assertSame(dto, result);

        verify(restTemplate)
                .postForEntity(
                        eq("http://docstore/api/files"),
                        any(HttpEntity.class),
                        eq(String.class)
                );

        verify(objectMapper).readValue(json, DocStoreResponseDTO.class);

        assertFalse(file.exists(), "Temp file should be deleted");
    }


    @Test
    void saveFile_shouldThrowException_whenRestTemplateFails() throws Exception {
        File file = File.createTempFile("test-rosalko", ".zip");

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("connection error"));

        CustomMessageException exception = assertThrows(CustomMessageException.class, () -> service.saveFile(file));

        assertEquals("Error while save file to docstore", exception.getMessage());
        assertTrue(file.exists());
        file.delete();
    }


    @Test
    void saveFile_shouldThrowException_whenJsonParsingFails() throws Exception {
        File file = File.createTempFile("test-rosalko", ".zip");

        ResponseEntity<String> response = ResponseEntity.ok("{}");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        when(objectMapper.readValue(anyString(), eq(DocStoreResponseDTO.class))).thenThrow(new RuntimeException("json error"));

        CustomMessageException exception = assertThrows(CustomMessageException.class, () -> service.saveFile(file));

        assertEquals("Error while save file to docstore", exception.getMessage());
        assertFalse(file.exists());
    }


    @Test
    void saveFile_shouldSendMultipartRequest() throws Exception {
        File file = File.createTempFile("test-rosalko", ".zip");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(ResponseEntity.ok("{}"));
        when(objectMapper.readValue(anyString(), eq(DocStoreResponseDTO.class))).thenReturn(new DocStoreResponseDTO());

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        service.saveFile(file);

        verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(String.class));

        HttpEntity<?> request = captor.getValue();
        assertInstanceOf(MultiValueMap.class, request.getBody());
        MultiValueMap<?, ?> body = (MultiValueMap<?, ?>) request.getBody();

        assertTrue(body.containsKey("file"));
        file.delete();
    }
}
