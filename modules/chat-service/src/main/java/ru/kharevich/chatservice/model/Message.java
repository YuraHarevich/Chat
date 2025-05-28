package ru.kharevich.chatservice.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

@Document("messages")
@Getter
@Setter
public class Message {

    @Id
    private ObjectId id;

    @Field("content")
    private String content;

    @Field("timestamp")
    @CreatedDate
    private LocalDateTime timestamp;

    @Field("sender")
    private UUID sender;

    @Field("receiver")
    private UUID receiver;

    @Field("status")
    private MessageStatus status;

    @Field("chat_id")
    @DBRef(db = "chats")
    private ObjectId chatId;

}
