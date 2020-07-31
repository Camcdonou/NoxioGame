package org.infpls.noxio.game.module.game.session;
import java.io.*;
import com.google.gson.*;
import org.springframework.web.socket.*;

 
import org.infpls.noxio.game.module.game.dao.DaoContainer;
import org.infpls.noxio.game.module.game.dao.lobby.*;
import org.infpls.noxio.game.module.game.dao.user.*;
import org.infpls.noxio.game.module.game.session.error.*;
import org.infpls.noxio.game.module.game.session.login.Login;
import org.infpls.noxio.game.module.game.session.lobby.Lobby;
import org.infpls.noxio.game.module.game.session.ingame.InGame;
import org.infpls.noxio.game.module.game.util.Oak;

public final class NoxioSession {
  private final WebSocketSession webSocket;
  private final DaoContainer dao;
  
  private UserData user;
  private String sid;
  private final SessionThread sessionThread;
  private SessionState sessionState;
 
  public NoxioSession(final WebSocketSession webSocket, final DaoContainer dao) throws IOException {
    this.webSocket = webSocket;
    this.dao = dao;

    sessionThread = new SessionThread(this);
    
    changeState("l");
  }
  
  public void start() {
    sessionThread.start();
  }
  
  public void handlePacket(final String data) throws IOException {
    sessionState.handlePacket(data);
  }
  
  public void sendPacket(final Packet p) {
    sessionThread.push(p);
  }
  
  /* Sends data over websocket on immiediate thread. Should only be called by this.sessionThread.run() */
  public void sendImmiediate(final Packet p) throws IOException, IllegalStateException {
    if(isOpen()) {
      final Gson gson = new GsonBuilder().create();
      webSocket.sendMessage(new TextMessage(gson.toJson(p)));
    }
  }
  
  private void changeState(final String id) throws IOException { changeState(id, null); }
  private void changeState(final String id, final Object generic) throws IOException {
    if(sessionState != null) { sessionState.destroy(); }
    switch(id) { 
      case "l" : { sessionState = new Login(this); break; }
      case "b" : { sessionState = new Lobby(this, dao.getLobbyDao()); break; }
      case "g" : { sessionState = new InGame(this, (GameLobby)generic); break; }
      default : throw new IOException("Invalid State Exception. What the fuck are you doing? @" + user);
    }
  }
  
  public void leaveGame() throws IOException {
    if(sessionState instanceof InGame) { changeState("b"); }
  }
  
  public void joinGame(final GameLobby gl) throws IOException {
    if(!loggedIn()) { throw new IOException("This session is not logged in!"); }
    changeState("g", gl);
  }
  
  public void login(final UserData user, final String sid) throws IOException {
    if(loggedIn()) { throw new IOException("This session is already logged in!"); }
    this.user = user;
    this.sid = sid;
    changeState("b");
  }
  
  public boolean loggedIn() {
    return user != null && isOpen();
  }
  
  public String getDisplay() {
    return user.display;
  }
  
  public String getUser() {
    return user.name;
  }
  
  public UserData getUserData() {
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
    sessionThread.close();
    sessionState.destroy();
  }
  
  /* Normal connection close */
  public void close() throws IOException {
    try { sessionThread.close(); } catch(Exception ex) {
      Oak.log(Oak.Type.SESSION, Oak.Level.CRIT, "Sessionthread.close() threw exception to NoxioSession!", ex);
    }
    webSocket.close();
  }
  
  /* Error connection close */
  public void close(final String message) throws IOException {
    try {
      Oak.log(Oak.Type.SESSION, Oak.Level.WARN, "Connection closed for user: '" + (loggedIn()?getUser():"Not Logged In") + "' with message: " + message);
      sessionThread.close();
      if(sessionThread.blockingWaitForClose()) { sendImmiediate(new PacketX00(message)); }
    } catch(Exception ex) {
      Oak.log(Oak.Type.SESSION, Oak.Level.CRIT, "Sessionthread.close() threw exception to NoxioSession!", ex);
    }
    webSocket.close(CloseStatus.NOT_ACCEPTABLE);
  }
  
  /* Exception connection close */
  public void close(final Exception ex) throws IOException {
    Oak.log(Oak.Type.SESSION, Oak.Level.WARN, "Connection closed for user: '" + (loggedIn()?getUser():"Not Logged In") + "' with Exception: ", ex);
    try {
      sessionThread.close();
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      ex.printStackTrace(pw);
      if(sessionThread.blockingWaitForClose()) { sendImmiediate(new PacketX01(ex.getMessage(), sw.toString())); }
    } catch(Exception ex2) {
      Oak.log(Oak.Type.SESSION, Oak.Level.CRIT, "Sessionthread.close() threw exception to NoxioSession!", ex2);
    }
    webSocket.close(CloseStatus.NOT_ACCEPTABLE);
  }
}
