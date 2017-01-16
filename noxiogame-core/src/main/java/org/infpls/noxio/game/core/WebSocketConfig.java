package org.infpls.noxio.game.core;

import org.infpls.noxio.game.module.game.websocket.GameWebSocket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.WebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(GameWebSocketHandler(), "/game");
    }

    @Bean
    public WebSocketHandler GameWebSocketHandler() {
        return new GameWebSocket();
    }

}