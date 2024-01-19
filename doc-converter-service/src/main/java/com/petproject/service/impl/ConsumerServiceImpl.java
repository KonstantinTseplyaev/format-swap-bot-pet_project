package com.petproject.service.impl;

import com.petproject.service.ConsumerService;
import com.petproject.service.DocumentUpdateService;
import com.petproject.service.TextUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.petproject.queue.RabbitQueue.DOCUMENT_MESSAGE_UPDATE;
import static com.petproject.queue.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Log4j
@Service
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {
    private final DocumentUpdateService documentService;
    private final TextUpdateService textUpdateService;

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdate(Update update) {
        log.debug("Text message is reserved");
        textUpdateService.processUpdate(update);
    }

    @Override
    @RabbitListener(queues = DOCUMENT_MESSAGE_UPDATE)
    public void consumeDocumentMessageUpdate(Update update) {
        log.debug("Document message is reserved");
        documentService.processUpdate(update);
    }
}
