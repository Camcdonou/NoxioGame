package org.infpls.noxio.game.module.game.session;

import java.io.*;
import com.google.gson.*;
import org.springframework.web.socket.*;

import org.infpls.noxio.game.module.game.dao.DaoContainer;
import org.infpls.noxio.game.module.game.session.login.Login;
import org.infpls.noxio.game.module.game.session.error.*;
import org.infpls.noxio.game.module.game.session.lobby.Lobby;

public class NoxioSession {
  private final WebSocketSession webSocket;
  private final DaoContainer dao;
  
  private String user, sid;
  private SessionState sessionState;
 
  public NoxioSession(final WebSocketSession webSocket, final DaoContainer dao) throws IOException {
    this.webSocket = webSocket;
    this.dao = dao;
        
    sessionState = new Login(this, dao.getUserDao(), dao.getServerInfoDao());
  }
  
  public void handlePacket(final String data) throws IOException {
    sessionState.handlePacket(data);
  }
  
  public void sendPacket(final Packet p) throws IOException {
    final Gson gson = new GsonBuilder().create();
    webSocket.sendMessage(new TextMessage(gson.toJson(p)));
  }
  
  /* State info
    00 - Login State
    01 - Lobby State
  */
  public void changeState(final int s) throws IOException { /* Not a huge fan of how this works */
    sessionState.destroy();
    switch(s) {
      case 1 : { sessionState = new Lobby(this); break; }
      default : { close(); break; } //NO.
    }
  }
  
  public void login(final String user, final String sid) throws IOException {
    if(loggedIn()) { throw new IOException("This session is already logged in!"); }
    this.user = user;
    this.sid = sid;
    changeState(1);
  }
  
  public boolean loggedIn() {
    return user != null;
  }
  
  public String getUser() {
    return user;
  }
  
  public String getSessionId() {
    return webSocket.getId();
  }
  
  public void destroy() throws IOException {
    sessionState.destroy();
  }
  
  /* Normal connection close */
  public void close() throws IOException {
    webSocket.close();
  }
  
  /* Error connection close */
  public void close(final String message) throws IOException {
    sendPacket(new PacketX00(message));
    webSocket.close(CloseStatus.NOT_ACCEPTABLE);
  }
  
  /* Exception connection close */
  public void close(final Exception ex) throws IOException {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    ex.printStackTrace(pw);
    sendPacket(new PacketX01(ex.getMessage(), sw.toString()));
    webSocket.close(CloseStatus.NOT_ACCEPTABLE);
  }
}
