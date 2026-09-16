package ru.seventech.rosalko.service.ivis;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import ru.seventech.basetemplate.error.NotFoundException;
import ru.seventech.rosalko.config.ivis.IvisCacheConfig;
import ru.seventech.rosalko.dto.ivis.IvisCacheKey;
import ru.seventech.rosalko.dto.ivis.IvisRequestDto;
import ru.seventech.rosalko.dto.ivis.IvisResponseDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IvisCacheService {

    private final Map<Integer, Cache<IvisCacheKey, IvisResponseDto>> cacheMap = new HashMap<>();

    public IvisCacheService(IvisCacheConfig cacheConfig) {
        cacheConfig.getCaches().values().forEach(settings ->
                cacheMap.put(settings.getSphere(), createCache(settings.getCapacity()))
        );
    }

    public void updateCache(List<IvisResponseDto> licences, Integer sphere) {
        Map<IvisCacheKey, IvisResponseDto> newCache = transformToCache(licences);
        Cache<IvisCacheKey, IvisResponseDto> targetCache = cacheMap.get(sphere);
        targetCache.invalidateAll();
        targetCache.putAll(newCache);
    }

    /**
     * @return - кеш, который атомарно заменит предыдущий
     */
    private Map<IvisCacheKey, IvisResponseDto> transformToCache(List<IvisResponseDto> licences) {
        return Optional.ofNullable(licences)
                .stream()
                .flatMap(List::stream)
                .filter(ivis -> Objects.nonNull(ivis.getLicense()) && ivis.getLicense().isValidLicense())
                .collect(Collectors.toMap(
                        cert -> new IvisCacheKey(cert.getLicense().getNumber(), cert.getLicense().getDateStart()),
                        cert -> cert
                ));
    }

    public IvisResponseDto search(IvisRequestDto requestDto) {
        checkSphere(requestDto.getSphere());
        IvisCacheKey key = new IvisCacheKey(requestDto.getNumber(), requestDto.getDateStart());
        return cacheMap.get(requestDto.getSphere()).getIfPresent(key);
    }

    /**
     * Проверяем сконфигурирован ли передаваемый sphere
     */
    public void checkSphere(Integer sphere) {
        if (!cacheMap.containsKey(sphere)) {
            throw new IllegalArgumentException("Unsupported sphere: " + sphere);
        }
    }

    private Cache<IvisCacheKey, IvisResponseDto> createCache(int capacity) {
        return Caffeine.newBuilder().maximumSize(capacity).build();
    }

}
