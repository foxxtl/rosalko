package ru.seventech.rosalko.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import ru.seventech.basetemplate.util.BaseSecurityHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BaseServiceTest {

    @Mock
    private BaseSecurityHelper securityHelper;

    @Mock
    private RestTemplate restTemplate;

    private TestBaseService service;


    @BeforeEach
    void setUp() {
        service = new TestBaseService(securityHelper, restTemplate);
    }

    @Test
    void rosalkoHeaders_shouldContainBrowserHeaders() {
        HttpHeaders headers = service.testRosalkoHeaders();

        assertEquals("*/*", headers.getFirst(HttpHeaders.ACCEPT));
        assertEquals(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36",
                headers.getFirst(HttpHeaders.USER_AGENT)
        );
    }


    @Test
    void headers_shouldAddAuthorizationForPublicRequest() {
        when(securityHelper.obtainSysAuthToken()).thenReturn("Bearer token");
        HttpHeaders headers = service.testHeaders(true);

        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(
                "Bearer token",
                headers.getFirst("Authorization")
        );
    }


    @Test
    void headers_shouldNotAddAuthorization_whenTokenNull() {
        when(securityHelper.prepareAuthenticationHeader()).thenReturn(null);
        HttpHeaders headers = service.testHeaders(false);
        assertNull(headers.getFirst("Authorization"));
    }


    @Test
    void getToken_shouldReturnToken() {
        when(securityHelper.obtainSysAuthToken()).thenReturn("token");
        assertEquals("token", service.testGetToken());
    }
}
