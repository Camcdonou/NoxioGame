package org.infpls.noxio.game.module.game.session;

import java.io.*;
import com.google.gson.*;
import org.springframework.web.socket.*;

import org.infpls.noxio.game.module.game.dao.DaoContainer;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.session.error.*;
import org.infpls.noxio.game.module.game.session.login.Login;
import org.infpls.noxio.game.module.game.session.lobby.Lobby;
import org.infpls.noxio.game.module.game.session.ingame.InGame;

public final class NoxioSession {
  private final WebSocketSession webSocket;
  private final DaoContainer dao;
  
  private String user, sid;
  private SessionState sessionState;
 
  public NoxioSession(final WebSocketSession webSocket, final DaoContainer dao) throws IOException {
    this.webSocket = webSocket;
    this.dao = dao;
        
    changeState("l");
  }
  
  public void handlePacket(final String data) throws IOException {
    sessionState.handlePacket(data);
  }
  
  public void sendPacket(final Packet p) throws IOException {
    /* Due to the multi threaded nature of this application, it's possible a packet can attempt to be sent right as teh connection closes.
       This isOpen() is just a small prevention measure to stop lots of pointless exceptions. */
    try {
      if(isOpen()) {
        final Gson gson = new GsonBuilder().create();
        webSocket.sendMessage(new TextMessage(gson.toJson(p)));
      }
    }
    catch(IllegalStateException | IOException iex) {
      System.err.println("User closed connection during packet sending: " + p.getType());
      System.err.println("Propogating exception up!");
      iex.printStackTrace();
      throw iex;
    }
  }
  
  private void changeState(final String id) throws IOException { changeState(id, null); }
  private void changeState(final String id, final Object generic) throws IOException {
    if(sessionState != null) { sessionState.destroy(); }
    switch(id) { 
      case "l" : { sessionState = new Login(this, dao.getUserDao(), dao.getInfoDao()); break; }
      case "b" : { sessionState = new Lobby(this, dao.getLobbyDao()); break; }
      case "g" : { sessionState = new InGame(this, (GameLobby)generic); break; }
      default : throw new IOException("Invalid State Exception. What the fuck are you doing? @" + user);
    }
  }
  
  public void leaveGame() throws IOException {
    changeState("b");
  }
  
  public void joinGame(final GameLobby gl) throws IOException {
    if(!loggedIn()) { throw new IOException("This session is not logged in!"); }
    changeState("g", gl);
  }
  
  public void login(final String user, final String sid) throws IOException {
    if(loggedIn()) { throw new IOException("This session is already logged in!"); }
    this.user = user;
    this.sid = sid;
    changeState("b");
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
  
  public String getWebSocketId() {
    return webSocket.getId();
  }
  
  public boolean isOpen() { 
    return webSocket.isOpen();
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
