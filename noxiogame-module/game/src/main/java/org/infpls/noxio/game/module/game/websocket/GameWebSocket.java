package org.infpls.noxio.game.module.game.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.infpls.noxio.game.module.game.dao.DaoContainer;
import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.util.Oak;

/* @TODO: Apparently GSON will explode if you send bad data to a float/int/long. 
   NaN in particular caused me to notice this. 
   Please put in handlers. */

public class GameWebSocket extends TextWebSocketHandler {

    @Autowired
    private DaoContainer dao;
  
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocket) {
      try {
        NoxioSession session = dao.getUserDao().createSession(webSocket, dao);
        session.start();
        webSocket.getAttributes().put("session", session);
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
    public void afterConnectionClosed(WebSocketSession webSocket, CloseStatus status) {
      try {
        dao.getUserDao().destroySession(webSocket);
      }
      catch(Exception ex) {
        Oak.log(Oak.Type.SESSION, Oak.Level.ERR, "Exception thrown at Websocket top level.", ex);
      }
    }

}