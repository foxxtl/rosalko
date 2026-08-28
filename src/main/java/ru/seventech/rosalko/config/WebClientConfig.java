package ru.seventech.rosalko.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${webclient.http.connect-timeout}")
    private int connectTimeout;

    @Value("${webclient.http.response-timeout}")
    private int responseTimeout;

    @Bean
    public WebClient rosalkoWebClient(WebClient.Builder builder) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(responseTimeout));

        return builder.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }
}
