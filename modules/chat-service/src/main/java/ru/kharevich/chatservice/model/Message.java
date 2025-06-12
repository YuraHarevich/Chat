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
import java.util.HashSet;
import java.util.UUID;

@Document("messages")
@Getter
@Setter
@CompoundIndex(def = "{'chat_id': 1, 'sentTime': -1}") // Для быстрого поиска
public class Message implements Cloneable{

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

    @Field("shared_id")
    private UUID sharedId;

    @Override
    public Message clone() {
        try {
            Message cloned = (Message) super.clone();
            // Клонируем неизменяемые или копируемые по значению поля
            cloned.id = null; // ID должен быть null для нового документа
            cloned.content = this.content != null ? new String(this.content) : null;
            cloned.sentTime = this.sentTime != null ? LocalDateTime.from(this.sentTime) : null;
            cloned.sender = this.sender; // UUID — immutable
            cloned.status = this.status; // enum — immutable
            cloned.chatId = this.chatId; // ObjectId — mutable, можно клонировать
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning Message failed", e);
        }
    }


}
