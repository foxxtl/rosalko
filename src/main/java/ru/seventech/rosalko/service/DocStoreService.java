package ru.seventech.rosalko.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.seventech.rosalko.dto.docstore.DocStoreResponseDTO;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;

import java.io.File;

@Slf4j
@Service
public class DocStoreService extends BaseService {

    @Value("${storage-url}")
    private String docStoreUrl;

    private final ObjectMapper objectMapper;

    public DocStoreService(BaseSecurityHelper securityHelper, RestTemplate rest, ObjectMapper objectMapper) {
        super(securityHelper, rest);
        this.objectMapper = objectMapper;
    }

    public DocStoreResponseDTO saveFile(File file) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));
            HttpHeaders headers = headers(true, MediaType.MULTIPART_FORM_DATA);

            ResponseEntity<String> response = getRest().postForEntity(docStoreUrl, new HttpEntity<>(body, headers), String.class);

            if (!file.delete()) {
                log.warn("Cannot delete temp file '{}'", file.getAbsolutePath());
            }
            return objectMapper.readValue(response.getBody(), DocStoreResponseDTO.class);
        } catch (Exception e) {
            throw new CustomMessageException("Error while save file to docstore", e);
        }
    }
}
