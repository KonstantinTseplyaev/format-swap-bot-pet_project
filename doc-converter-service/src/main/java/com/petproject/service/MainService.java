package com.petproject.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface MainService {
    void processTextUpdate(Update update);
    void processDocumentUpdate(Update update);
    void processCallbackUpdate(Update update);
}
