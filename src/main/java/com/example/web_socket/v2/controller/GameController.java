package com.example.web_socket.v2.controller;

import com.example.web_socket.domain.GameRequest;
import com.example.web_socket.domain.GameResponse;
import com.example.web_socket.domain.enums.GameMenu;
import com.example.web_socket.domain.enums.GameStatus;
import com.example.web_socket.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.scheduling.annotation.Scheduled;
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

    // 테스트용
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // TODO 실제 live 인 세션들만 유지하도록 1분마다 확인 하도록 구현
    @Scheduled(fixedRate = 60000)
    public void test() {
        synchronized (sessions) {
            System.out.println("test");
            sessions.forEach(session -> {
                System.out.println(session);
                messagingTemplate.convertAndSend("/topic/test/" + session, "ping");
            });
        }
    }

    // init
    @MessageMapping("/init")
    @SendToUser("/queue/init")
    public GameResponse initGame(SimpMessageHeaderAccessor headerAccessor) {

        // 세션은 최대 2개까지, 그이상일 경우 초기화 후 예외 발생
        if (sessions.size() > 1) {
            sessions.clear();
            System.out.println("sessions clear - " + sessions.size());
            throw new RuntimeException("max 2, init session");
        }

        // 플레이어 초기화
//        String id = UUID.randomUUID().toString();
        String id = headerAccessor.getSessionId();
        System.out.println("init session id - " + id);
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
        gameResponse.setStatus(ready ? GameStatus.SUCCESS : GameStatus.FAIL);
        gameResponse.setMenu(GameMenu.READY);
        gameResponse.setPlayer(gameService.getPlayer(id));
        return gameResponse;
    }

    // isStart
    @MessageMapping("/isStart")
    @SendTo("/topic/isStart")
    public GameResponse isStart() {
        GameResponse gameResponse = new GameResponse();
        boolean isStart = gameService.isStart();
        gameResponse.setMenu(GameMenu.IS_START);
        gameResponse.setStatus(
                isStart ? GameStatus.SUCCESS : GameStatus.FAIL
        );

        return gameResponse;
    }

    // roll
}
