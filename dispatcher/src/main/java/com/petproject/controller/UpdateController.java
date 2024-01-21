package com.petproject.controller;

import com.petproject.service.UpdateProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.petproject.queue.RabbitQueue.CALLBACK_MESSAGE_UPDATE;
import static com.petproject.queue.RabbitQueue.DOCUMENT_MESSAGE_UPDATE;
import static com.petproject.queue.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Log4j
@Component
@RequiredArgsConstructor
public class UpdateController {
    private final TelegramBot telegramBot;
    private final UpdateProducer updateProducer;

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Update is null");
            return;
        }

        if (update.getMessage() != null) {
            distributeMessageByType(update);
        } else if (update.hasCallbackQuery()) {
            distributeCallback(update);
        } else {
            log.error("Unsupported message type: " + update);
        }
    }

    private void distributeCallback(Update update) {
        log.debug("callback data: " + update.getCallbackQuery().getData());
        updateProducer.produce(CALLBACK_MESSAGE_UPDATE, update);
    }

    private void distributeMessageByType(Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            log.debug(message.getText());
            updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
        } else if (message.hasDocument()) {
            updateProducer.produce(DOCUMENT_MESSAGE_UPDATE, update);
        } else {
            log.error("Неподдерживаемый формат!");
            setUnsupportedMessage(message);
        }
    }

    private void setUnsupportedMessage(Message message) {
        SendMessage sendMessage = SendMessage.builder()
                .text("Неподдерживаемый формат!")
                .chatId(message.getChatId())
                .build();
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendAnswer(sendMessage);
    }
}
