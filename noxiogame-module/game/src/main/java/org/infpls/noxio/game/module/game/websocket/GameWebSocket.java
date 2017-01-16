package org.infpls.noxio.game.module.game.websocket;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class GameWebSocket extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocket) {
      try {
        
      }
      catch(Exception e) {
        System.err.println("Exception thrown in " + this.toString() + ":::afterConnectionEstablished");
        e.printStackTrace();
      }
    }

    @Override
    public void handleTextMessage(WebSocketSession webSocket, TextMessage data) {
      try {
        
      }
      catch(Exception e) {
        System.err.println("Exception thrown in " + this.toString() + ":::handleTextMessage");
        e.printStackTrace();
      }
    }
  
    @Override
    public void afterConnectionClosed(WebSocketSession webSocket, CloseStatus status) {
      try {
        
      }
      catch(Exception ex) {
        System.err.println("Exception thrown in " + this.toString() + ":::afterConnectionClosed");
        ex.printStackTrace();
      }
    }

}