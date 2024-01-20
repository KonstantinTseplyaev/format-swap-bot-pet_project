package com.petproject.service;

import com.petproject.entity.AppUser;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface AppUserService {
    String registerUser(AppUser user);

    AppUser findOrCreateUser(Message message);

    void setBasicState(AppUser user);
}
