package com.example.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private UUID id;

    @NotBlank(message = "Message content is required")
    private String content;

    @Builder.Default
    private String messageType = "TEXT"; // TEXT, IMAGE, FILE

    private String attachmentUrl;

    private UUID senderId;

    private String senderType;

    private Boolean isRead;

    private LocalDateTime createdAt;
}
