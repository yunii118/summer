package org.choongang.chatting.controllers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestChatRoom {
    @NotBlank
    private String roomId;

    @NotBlank
    private String roomNm;

    @Min(2)
    private int capacity = 2;
}
