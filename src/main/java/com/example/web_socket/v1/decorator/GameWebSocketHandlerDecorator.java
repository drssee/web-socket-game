package com.example.web_socket.v1.decorator;

import com.example.web_socket.v1.handler.GameWebSocketHandler;
import com.example.web_socket.service.GameService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class GameWebSocketHandlerDecorator extends GameWebSocketHandler {

    private final String ERROR_MSG = "INTERNAL_SERVER_ERROR";

    public GameWebSocketHandlerDecorator(GameService gameService) {
        super(gameService);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            super.handleTextMessage(session, message);
        } catch (Exception e) {
            session.close(new CloseStatus(CloseStatus.SERVER_ERROR.getCode(), ERROR_MSG));
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        int code = closeStatus.getCode();
        if (code == CloseStatus.NO_CLOSE_FRAME.getCode() || code == CloseStatus.SERVER_ERROR.getCode()) {
            closeStatus = closeStatus.withReason(ERROR_MSG);
            session.close(closeStatus);
        }
        super.afterConnectionClosed(session, closeStatus);
    }


}
