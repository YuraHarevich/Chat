package ru.kharevich.chatservice.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kharevich.chatservice.model.Chat;

public interface ChatRepository extends MongoRepository<Chat, ObjectId> {
    Page<Chat> findAll(Pageable pageable);
}