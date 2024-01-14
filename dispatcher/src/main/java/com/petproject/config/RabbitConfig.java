package com.petproject.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.petproject.queue.RabbitQueue.ANSWER_MESSAGE;
import static com.petproject.queue.RabbitQueue.DOCUMENT_MESSAGE_UPDATE;
import static com.petproject.queue.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Configuration
public class RabbitConfig {
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue textMessageQueue() {
        return new Queue(TEXT_MESSAGE_UPDATE);
    }

    @Bean
    public Queue DocumentMessageQueue() {
        return new Queue(DOCUMENT_MESSAGE_UPDATE);
    }

    @Bean
    public Queue AnswerMessageQueue() {
        return new Queue(ANSWER_MESSAGE);
    }
}
