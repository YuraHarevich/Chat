package ru.kharevich.chatservice.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.kharevich.chatservice.model.Chat;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ChatRepository extends MongoRepository<Chat, ObjectId> {

    Page<Chat> findAll(Pageable pageable);

    List<Chat> findByParticipants(Set<UUID> participants);

    List<Chat> findBySharedId(UUID sharedId);

    Optional<Chat> findBySharedIdAndOwner(UUID sharedId, UUID owner);

    Page<Chat> findByOwner(UUID owner, Pageable pageable);

}