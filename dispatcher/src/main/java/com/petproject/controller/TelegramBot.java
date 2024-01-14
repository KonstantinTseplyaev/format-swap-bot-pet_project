package com.petproject.controller;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Log4j
@Component
public class TelegramBot extends TelegramWebhookBot {
    @Value("${bot.name}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.uri}")
    private String botUri;

    @PostConstruct
    void initWebhook() {
        SetWebhook webhook = SetWebhook.builder()
                .url(botUri)
                .build();
        try {
            this.setWebhook(webhook);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return "/update";
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    public void sendAnswer(SendMessage sendMessage) {
        if (sendMessage != null) {
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
    }
}
