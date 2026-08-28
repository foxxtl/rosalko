package ru.seventech.rosalko.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseWebClientTest {

    @Mock
    private BaseSecurityHelper securityHelper;

    @Mock
    private WebClient webClient;

    private TestWebClient client;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;


    private static class TestWebClient extends BaseWebClient {
        TestWebClient(BaseSecurityHelper securityHelper, WebClient webClient) {
            super(securityHelper, webClient);
        }
    }

    @BeforeEach
    void setUp() {
        client = new TestWebClient(securityHelper, webClient);
    }

    @Test
    void getClient_shouldReturnWebClient() {
        assertSame(webClient, client.getClient());
    }

    @Test
    void getByUrl_shouldPrepareGetRequest() {
        String url = "https://example.com/test";

        Consumer<HttpHeaders> headers = httpHeaders -> httpHeaders.set("Test-Header", "test");

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(url);
        doReturn(requestHeadersSpec).when(requestHeadersSpec).headers(headers);
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();

        WebClient.ResponseSpec result = client.getByUrl(url, headers);

        assertSame(responseSpec, result);

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(url);
        verify(requestHeadersSpec).headers(headers);
        verify(requestHeadersSpec).retrieve();
    }

    @Test
    void postByUrl_shouldPreparePostRequest() {
        String url = "https://example.com/test";

        @SuppressWarnings("unchecked")
        BodyInserter<?, ? super ClientHttpRequest> body = mock(BodyInserter.class);

        Consumer<HttpHeaders> headers = httpHeaders -> httpHeaders.set("Test-Header", "test");

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(url);
        doReturn(requestBodySpec).when(requestBodySpec).headers(headers);
        doReturn(requestHeadersSpec).when(requestBodySpec).body(body);
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();

        WebClient.ResponseSpec result = client.postByUrl(url, body, headers);

        assertSame(responseSpec, result);
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(url);
        verify(requestBodySpec).headers(headers);
        verify(requestBodySpec).body(body);
        verify(requestHeadersSpec).retrieve();
    }


    @Test
    void rosalkoHeaders_shouldSetRequiredHeaders() {
        HttpHeaders headers = new HttpHeaders();
        client.rosalkoHeaders().accept(headers);

        assertEquals(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/151.0.0.0 Safari/537.36",
                headers.getFirst(HttpHeaders.USER_AGENT)
        );
        assertEquals("*/*", headers.getFirst(HttpHeaders.ACCEPT));
    }

    @Test
    void headers_shouldSetContentTypeAndPublicToken() {
        String token = "Bearer public-token";

        when(securityHelper.obtainSysAuthToken()).thenReturn(token);

        HttpHeaders headers = new HttpHeaders();

        client.headers(true, MediaType.APPLICATION_JSON).accept(headers);

        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(token, headers.getFirst(HttpHeaders.AUTHORIZATION));
        verify(securityHelper).obtainSysAuthToken();
        verify(securityHelper, never()).prepareAuthenticationHeader();
    }

    @Test
    void headers_shouldSetContentTypeAndPrivateToken() {
        String token = "Bearer private-token";

        when(securityHelper.prepareAuthenticationHeader()).thenReturn(token);
        HttpHeaders headers = new HttpHeaders();

        client.headers(false, MediaType.APPLICATION_JSON).accept(headers);

        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(token, headers.getFirst(HttpHeaders.AUTHORIZATION));
        verify(securityHelper).prepareAuthenticationHeader();
        verify(securityHelper, never()).obtainSysAuthToken();
    }

    @Test
    void headers_shouldThrowException_whenPrivateTokenIsMissing() {
        when(securityHelper.prepareAuthenticationHeader()).thenReturn(null);
        HttpHeaders headers = new HttpHeaders();

        CustomMessageException exception = assertThrows(
                CustomMessageException.class,
                () -> client.headers(false, MediaType.APPLICATION_JSON)
                        .accept(headers)
        );

        assertEquals("Authentication header is missing", exception.getMessage());
        assertNull(headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void getToken_shouldReturnSecurityToken() {
        String token = "test-token";
        when(securityHelper.obtainSysAuthToken()).thenReturn(token);

        assertEquals(token, client.getToken());
        verify(securityHelper).obtainSysAuthToken();
    }

    @Test
    void deleteTempFile_shouldDeleteFile() throws IOException {
        Path file = Files.createTempFile("base-web-client-test", ".tmp");
        assertTrue(Files.exists(file));
        client.deleteTempFile(file);
        assertFalse(Files.exists(file));
    }

    @Test
    void deleteTempFile_shouldDoNothing_whenFileIsNull() {
        client.deleteTempFile(null);
        verifyNoInteractions(securityHelper);
    }

    @Test
    void deleteTempFile_shouldNotThrow_whenFileDoesNotExist() {
        Path file = Path.of(
                "target",
                "does-not-exist",
                "file.tmp"
        );
        client.deleteTempFile(file);
        assertFalse(Files.exists(file));
    }

    @Test
    void deleteTempFile_shouldHandleIOException() {
        Path file = mock(Path.class);

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(file))
                    .thenThrow(new IOException("test exception"));

            assertDoesNotThrow(() -> client.deleteTempFile(file));
        }
    }


}


