package org.infpls.noxio.game.module.game.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.infpls.noxio.game.module.game.dao.DaoContainer;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class GameWebSocket extends TextWebSocketHandler {

    @Autowired
    private DaoContainer dao;
  
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocket) {
      try {
        NoxioSession session = dao.getUserDao().createSession(webSocket, dao);
        webSocket.getAttributes().put("session", session);
      }
      catch(Exception e) {
        System.err.println("Exception thrown in " + this.toString() + ":::afterConnectionEstablished");
        e.printStackTrace();
      }
    }

    @Override
    public void handleTextMessage(WebSocketSession webSocket, TextMessage data) {
      try {
        NoxioSession session = (NoxioSession)(webSocket.getAttributes().get("session"));
        session.handlePacket(data.getPayload());
      }
      catch(Exception e) {
        System.err.println("Exception thrown in " + this.toString() + ":::handleTextMessage");
        e.printStackTrace();
      }
    }
  
    @Override
    public void afterConnectionClosed(WebSocketSession webSocket, CloseStatus status) {
      try {
        dao.getUserDao().destroySession(webSocket);
      }
      catch(Exception ex) {
        System.err.println("Exception thrown in " + this.toString() + ":::afterConnectionClosed");
        ex.printStackTrace();
      }
    }

}