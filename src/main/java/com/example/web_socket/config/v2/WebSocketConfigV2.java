package com.example.web_socket.config.v2;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfigV2 implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //클라이언트의 구독 경로 등록
        registry.enableSimpleBroker("/topic", "/queue");
        //클라이언트의 전송 경로
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Vue의 SockJS가 연결될 엔드포인트
        registry.addEndpoint("/game/v2")
                .setAllowedOrigins("http://localhost:8081") // 허용하는 Origin
                .withSockJS();
    }
}
