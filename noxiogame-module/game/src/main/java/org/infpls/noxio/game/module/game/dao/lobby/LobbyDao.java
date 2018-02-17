package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;
import java.util.*;
import org.infpls.noxio.game.module.game.util.Scung;

public class LobbyDao {
  private final List<GameLobby> lobbies; /* This is a list of all active user NoxioSessions. */
  
  public LobbyDao() {
    lobbies = new ArrayList();
    try {
      List<LobbySettings> lss = LobbySettings.parseMultipleSettings(Scung.readFile("lobby.defaults"));
      for(int i=0;i<lss.size();i++) {
        final OfficialLobby lob = new OfficialLobby(lss.get(i));
        lobbies.add(lob);
        lob.start();
      }
    }
    catch(IOException ex) {
      ex.printStackTrace();
    }
  }
  
  public GameLobby createLobby(final String name) throws IOException {
    final LobbySettings settings = new LobbySettings();
    settings.set("game_name", name);
    GameLobby lobby = new CustomLobby(settings);
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
}
