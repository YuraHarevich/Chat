package ru.kharevich.chatservice.dto.other;

public record MessageTransferEntity(
        String chatId,
        String messageId
) {
}
