package ru.seventech.rosalko.config.ivis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ivis.web")
public class IvisWebConfig {

    private String url;

    private String username;

    private String password;
}
