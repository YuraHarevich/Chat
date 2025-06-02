package ru.kharevich.chatservice.service;

import ru.kharevich.chatservice.dto.other.MessageTransferEntity;
import ru.kharevich.chatservice.model.Message;

public interface SentMessageProcessService {
    void processSentMessage(MessageTransferEntity message);
}
