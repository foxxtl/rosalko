package ru.seventech.rosalko.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class RosalkoConnectorService extends BaseWebClient {

    @Value(value = "${rosalko.html-page-url}")
    private String rosalkoHtmlPageUrl;

    public RosalkoConnectorService(BaseSecurityHelper securityHelper, WebClient webClient) {
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
     * Метод скачивает файл, параллельно записывая содержимое во временный файл.
     *
     * @return - путь к временному файлу
     */
    public Mono<Path> downloadFile(String archiveUrl, String format) {
        return Mono.defer(() -> {
            try {
                log.info("Start download file by url: {}", archiveUrl);
                Path filePath = Files.createTempFile("rosalko-", format);

                return getByUrl(archiveUrl, rosalkoHeaders())
                        .bodyToFlux(DataBuffer.class)
                        .as(dataBuffers -> DataBufferUtils.write(dataBuffers, filePath))
                        .then(Mono.fromRunnable(() -> log.info("End download file by url: {}", archiveUrl)))
                        .thenReturn(filePath)
                        .doOnError(e -> deleteTempFile(filePath));
            } catch (IOException e) {
                return Mono.error(new CustomMessageException("Error creating temp file", e));
            }
        });
    }

}
