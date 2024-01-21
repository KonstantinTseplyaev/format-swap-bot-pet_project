package com.petproject.service.impl;

import com.petproject.dao.AppUserDao;
import com.petproject.entity.AppUser;
import com.petproject.entity.enums.UserState;
import com.petproject.model.enums.ChatCommand;
import com.petproject.model.enums.ConvertType;
import com.petproject.service.AppUserService;
import com.petproject.service.FileService;
import com.petproject.service.MainService;
import com.petproject.service.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.petproject.entity.enums.UserState.BASIC_STATE;
import static com.petproject.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static com.petproject.model.enums.ChatCommand.CANSEL;
import static com.petproject.model.enums.ChatCommand.HELP;
import static com.petproject.model.enums.ChatCommand.REGISTRATION;
import static com.petproject.model.enums.ChatCommand.START;
import static com.petproject.model.enums.ConvertType.UNSUPPORTED_TYPE;

@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {
    private final ProducerService producerService;
    private final AppUserService appUserService;
    private final FileService fileService;
    private final AppUserDao appUserDao;

    @Override
    public void processTextUpdate(Update update) {
        Message message = update.getMessage();
        User tgUser = message.getFrom();
        AppUser user = findOrCreateUser(tgUser);
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

    @Override
    public void processDocumentUpdate(Update update) {
        Message message = update.getMessage();
        User tgUser = message.getFrom();
        AppUser user = findOrCreateUser(tgUser);
        Document document = message.getDocument();
        String docId = document.getFileId();
        String docType = document.getMimeType();
        ConvertType type = ConvertType.fromValue(docType);
        long chatId = message.getChatId();
        String answer = "";

        if (user.isActive()) {
            if (type.equals(UNSUPPORTED_TYPE)) {
                answer = "Данный тип файла пока не поддерживается для конвертации";
            } else {
                saveCurrentDoc(docId, docType, user);
                sendAnswerWithKeyBoard(type, chatId);
                return;
            }
        } else if (user.getState().equals(WAIT_FOR_EMAIL_STATE)) {
            answer = "Регистрация не была полностью завершена. " +
                    "Пожалуйста, продолжите регистрацию, введя свой email";
        } else {
            answer = "Для конвертации документов вам необходимо зарегистрироваться. Выберите комманду /registration";
        }

        sendAnswer(answer, chatId);
    }

    @Override
    public void processCallbackUpdate(Update update) {
        CallbackQuery callback = update.getCallbackQuery();
        User tgUser = callback.getFrom();
        AppUser user = findOrCreateUser(tgUser);
        System.out.println("id документа " + user.getCurrentDocId() + ", convertFrom " + user.getCurrentDocType() +
                ", convertTo " + callback.getData());
    }

    private void sendAnswer(String answer, long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .text(answer)
                .chatId(chatId)
                .build();
        producerService.produceAnswer(sendMessage);
    }

    private void sendAnswerWithKeyBoard(ConvertType type, long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .text("Выберите формат, в который нужно конвертировть")
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(createButtons(type)).build())
                .chatId(chatId)
                .build();
        producerService.produceAnswer(sendMessage);
    }

    private List<List<InlineKeyboardButton>> createButtons(ConvertType type) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (ConvertType t : ConvertType.values()) {
            if (!t.equals(type)) {
                buttons.add(List.of(
                        InlineKeyboardButton.builder()
                                .text(t.getTypeName()).callbackData(t.getTypeName()).build()));
            }
        }
        return buttons;
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
                Отправьте в чат свой документ и выберите формат, в который нужно его конвертировать
                (доступно только зарегистрированным пользователям)
                                
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

    private void saveCurrentDoc(String docId, String docType, AppUser user) {
        user.setCurrentDocId(docId);
        user.setCurrentDocType(docType);
        appUserDao.save(user);
    }

    private AppUser findOrCreateUser(User telegramUser) {
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
}
