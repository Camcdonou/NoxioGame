package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;
import java.util.*;

public class LobbyDao {
  private final List<GameLobby> lobbies; /* This is a list of all active user NoxioSessions. */
  
  public LobbyDao() {
    lobbies = new ArrayList();
    try {
      lobbies.add(new OfficialLobby("Test Game 1"));
      lobbies.add(new OfficialLobby("Test Game 2"));
    }
    catch(IOException ex) {
      ex.printStackTrace();
    }
  }
  
  public GameLobby createLobby(final String name) throws IOException {
    GameLobby lobby = new CustomLobby(name);
    lobbies.add(lobby);
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
}
