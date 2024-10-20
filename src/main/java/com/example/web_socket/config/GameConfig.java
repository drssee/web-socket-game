package com.example.web_socket.config;

import com.example.web_socket.game.service.GameService;
import com.example.web_socket.game.service.SimpleGameServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfig {

    @Bean
    public GameService gameService() {
        return new SimpleGameServiceImpl();
    }
}
