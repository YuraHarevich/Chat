package ru.kharevich.chatservice.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Document("chats")
@Getter
@Setter
public class Chat implements Cloneable {

    @Id
    private ObjectId id;

    @Field("shared_id")
    private UUID sharedId;

    @Field("participants")
    private Set<UUID> participants;

    @Field("owner")
    private UUID owner;

    @Field("creation_time")
    @CreatedDate
    private LocalDateTime creationTime;

    @Override
    public Chat clone() {
        try {
            Chat cloned = (Chat) super.clone();
            cloned.setId(null);
            cloned.participants = new HashSet<>(this.participants);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}