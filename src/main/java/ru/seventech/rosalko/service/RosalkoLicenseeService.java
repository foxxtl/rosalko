package ru.seventech.rosalko.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.rosalko.dto.docstore.DocStoreResponseDTO;
import ru.seventech.rosalko.rabbit.RabbitProducer;

import java.nio.file.Path;
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
    @Async
    public void refresh() {
        log.info("Start refreshing rosalko licensees, {}", LocalDateTime.now());
        String htmlPageUrl = rosalkoConnectorService.getHtmlPage();
        String url = extractDownloadUrl(htmlPageUrl);
        Path filePath = rosalkoConnectorService.downloadFile(url, ".zip");
        DocStoreResponseDTO docStoreResponseDTO = docStoreService.saveFile(filePath);
//        rabbitProducer.sendTransformerMessage(docStoreResponseDTO);
        log.info("Licensee file send to transformer");
    }

    @PostConstruct
    private void init() {
        refresh();
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
