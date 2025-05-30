package ru.kharevich.chatservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import ru.kharevich.chatservice.dto.other.MessageTransferEntity;
import ru.kharevich.chatservice.exception.ChatNotFoundException;
import ru.kharevich.chatservice.exception.MessageNotFoundException;
import ru.kharevich.chatservice.model.Chat;
import ru.kharevich.chatservice.model.Message;
import ru.kharevich.chatservice.model.MessageStatus;
import ru.kharevich.chatservice.repository.ChatRepository;
import ru.kharevich.chatservice.repository.MessageRepository;
import ru.kharevich.chatservice.service.SentMessageProcessService;
import ru.kharevich.chatservice.utils.constants.ChatServiceResponseConstantMessages;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.kharevich.chatservice.utils.constants.ChatServiceResponseConstantMessages.CHAT_WITH_ID_NOT_FOUND;
import static ru.kharevich.chatservice.utils.constants.ChatServiceResponseConstantMessages.MESSAGE_WITH_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentMessageProcessServiceImpl implements SentMessageProcessService {

    private final MessageRepository messageRepository;

    private final ChatRepository chatRepository;

    @Override
    public void processSentMessage(MessageTransferEntity messageTransferEntity) {
        ObjectId chatId = new ObjectId(messageTransferEntity.chatId());
        ObjectId messageId = new ObjectId(messageTransferEntity.messageId());
        UUID sharedId = chatRepository.findById(chatId)
                .orElseThrow(() ->
                        new ChatNotFoundException(CHAT_WITH_ID_NOT_FOUND
                                .formatted(messageTransferEntity.chatId())))
                .getSharedId();
        List<Chat> chatList = chatRepository.findBySharedId(sharedId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(MESSAGE_WITH_ID_NOT_FOUND));

        message.setStatus(MessageStatus.RECEIVED);
        chatList.forEach(chat -> {
            if(!chat.getId().equals(chatId)) {
                Message cloned = message.clone();
                cloned.setChatId(chat.getId());
                messageRepository.save(cloned);
                log.info("Message received by" + chat.getOwner());
            }
        });
        messageRepository.save(message);
    }
}
