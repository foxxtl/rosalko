package ru.seventech.rosalko.service.ivis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.basetemplate.util.BaseSecurityHelper;
import ru.seventech.rosalko.config.ivis.IvisWebConfig;
import ru.seventech.rosalko.dto.ivis.IvisRequestDto;
import ru.seventech.rosalko.dto.ivis.IvisResponseDto;
import ru.seventech.rosalko.service.BaseWebClient;

import java.util.List;

@Slf4j
@Service
public class IvisLicensesService extends BaseWebClient {

    private final IvisWebConfig webConfig;

    private final IvisCacheService cacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public IvisLicensesService(BaseSecurityHelper securityHelper, WebClient webClient,
                               IvisWebConfig webConfig, IvisCacheService cacheService) {
        super(securityHelper, webClient);
        this.cacheService = cacheService;
        this.webConfig = webConfig;
    }

    @Async
    public void refresh(Integer sphere) {
        cacheService.checkSphere(sphere);
        List<IvisResponseDto> licences = getLicenses(sphere);
        cacheService.updateCache(licences, sphere);
    }

    public IvisResponseDto search(IvisRequestDto requestDto) {
        return cacheService.search(requestDto);
    }

    /**
     * Получение лицензий Россельхознадзора
     */
    private List<IvisResponseDto> getLicenses(Integer sphere) {
        try {
            String jsonContent = getByUrl(buildUrl(sphere), basicAuthHeaders(webConfig.getUsername(), webConfig.getPassword()))
                    .bodyToMono(String.class)
                    .block();

            return objectMapper.readValue(jsonContent, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error getting licences for sphere {}", sphere, e);
            throw new CustomMessageException("Error getting licences for sphere " + sphere, e);
        }
    }

    private String buildUrl(Integer sphere) {
        return UriComponentsBuilder.fromUriString(webConfig.getUrl())
                .queryParam("sphere", sphere)
                .build(true)
                .toString();
    }

}
