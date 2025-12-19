package org.infpls.noxio.game.core;

import org.infpls.noxio.game.module.game.websocket.GameWebSocket;
import org.infpls.noxio.game.module.game.dao.DaoContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.WebSocketHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private DaoContainer dao;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(GameWebSocketHandler(), "/game").setAllowedOrigins("*");
    }

    @Bean
    public ScheduledExecutorService webSocketPingScheduler() {
        return Executors.newScheduledThreadPool(2);
    }

    @Bean
    public WebSocketHandler GameWebSocketHandler() {
        return new GameWebSocket(dao, webSocketPingScheduler());
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // Set max session idle timeout to 30 minutes (in milliseconds)
        container.setMaxSessionIdleTimeout(1800000L);
        // Set max text message buffer size
        container.setMaxTextMessageBufferSize(8192);
        // Set max binary message buffer size
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }

}