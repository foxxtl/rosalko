package ru.seventech.rosalko.config.ivis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ivis")
public class IvisCacheConfig {

    private Map<String, CacheSettings> caches = new HashMap<>();

    @Getter
    @Setter
    public static class CacheSettings {
        private Integer sphere;
        private Integer capacity;
    }
}
