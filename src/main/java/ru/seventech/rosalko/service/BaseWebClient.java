package ru.seventech.rosalko.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;
import ru.seventech.log.utils.RequestHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Objects.isNull;

@Slf4j
public abstract class BaseWebClient {

    private final BaseSecurityHelper securityHelper;
    private final WebClient webClient;

    public BaseWebClient(BaseSecurityHelper securityHelper, WebClient webClient) {
        this.securityHelper = securityHelper;
        this.webClient = webClient;
    }

    /**
     * Расширенный буфер необходим для получения ответа от ИВИС
     */
    protected WebClient getClient() {
        return webClient.mutate()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(5 * 1024 * 1024)) // 5мб
                .build();
    }

    /**
     * Подготовка GET запроса
     */
    protected WebClient.ResponseSpec getByUrl(String url, Consumer<HttpHeaders> headers) {
        return getClient()
                .get()
                .uri(url)
                .headers(headers)
                .retrieve();
    }

    /**
     * Подготовка POST запроса
     */
    protected WebClient.ResponseSpec postByUrl(String url, BodyInserter<?, ? super ClientHttpRequest> body, Consumer<HttpHeaders> headers) {
        return webClient
                .post()
                .uri(url)
                .headers(headers)
                .body(body)
                .retrieve();
    }


    protected Consumer<HttpHeaders> rosalkoHeaders() {
        return headers -> {
            headers.set(HttpHeaders.USER_AGENT,
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36"
            );
            headers.set(HttpHeaders.ACCEPT, "*/*");
        };
    }

    protected Consumer<HttpHeaders> headers(boolean isPublic, MediaType contentType) {
        return headers -> {
            commonHeader(headers, contentType);
            prepareAuthenticationHeader(headers, isPublic);
        };
    }

    protected Consumer<HttpHeaders> basicAuthHeaders(String username, String password) {
        return headers -> {
            commonHeader(headers, MediaType.APPLICATION_JSON);
            headers.setBasicAuth(username, password);
        };
    }

    private void commonHeader(HttpHeaders headers, MediaType contentType) {
        headers.setContentType(contentType);
        headers.set("request_id", RequestHelper.getRequestId());
    }

    protected void deleteTempFile(Path file) {
        if (Objects.nonNull(file)) {
            try {
                Files.deleteIfExists(file);
                log.info("Successfully delete temp file {}", file);
            } catch (IOException ex) {
                log.warn("Cannot delete temp file '{}'", file, ex);
            }
        }
    }

    private void prepareAuthenticationHeader(HttpHeaders headers, boolean isPublic) {
        if (isPublic) {
            headers.set("Authorization", getToken());
        } else {
            String token = securityHelper.prepareAuthenticationHeader();
            if (isNull(token)) {
                throw new CustomMessageException("Authentication header is missing");
            }
            headers.set("Authorization", token);
        }
    }

    protected String getToken() {
        return securityHelper.obtainSysAuthToken();
    }

}
