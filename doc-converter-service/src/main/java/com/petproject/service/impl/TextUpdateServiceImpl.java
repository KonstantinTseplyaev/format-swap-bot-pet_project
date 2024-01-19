package com.petproject.service.impl;

import com.petproject.dao.AppUserDao;
import com.petproject.entity.AppUser;
import com.petproject.entity.enums.UserState;
import com.petproject.model.ChatCommand;
import com.petproject.service.AppUserService;
import com.petproject.service.ProducerService;
import com.petproject.service.TextUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static com.petproject.entity.enums.UserState.BASIC_STATE;
import static com.petproject.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static com.petproject.model.ChatCommand.CANSEL;
import static com.petproject.model.ChatCommand.HELP;
import static com.petproject.model.ChatCommand.REGISTRATION;
import static com.petproject.model.ChatCommand.START;

@Service
@RequiredArgsConstructor
public class TextUpdateServiceImpl implements TextUpdateService {
    private final ProducerService producerService;
    private final AppUserService appUserService;
    private final AppUserDao appUserDao;

    @Override
    public void processUpdate(Update update) {
        Message message = update.getMessage();
        AppUser user = findOrCreateUser(message);
        UserState userState = user.getState();
        String textMessage = message.getText();
        long chatId = message.getChatId();
        ChatCommand command = ChatCommand.fromValue(textMessage);
        String answer;

        if (CANSEL.equals(command)) {
            answer = processCanselCommand(user);
        } else if (userState.equals(BASIC_STATE)) {
            answer = processServiceCommands(command, user);
        } else if (userState.equals(WAIT_FOR_EMAIL_STATE)) {
            answer = setEmail(user, textMessage);
        } else {
            answer = "Неизвестная ошибка! Введите /cansel и попробуйте снова";
        }

        sendAnswer(answer, chatId);
    }

    private String setEmail(AppUser user, String textMessage) {
        return "";
    }

    private String processServiceCommands(ChatCommand command, AppUser user) {
        if (HELP.equals(command)) {
            return sendHelp();
        } else if (START.equals(command)) {
            return sendGreeting(user);
        } else if (REGISTRATION.equals(command)) {
            return appUserService.registerUser(user);
        } else {
            return "Неизвестная комманда! Чтобы посмотреть список доступных комманд введите /help";
        }
    }

    private String sendHelp() {
        return """
                Отправьте в чат свой документ и выберите формат, в который нужно конвертировать
                (доступно только зарегестрированным пользователям)
                                
                Список доступных комманд:
                /cansel - отмена выполнения текущей комманды
                /registration - регистрация пользователя\s""";
    }

    private String sendGreeting(AppUser user) {
        return "Приветствую, " + user.getFirstname() + "! Я FormatSwapBot - " +
                "лучший бот по конвертации ваших документов в нужный формат! " +
                "Введите /help, чтобы подробнее узнать о моих возможностях";
    }

    private String processCanselCommand(AppUser user) {
        user.setState(BASIC_STATE);
        appUserDao.save(user);
        return "Комманда отменена";
    }

    private AppUser findOrCreateUser(Message message) {
        User telegramUser = message.getFrom();
        Optional<AppUser> user = appUserDao.findByTelegramUserId(telegramUser.getId());
        if (user.isEmpty()) {
            AppUser newUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .telegramUserName(telegramUser.getUserName())
                    .firstname(telegramUser.getFirstName())
                    .lastname(telegramUser.getLastName())
                    .state(BASIC_STATE)
                    .isActive(false)
                    .build();
            return appUserDao.save(newUser);
        }
        return user.get();
    }

    private void sendAnswer(String answer, long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .text(answer)
                .chatId(chatId)
                .build();
        producerService.produceAnswer(sendMessage);
    }
}
