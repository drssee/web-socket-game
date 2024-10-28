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

        // 세션은 최대 2개까지, 그이상일 경우 초기화 후 예외 발생
        if (sessions.size() > 1) {
            sessions.forEach(sessions::remove);
            throw new RuntimeException("max 2, init session");
        }

        // 플레이어 초기화
        String id = UUID.randomUUID().toString();
        sessions.add(id);
        System.out.println("sessions.size() = " + sessions.size());
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
        System.out.println("sessions.size() = " + sessions.size());
        gameService.removePlayer(id);
    }

    // ready
    @MessageMapping("/ready")
    @SendToUser("/queue/ready")
    public GameResponse ready(String id) {

        GameResponse gameResponse = new GameResponse();
        boolean ready = gameService.setReady(id); // 준비
        boolean isStart = gameService.isStart(); // 모든 세션이 준비 되었는지 확인
        gameResponse.setPlayer(gameService.getPlayer(id));

        if (isStart) { // 모든 세션이 준비 되었으면 시작상태 리턴
            gameResponse.setStatus(GameStatus.SUCCESS);
            gameResponse.setMenu(GameMenu.IS_START);
            gameService.changeTurn(); // 시작하면서 턴 부여
            // isStart로 별도의 @SendTo를 모든 세션에 해주기

        } else { // 그외의 경우 준비된 세션만 준비 상태 리턴
            gameResponse.setStatus(ready ? GameStatus.SUCCESS : GameStatus.FAIL);
            gameResponse.setMenu(GameMenu.READY);
        }
        return gameResponse;
    }

    // isStart
    @MessageMapping("/isStart")
    @SendTo("/topic/isStart")
    public boolean isStart() {
        return gameService.isStart();
    }

    // roll
}
