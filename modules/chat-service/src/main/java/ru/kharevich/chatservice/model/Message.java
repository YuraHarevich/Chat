package ru.kharevich.chatservice.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

@Document("messages")
@Getter
@Setter
@CompoundIndex(def = "{'chat_id': 1, 'sentTime': -1}") // Для быстрого поиска
public class Message {

    @Id
    private ObjectId id;

    @Field("content")
    private String content;

    @Field("sentTime")
    @CreatedDate
    private LocalDateTime sentTime;

    @Field("sender")
    private UUID sender;

    @Field("status")
    private MessageStatus status;

    @Field("chat_id")
    private ObjectId chatId;

}
