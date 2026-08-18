package ru.seventech.rosalko.service;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import ru.seventech.basetemplate.util.BaseSecurityHelper;

class TestBaseService extends BaseService {

    public TestBaseService(BaseSecurityHelper securityHelper, RestTemplate rest) {
        super(securityHelper, rest);
    }

    public HttpHeaders testRosalkoHeaders() {
        return rosalkoHeaders();
    }

    public HttpHeaders testHeaders(boolean isPublic) {
        return headers(isPublic);
    }

    public String testGetToken() {
        return getToken();
    }
}
