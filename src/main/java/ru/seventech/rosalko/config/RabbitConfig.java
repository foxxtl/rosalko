package ru.seventech.rosalko.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@EnableRabbit
@Configuration
public class RabbitConfig {

    public static final String ROSALKO_QUEUE = "q.mdm.transform.rosalko.job";
    public static final String ROSALKO_EXCHANGE = "t.mdm.transform.rosalko.job";

    public static final String ROSALKO_ERROR_QUEUE = "q.mdm.transform.rosalko.job.error";
    public static final String ROSALKO_ERROR_EXCHANGE = "t.mdm.transform.rosalko.job.error";

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    @Primary
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter(objectMapper());
    }

    @ConfigurationProperties(prefix = "rosalko.rabbitmq")
    @Bean(name = "rosalkoRabbitConnection")
    @Primary
    public ConnectionFactory rosalkoRabbitConnection() {
        return new CachingConnectionFactory();
    }

    @Bean(name = "rosalkoRabbitTemplate")
    public RabbitTemplate rosalkoRabbitTemplate(@Qualifier("rosalkoRabbitConnection") ConnectionFactory connectionFactory,
                                                MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.containerAckMode(AcknowledgeMode.MANUAL);
        return rabbitTemplate;
    }

    @Bean(name = "rosalkoExchange")
    DirectExchange rosalkoExchange() {
        return new DirectExchange(ROSALKO_EXCHANGE);
    }

    @Bean(name = "rosalkoQueue")
    Queue rosalkoQueue() {
        return QueueBuilder.durable(ROSALKO_QUEUE).build();
    }

    @Bean
    Binding rosalkoInputBinding(@Qualifier("rosalkoQueue") Queue rosalkoQueue,
                                @Qualifier("rosalkoExchange") Exchange rosalkoExchange) {
        return BindingBuilder.bind(rosalkoQueue).to(rosalkoExchange).with(ROSALKO_QUEUE).noargs();
    }

    @Bean("rosalkoErrorExchange")
    DirectExchange rosalkoErrorExchange() {
        return new DirectExchange(ROSALKO_ERROR_EXCHANGE);
    }

    @Bean(name = "rosalkoErrorQueue")
    Queue rosalkoErrorQueue() {
        return QueueBuilder.durable(ROSALKO_ERROR_QUEUE).build();
    }

    @Bean
    Binding rosalkoErrorBinding(@Qualifier("rosalkoErrorQueue") Queue rosalkoErrorQueue,
                                @Qualifier("rosalkoErrorExchange") Exchange rosalkoErrorExchange) {
        return BindingBuilder.bind(rosalkoErrorQueue).to(rosalkoErrorExchange).with(ROSALKO_ERROR_QUEUE).noargs();
    }

}

