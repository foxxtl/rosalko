package ru.seventech.rosalko.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.rosalko.dto.docstore.DocStoreResponseDTO;
import ru.seventech.rosalko.rabbit.RabbitProducer;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class RosalkoLicenseeService {

    private final DocStoreService docStoreService;
    private final RosalkoConnectorService rosalkoConnectorService;
    private final RabbitProducer rabbitProducer;

    public RosalkoLicenseeService(DocStoreService docStoreService, RosalkoConnectorService rosalkoConnectorService, RabbitProducer rabbitProducer) {
        this.rosalkoConnectorService = rosalkoConnectorService;
        this.docStoreService = docStoreService;
        this.rabbitProducer = rabbitProducer;
    }

    /**
     * Метод получает html страницу, извлекает из нее url для скачивания .zip архива, получает файл, сохраняет в docstore,
     * передает uuid файла в transformer-service на обработку
     */
    public void refresh() {
        log.info("Start refreshing rosalko licensees, {}", LocalDateTime.now());
        String htmlPageUrl = rosalkoConnectorService.getHtmlPage();
        String archiveUrl = extractDownloadUrl(htmlPageUrl);
        Mono<DocStoreResponseDTO> docStoreResponse = downloadAndSave(archiveUrl);
        rabbitProducer.sendTransformerMessage(docStoreResponse.block());
        log.info("Licensee file send to transformer");
    }

    @PostConstruct
    private void init() {
        refresh();
    }

    /**
     * Метод скачивает архив Россалко и сохраняет в docstore
     */
    public Mono<DocStoreResponseDTO> downloadAndSave(String archiveUrl) {
        return rosalkoConnectorService.downloadFile(archiveUrl, ".zip")
                .flatMap(docStoreService::saveFile);
    }

    /**
     * Метод извлекает с html страницы URL на скачивание zip архива
     */
    private String extractDownloadUrl(String htmlContent) {
        String linkRegex = "https://fsrar\\.gov\\.ru/opendata/[^\\s\"']+\\.zip";
        Matcher matcher = Pattern.compile(linkRegex).matcher(htmlContent);

        if (matcher.find()) {
            log.info("Successfully get url for zip archive. Url: {}", matcher.group());
            return matcher.group();
        } else {
            throw new CustomMessageException("Can't find url by regex");
        }
    }

}
