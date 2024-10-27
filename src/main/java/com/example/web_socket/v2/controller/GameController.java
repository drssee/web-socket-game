package com.example.web_socket.v2.controller;

import com.example.web_socket.domain.GameRequest;
import com.example.web_socket.domain.GameResponse;
import com.example.web_socket.domain.enums.GameMenu;
import com.example.web_socket.domain.enums.GameStatus;
import com.example.web_socket.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Controller
public class GameController {

    private final Set<String> sessions = Collections.synchronizedSet(new HashSet<>());

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // init
    @MessageMapping("/init")
    @SendToUser("/queue/init")
    public GameResponse initGame() {

        // 플레이어 초기화
        String id = UUID.randomUUID().toString();
        sessions.add(id);
        gameService.addPlayer(id);

        // 응답 객체 생성
        GameResponse gameResponse = new GameResponse();
        gameResponse.setMenu(GameMenu.INIT);
        gameResponse.setStatus(GameStatus.SUCCESS);
        gameResponse.setPlayer(gameService.getPlayer(id));
        gameResponse.setBoards(gameService.getBoards());

        return gameResponse;
    }

    // disconnect
    @MessageMapping("/disconnect")
    @SendToUser("/queue/disconnect")
    public void disconnectGame(String id) {
        sessions.remove(id);
        gameService.removePlayer(id);
    }

    // ready
    @MessageMapping("/ready")
    @SendTo("/topic/ready")
    public GameResponse ready(GameRequest gameRequest) {
        return null;
    }

    // roll
}
