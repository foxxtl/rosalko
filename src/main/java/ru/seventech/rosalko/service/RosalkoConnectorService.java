package ru.seventech.rosalko.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Objects;

@Service
@Slf4j
public class RosalkoConnectorService extends BaseService {

    @Value(value = "${rosalko.html-page-url}")
    private String rosalkoHtmlPageUrl;

    public RosalkoConnectorService(BaseSecurityHelper securityHelper, RestTemplate rest) {
        super(securityHelper, rest);
    }

    /**
     * @return String - html представление страницы
     */
    public String getHtmlPage() {
        try {
            URI pageUri = URI.create(rosalkoHtmlPageUrl);
            ResponseEntity<String> result = getRest().exchange(request(rosalkoHeaders(), HttpMethod.GET, pageUri), String.class);
            return result.getBody();
        } catch (Exception e) {
            throw new CustomMessageException("Error getting html page", e);
        }
    }


    public File downloadFile(String fileUrl, String format) {
        try {
            log.info("Start download file by url: {}", fileUrl);
            ResponseEntity<byte[]> result = getRest().exchange(request(rosalkoHeaders(), HttpMethod.GET, URI.create(fileUrl)), byte[].class);

            if (Objects.nonNull(result.getBody())) {
                File file = File.createTempFile("rosalko-", format);
                try (OutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(result.getBody());
                }
                log.info("End download file by url: {}", fileUrl);
                return file;
            } else {
                throw new CustomMessageException("Download file is empty");
            }
        } catch (Exception e) {
            throw new CustomMessageException("Error while download file by url: " + fileUrl, e);
        }
    }

}
