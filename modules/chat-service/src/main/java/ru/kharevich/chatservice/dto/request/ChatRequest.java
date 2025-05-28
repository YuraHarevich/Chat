package ru.kharevich.chatservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

import java.util.UUID;

public record ChatRequest(

        @NotNull(message = "Participants cannot be null")
        @Size(min = 2, message = "There must be at least 2 participants")
        Set<@NotNull(message = "Participant ID cannot be null") UUID> participants

) {
}
