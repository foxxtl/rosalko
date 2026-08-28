package ru.seventech.rosalko.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;
import ru.seventech.rosalko.dto.docstore.DocStoreResponseDTO;

@Slf4j
@Service
public class RosalkoTransferService extends BaseWebClient {

    @Value(value = "${rosalko.html-page-url}")
    private String rosalkoHtmlPageUrl;

    @Value("${storage-url.base-url}")
    private String docStoreUrl;

    @Value("${storage-url.download-url}")
    private String downloadUrl;

    public RosalkoTransferService(BaseSecurityHelper securityHelper, WebClient webClient) {
        super(securityHelper, webClient);
    }

    /**
     * @return String - html представление страницы
     */
    public String getHtmlPage() {
        try {
            return getByUrl(rosalkoHtmlPageUrl, rosalkoHeaders())
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new CustomMessageException("Error getting html page", e);
        }
    }

    /**
     * Метод скачивает файл, параллельно записывая содержимое в docstore.
     */
    public DocStoreResponseDTO transferFile(String fileUrl, String format) {
        try {
            log.info("Start transfer file from url: {}", fileUrl);
            Flux<DataBuffer> fileContent = getByUrl(fileUrl, rosalkoHeaders()).bodyToFlux(DataBuffer.class);

            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            builder.asyncPart("file", fileContent, DataBuffer.class)
                    .filename("rosalko" + format);

            DocStoreResponseDTO responseDTO = postByUrl(docStoreUrl + downloadUrl, BodyInserters.fromMultipartData(builder.build()), headers(true, MediaType.MULTIPART_FORM_DATA))
                    .bodyToMono(DocStoreResponseDTO.class).block();

            log.info("End transfer file url: {}. Docstore uuid: {}", fileUrl, responseDTO.getFile_uuid());
            return responseDTO;
        } catch (Exception e) {
            throw new CustomMessageException("Error while transfer file by url: " + fileUrl, e);
        }
    }

}
