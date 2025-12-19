package org.infpls.noxio.game.module.game.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.infpls.noxio.game.module.game.dao.DaoContainer;
import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.util.Oak;

import java.util.concurrent.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* @TODO: Apparently GSON will explode if you send bad data to a float/int/long.
   NaN in particular caused me to notice this.
   Please put in handlers. */

public class GameWebSocket extends TextWebSocketHandler {

    private final DaoContainer dao;
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> pingTasks = new ConcurrentHashMap<>();

    public GameWebSocket(DaoContainer dao, ScheduledExecutorService scheduler) {
        this.dao = dao;
        this.scheduler = scheduler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocket) {
      try {
        NoxioSession session = dao.getUserDao().createSession(webSocket, dao);
        session.start();
        webSocket.getAttributes().put("session", session);

        // Start sending WebSocket PING frames every 20 seconds to keep connection alive
        // This is critical for tunneling services (Cloudflare, playit.gg) that timeout idle connections
        ScheduledFuture<?> pingTask = scheduler.scheduleAtFixedRate(() -> {
          try {
            if (webSocket.isOpen()) {
              webSocket.sendMessage(new PingMessage());
              Oak.log(Oak.Type.SESSION, Oak.Level.INFO, "Sent WebSocket PING to keep connection alive: " + webSocket.getId());
            }
          } catch (Exception e) {
            Oak.log(Oak.Type.SESSION, Oak.Level.WARN, "Failed to send WebSocket PING: " + e.getMessage());
          }
        }, 20, 20, TimeUnit.SECONDS);

        pingTasks.put(webSocket.getId(), pingTask);
      }
      catch(Exception ex) {
        Oak.log(Oak.Type.SESSION, Oak.Level.ERR, "Exception thrown at Websocket top level.", ex);
      }
    }

    @Override
    public void handleTextMessage(WebSocketSession webSocket, TextMessage data) {
      try {
        NoxioSession session = (NoxioSession)(webSocket.getAttributes().get("session"));
        session.handlePacket(data.getPayload());
      }
      catch(Exception ex) {
        Oak.log(Oak.Type.SESSION, Oak.Level.ERR, "Exception thrown at Websocket top level.", ex);
      }
    }
  
    @Override
    public void handlePongMessage(WebSocketSession webSocket, PongMessage message) {
      // Client responded to our PING with a PONG - connection is alive
      Oak.log(Oak.Type.SESSION, Oak.Level.INFO, "Received WebSocket PONG from: " + webSocket.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocket, CloseStatus status) {
      try {
        // Stop sending PING frames for this connection
        ScheduledFuture<?> pingTask = pingTasks.remove(webSocket.getId());
        if (pingTask != null) {
          pingTask.cancel(false);
        }

        dao.getUserDao().destroySession(webSocket);
      }
      catch(Exception ex) {
        Oak.log(Oak.Type.SESSION, Oak.Level.ERR, "Exception thrown at Websocket top level.", ex);
      }
    }

}