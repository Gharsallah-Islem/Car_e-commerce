package com.example.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    @NotBlank(message = "Message content is required")
    private String content;

    private String messageType = "TEXT"; // TEXT, IMAGE, FILE

    private String attachmentUrl;
}
