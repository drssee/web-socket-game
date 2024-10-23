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
        if (sessions.size() != 2) {
            throw new RuntimeException("need 2");
        }
        String payload = message.getPayload();
        GameRequest gameRequest = objectMapper.readValue(payload, GameRequest.class);

        GameResponse response = new GameResponse();
        String command = gameRequest.getCommand();

        switch (command) {
            case "ready":
                boolean ready = gameService.setReady(session.getId()); // 준비
                boolean isStart = gameService.isStart(); // 모든 세션이 준비 되었는지 확인

                if (isStart) { // 모든 세션이 준비 되었으면 시작상태 리턴
                    command = "isStart";
                    response.setStatus(GameStatus.SUCCESS);
                    response.setMenu(GameMenu.IS_START);
                    gameService.changeTurn(); // 시작하면서 턴 부여

                } else { // 그외의 경우 준비된 세션만 준비 상태 리턴
                    response.setStatus(ready ? GameStatus.SUCCESS : GameStatus.FAIL);
                    response.setMenu(GameMenu.READY);
                }

                break;

            case "roll":
                // 주사위 숫자로 게임 프로세스 실행
                int roll = gameService.setRoll(session.getId());
                int curBoardNum = gameRequest.getBoard().getId();
                response = gameService.process(session.getId(), curBoardNum, roll);

                // 프로세스 후 게임오버일 경우
                if (response.isGameOver()) {
                    response.setStatus(GameStatus.SUCCESS);
                    response.setMenu(GameMenu.IS_GAME_OVER);
                } else {
                    response.setStatus(GameStatus.SUCCESS);
                    response.setMenu(GameMenu.PROCESS);
                    gameService.changeTurn();
                }

                break;
        }

        // 단일 세션 응답이 필요한 경우
        if (command.equals("ready")) {
            if (session.isOpen()) {
                synchronized (session) {
                    response.setPlayer(gameService.getPlayer(session.getId()));
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                }
            }
        }
        // 연결된 모든 사용자에게 실행된 내용 전달이 필요한 경우
        else {
            for (WebSocketSession webSocketSession : sessions) {
                if (webSocketSession.isOpen()) {
                    synchronized (webSocketSession) {
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
