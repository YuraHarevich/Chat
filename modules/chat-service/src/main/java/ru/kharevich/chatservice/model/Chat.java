package ru.kharevich.chatservice.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

@Document("chats")
@Getter
@Setter
public class Chat {

    @Id
    private ObjectId id;

    @Field("first_person")
    private UUID firstPerson;

    @Field("second_person")
    private UUID secondPerson;

    @Field("creation_time")
    @CreatedDate
    private LocalDateTime creationTime;

}
