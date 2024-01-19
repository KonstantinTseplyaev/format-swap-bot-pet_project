package com.petproject.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface TextUpdateService {
    void processUpdate(Update update);
}
