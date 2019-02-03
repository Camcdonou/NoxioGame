package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.util.Oak;
import org.infpls.noxio.game.module.game.util.Scung;

public class LobbyDao {
  private HttpThread httpToAuth;
  private final List<GameLobby> lobbies; /* This is a list of all active user NoxioSessions. */
  
  public LobbyDao() {
    lobbies = new ArrayList();
    httpToAuth = new HttpThread();
    httpToAuth.start();
    
    try {
      List<LobbySettings> lss = LobbySettings.parseMultipleSettings(Scung.readFile("lobby.defaults"));
      for(int i=0;i<lss.size();i++) {
        final OfficialLobby lob = new OfficialLobby(httpToAuth, lss.get(i));
        lobbies.add(lob);
        lob.start();
      }
    }
    catch(NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException | IOException ex) {
      Oak.log(Oak.Level.CRIT, "Error during setup of default lobbies.", ex);
    }
  }
  
  public GameLobby createLobby(final LobbySettings ls) throws IOException {
    GameLobby lobby = new CustomLobby(httpToAuth, ls);
    lobbies.add(lobby);
    lobby.start();
    return lobby;
  }
  
  public List<GameLobbyInfo> getLobbyInfo() {
    cleanUp();
    List<GameLobbyInfo> info = new ArrayList();
    for(int i=0;i<lobbies.size();i++) {
      info.add(lobbies.get(i).getInfo());
    }
    return info;
  }
  
  public GameLobby getLobby(final String lid) {
    cleanUp();
    for(int i=0;i<lobbies.size();i++) {
      if(lobbies.get(i).getLid().equals(lid)) {
        return lobbies.get(i);
      }
    }
    return null;
  }
  
  public GameLobby getLobbyAuto() {
    cleanUp();
    GameLobby best = null;
    for(int i=0;i<lobbies.size();i++) {
      GameLobby gl = lobbies.get(i);
      if(best == null) { best = gl; continue; }
      if(gl.players.size() > best.players.size() && gl.players.size() < gl.maxPlayers) { best = gl; }
    }
    return best;
  }
 
  /* This method deletes any user created lobbies that are flagged as closed. */
  public void cleanUp() { 
    for(int i=0;i<lobbies.size();i++) {
      if(lobbies.get(i).isClosed()) {
        lobbies.remove(i);
      }
    }
  }

  public List<String> getOnlineLobbyList() {
    final List<String> loblist = new ArrayList();
    for(int i=0;i<lobbies.size();i++) {
      final GameLobby lob = lobbies.get(i);
      final StringBuilder sb = new StringBuilder();
      sb.append(lob.name); sb.append(" :: ");
      sb.append(lob.lid); sb.append(" :: ");
      sb.append(lob.players.size());  sb.append("/"); sb.append(lob.maxPlayers);
      sb.append("</br>");
      for(int j=0;j<lob.players.size();j++) {
        sb.append(lob.players.get(j).getUser()); sb.append(" ");
      }
      loblist.add(sb.toString());
    }
    return loblist;
  }

  public void destroy() {
    try {
      for(int i=0;i<lobbies.size();i++) {
        lobbies.get(i).close("Game server is shutting down...");
      }
      httpToAuth.close();
    }
    catch(IOException ex) {
      Oak.log(Oak.Level.ERR, "Error during server shutdown.", ex);
    }
  }
}
