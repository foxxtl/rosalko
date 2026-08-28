package ru.seventech.rosalko.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static ru.seventech.rosalko.config.RabbitConfig.ROSALKO_EXCHANGE;
import static ru.seventech.rosalko.config.RabbitConfig.ROSALKO_QUEUE;


@Slf4j
@Service
public class RabbitProducer {

    private final RabbitTemplate rabbitTemplate;

    public RabbitProducer(@Qualifier("transformerRabbitTemplate") RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTransformerMessage(Object messageBody) {
        log.info("Sending message to transform service. Body: {}", messageBody);
        rabbitTemplate.convertAndSend(ROSALKO_EXCHANGE, ROSALKO_QUEUE, messageBody);
    }


}
