package ru.kharevich.chatservice.service;

import ru.kharevich.chatservice.dto.other.MessageTransferEntity;

public interface SentMessageProcessService {
    void processSentMessage(MessageTransferEntity message);
}
