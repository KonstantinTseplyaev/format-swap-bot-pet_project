package com.petproject.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface DocumentUpdateService {
    void processUpdate(Update update);
}
