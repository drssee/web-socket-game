package com.example.web_socket.v2.controller;

import com.example.web_socket.domain.GameResponse;
import com.example.web_socket.domain.enums.GameMenu;
import com.example.web_socket.domain.enums.GameStatus;
import com.example.web_socket.service.GameService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class GameController {

    private static final Logger logger = LogManager.getLogger(GameController.class);

    private final Set<String> sessions = Collections.synchronizedSet(new HashSet<>());
    // 보류중인 세션들 저장
    private final Set<String> pendingSessions = Collections.synchronizedSet(new HashSet<>());

    private final GameService gameService;

    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    // 실제 live 인 세션들만 유지하도록 1분마다 확인
    @Scheduled(fixedRate = 60000)
    public void checkSessions() {
        logger.info("checkSessions()");
        logger.info("session.size() = {}", sessions.size());

        // pendingSessions 초기화
        pendingSessions.clear();
        pendingSessions.addAll(sessions);

        sessions.forEach(session -> {
            logger.info("session - {}", session);
            messagingTemplate.convertAndSend("/topic/ping/" + session, session);
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("pendingSession.size() = {}", pendingSessions.size());
                pendingSessions.forEach(pendingSession -> {
                    logger.info("pendingSession - {}", pendingSession);
                    sessions.remove(pendingSession);
                    logger.info("sessions.size() = {}", sessions.size());
                    gameService.removePlayer(pendingSession);
                });
                pendingSessions.clear();
            }
        }, 3000);
    }

    // pong
    @MessageMapping("/pong")
    public void receivePong(String session) {
        // 응답이 확인된 세션 제거
        pendingSessions.remove(session);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public String error(Exception e) {
        logger.error(e);
        return e.getMessage();
    }

    // init
    @MessageMapping("/init")
    @SendToUser("/queue/init")
    public GameResponse initGame(SimpMessageHeaderAccessor headerAccessor) {

        // 세션은 최대 2개까지, 그이상일 경우 예외 발생
        if (sessions.size() > 1) {
            throw new RuntimeException("max 2, init session");
        }

        // 플레이어 초기화
        String id = headerAccessor.getSessionId();
        messagingTemplate.convertAndSendToUser(id, "/queue/test", "zz");
        logger.info("init session id - {}", id);
        sessions.add(id);
        logger.info("session.size() = {}", sessions.size());
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
        logger.info("session.size() = {}", sessions.size());
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
