package com.example.web_socket.game.handler;

import com.example.web_socket.game.domain.GameRequest;
import com.example.web_socket.game.domain.GameResponse;
import com.example.web_socket.game.domain.enums.GameMenu;
import com.example.web_socket.game.domain.enums.GameStatus;
import com.example.web_socket.game.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final GameService gameService;
    private final ObjectMapper objectMapper;

    public GameWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
        this.objectMapper = new ObjectMapper();
    }

    private Set<WebSocketSession> sessions = Collections.synchronizedSet(new LinkedHashSet<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (sessions.size() > 1) throw new RuntimeException("max 2");
        System.out.println("session established - " + session.getId());
        initPlayer(session);
    }

    private void initPlayer(WebSocketSession session) {
        // 초기화된 세션에게 별도의 응답 메시지를 전달함
        sessions.add(session);
        gameService.addPlayer(session.getId());
        GameResponse response = new GameResponse();
        response.setMenu(GameMenu.INIT);
        response.setStatus(GameStatus.SUCCESS);
        response.setPlayer(gameService.getPlayer(session.getId()));
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        GameRequest gameRequest = objectMapper.readValue(payload, GameRequest.class);

        GameResponse response = new GameResponse();

        switch (gameRequest.getCommand()) {
            case "ready":
                boolean ready = gameService.ready(session.getId());
                response.setStatus(ready ? GameStatus.SUCCESS : GameStatus.FAIL);
                response.setMenu(GameMenu.READY);
                break;
            case "isStart":
                boolean start = gameService.isStart();
                response.setStatus(start ? GameStatus.SUCCESS : GameStatus.FAIL);
                response.setMenu(GameMenu.IS_START);
                break;
            case "changeTurn":
                gameService.changeTurn();
                response.setStatus(GameStatus.SUCCESS);
                response.setMenu(GameMenu.CHANGE_TURN);
                break;
            case "roll":
                int roll = gameService.roll(session.getId());
                response.setStatus(1 <= roll && roll <= 6 ? GameStatus.SUCCESS : GameStatus.FAIL);
                response.setMenu(GameMenu.ROLL);
                break;
            case "handle":
                response = gameService.handle(session.getId(), gameRequest.getBoard().getId());
                response.setStatus(GameStatus.SUCCESS);
                response.setMenu(GameMenu.HANDLE);
                break;
            case "isGameOver":
                gameService.isGameOver(session.getId(), gameRequest.getBoard().getId());
                response.setStatus(GameStatus.SUCCESS);
                response.setMenu(GameMenu.IS_GAME_OVER);
                break;
        }

        // 연결된 모든 사용자에게 실행된 내용 전달
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen()) {
                synchronized (webSocketSession) {
                    if (webSocketSession.isOpen()) {
                        // 응답에 각 세션에 해당하는 플레이어 주입해줌
                        response.setPlayer(gameService.getPlayer(webSocketSession.getId()));
                        webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("status: " + status);
        sessions.remove(session);
        gameService.removePlayer(session.getId());
    }
}
