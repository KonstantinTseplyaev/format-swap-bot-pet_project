package com.petproject.service.impl;

import com.petproject.controller.UpdateController;
import com.petproject.service.AnswerConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.petproject.queue.RabbitQueue.ANSWER_MESSAGE;

@Service
@RequiredArgsConstructor
public class AnswerConsumerImpl implements AnswerConsumer {
    private final UpdateController updateController;

    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void consume(SendMessage sendMessage) {
        updateController.setView(sendMessage);
    }
}
