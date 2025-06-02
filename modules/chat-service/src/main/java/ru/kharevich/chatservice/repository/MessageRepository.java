package ru.kharevich.chatservice.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kharevich.chatservice.model.Message;

import java.util.UUID;

public interface MessageRepository extends MongoRepository<Message, ObjectId> {
    Page<Message> findByChatIdOrderBySentTimeDesc(ObjectId chatId, Pageable pageable);
}
