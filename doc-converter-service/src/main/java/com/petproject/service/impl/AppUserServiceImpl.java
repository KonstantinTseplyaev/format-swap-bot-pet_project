package com.petproject.service.impl;

import com.petproject.dao.AppUserDao;
import com.petproject.entity.AppUser;
import com.petproject.service.AppUserService;
import com.petproject.service.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static com.petproject.entity.enums.UserState.BASIC_STATE;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {
    private final ProducerService producerService;
    private final AppUserDao appUserDao;

    @Override
    public String registerUser(AppUser user) {
        return "Ура! Вы успешно зарегестрировались!";
    }

    @Override
    public AppUser findOrCreateUser(Message message) {
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

    @Override
    public void setBasicState(AppUser user) {
        user.setState(BASIC_STATE);
        appUserDao.save(user);
    }
}
