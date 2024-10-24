package com.example.web_socket.config;

import com.example.web_socket.decorator.GameWebSocketHandlerDecorator;
import com.example.web_socket.game.service.GameService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameService gameService;

    public WebSocketConfig(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new GameWebSocketHandlerDecorator(gameService), "/game")
                .setAllowedOrigins("*"); // CORS 설정
    }
}
