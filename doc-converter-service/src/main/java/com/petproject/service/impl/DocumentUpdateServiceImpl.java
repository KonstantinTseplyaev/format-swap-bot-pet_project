package com.petproject.service.impl;

import com.petproject.model.enums.ConvertType;
import com.petproject.service.DocumentUpdateService;
import com.petproject.service.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentUpdateServiceImpl implements DocumentUpdateService {
    private final ProducerService producerService;

    @Override
    public void processUpdate(Update update) {
        Message message = update.getMessage();
        Document document = message.getDocument();
        String docType = document.getMimeType();
        ConvertType type = ConvertType.fromValue(docType);
        long chatId = message.getChatId();
        if (type == null) {
            sendErrorAnswer(chatId);
        } else {
            sendAnswerWithKeyBoard(chatId);
        }
    }

    private void sendAnswerWithKeyBoard(long chatId) {

        SendMessage sendMessage = SendMessage.builder()
                .text("Выберите формат, в который нужно конвертировть")
                .replyMarkup(createButtons())
                .chatId(chatId)
                .build();
        producerService.produceAnswer(sendMessage);
    }

    private ReplyKeyboardMarkup createButtons() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        for (ConvertType type : ConvertType.values()) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(type.getTypeName()));
            rows.add(row);
        }

        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }

    private void sendErrorAnswer(long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .text("Формат вашего файла пока не поддерживается для конвертации")
                .chatId(chatId)
                .build();
        producerService.produceAnswer(sendMessage);
    }
}
