package ru.seventech.rosalko.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;
import ru.seventech.rosalko.dto.docstore.DocStoreResponseDTO;

import java.nio.file.Path;

@Slf4j
@Service
public class DocStoreService extends BaseWebClient {

    @Value("${storage-url.base-url}")
    private String docStoreUrl;

    @Value("${storage-url.download-url}")
    private String downloadUrl;

    private final ObjectMapper objectMapper;

    public DocStoreService(BaseSecurityHelper securityHelper, WebClient webClient, ObjectMapper objectMapper) {
        super(securityHelper, webClient);
        this.objectMapper = objectMapper;
    }

    public DocStoreResponseDTO saveFile(Path tempFilePath) {
        try {
            ResponseEntity<String> response = postByUrl(docStoreUrl + downloadUrl, multipartBody(tempFilePath), headers(true, MediaType.MULTIPART_FORM_DATA))
                    .toEntity(String.class)
                    .block();

            return objectMapper.readValue(response.getBody(), DocStoreResponseDTO.class);
        } catch (Exception e) {
            throw new CustomMessageException("Error while save file to docstore", e);
        } finally {
            deleteTempFile(tempFilePath);
        }
    }

    private BodyInserters.MultipartInserter multipartBody(Path filePath) {
        return BodyInserters.fromMultipartData("file", new FileSystemResource(filePath));
    }

}
