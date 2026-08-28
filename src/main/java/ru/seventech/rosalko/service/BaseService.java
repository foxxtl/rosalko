package ru.seventech.rosalko.service;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;

import java.net.URI;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Getter
public abstract class BaseService {

    private final BaseSecurityHelper securityHelper;
    private final RestTemplate rest;

    public BaseService(BaseSecurityHelper securityHelper, RestTemplate rest) {
        this.securityHelper = securityHelper;
        this.rest = rest;
    }

    protected HttpHeaders headers(boolean isPublic) {
        return headers(isPublic, MediaType.APPLICATION_JSON);
    }

    protected HttpHeaders headers(boolean isPublic, MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        prepareAuthenticationHeader(headers, isPublic);
        return headers;
    }

    protected RequestEntity request(HttpHeaders headers, HttpMethod method, URI uri) {
        return new RequestEntity(headers, method, uri);
    }

    protected String getToken() {
        return securityHelper.obtainSysAuthToken();
    }

    private void prepareAuthenticationHeader(HttpHeaders headers, boolean isPublic) {
        if (isPublic) {
            headers.set("Authorization", securityHelper.obtainSysAuthToken());
        } else {
            String token = securityHelper.prepareAuthenticationHeader();
            if (isNull(token)) {
                throw new CustomMessageException("Authentication header is missing");
            }
            headers.set("Authorization", token);
        }
    }

}

