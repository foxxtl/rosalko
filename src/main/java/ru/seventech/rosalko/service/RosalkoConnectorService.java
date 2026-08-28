package ru.seventech.rosalko.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;

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
    public Path downloadFile(String fileUrl, String format) {
        Path filePath = null;
        try {
            log.info("Start download file by url: {}", fileUrl);

            filePath = Files.createTempFile("rosalko-", format);
            Path finalPath = filePath;

            getByUrl(fileUrl, rosalkoHeaders())
                    .bodyToFlux(DataBuffer.class)
                    .as(dataBuffers -> DataBufferUtils.write(dataBuffers, finalPath))
                    .block();

            log.info("End download file by url: {}", fileUrl);
            return filePath;
        } catch (Exception e) {
            deleteTempFile(filePath);
            throw new CustomMessageException("Error while download file by url: " + fileUrl, e);
        }
    }

}
