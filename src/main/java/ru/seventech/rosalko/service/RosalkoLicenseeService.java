package ru.seventech.rosalko.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.rosalko.dto.docstore.DocStoreResponseDTO;
import ru.seventech.rosalko.rabbit.RabbitProducer;

import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class RosalkoLicenseeService {

    private final RosalkoTransferService rosalkoTransferService;
    private final RabbitProducer rabbitProducer;

    /**
     * Семафор гарантирует, что в один момент времени активен один поток.
     * Необходимо для исключения возможности одновременной обработки одного файла Росалкогольрегулирования.
     */
    private final Semaphore semaphore = new Semaphore(1);

    public RosalkoLicenseeService(RosalkoTransferService rosalkoTransferService, RabbitProducer rabbitProducer) {
        this.rosalkoTransferService = rosalkoTransferService;
        this.rabbitProducer = rabbitProducer;
    }

    /**
     * Метод получает html страницу, извлекает из нее url для скачивания .zip архива, получает файл, сохраняет в docstore,
     * передает uuid файла в transformer-service на обработку
     */
    @PostConstruct
    public void refresh() {

        if (!semaphore.tryAcquire()) {
            log.warn("Refresh process is already running. Skipping this execution.");
            return;
        }

        try {
            log.info("Start refreshing rosalko licensees");
            String htmlPageUrl = rosalkoTransferService.getHtmlPage();
            String archiveUrl = extractDownloadUrl(htmlPageUrl);
            DocStoreResponseDTO docStoreResponse = rosalkoTransferService.transferFile(archiveUrl, ".zip");
            rabbitProducer.sendTransformerMessage(docStoreResponse);
            log.info("Licensee file send to transformer");
        } finally {
            semaphore.release();
        }
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
