package com.example.web_socket.v2.controller;

import com.example.web_socket.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    private final GameService gameService;
    private final ObjectMapper objectMapper;

    public GameController(GameService gameService) {
        this.gameService = gameService;
        this.objectMapper = new ObjectMapper();
    }

    // TODO 2. handleTextMessage 역할을 하도록 해야함

    @MessageMapping("/test")
    @SendTo("/topic/test")
    public String handleTest(String s) {
        return "return: " + s;
    }

    // ready
//    @MessageMapping("/ready")

    // roll
}
