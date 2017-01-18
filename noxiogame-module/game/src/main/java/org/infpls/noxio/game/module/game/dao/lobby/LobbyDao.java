package org.infpls.noxio.game.module.game.dao.lobby;

import java.util.*;

/* @FIXME
   LobbyDao do thing and do
*/

public class LobbyDao {
  private final List<GameLobby> lobbies; /* This is a list of all active user NoxioSessions. */
  
  public LobbyDao() {
    lobbies = new ArrayList();
    lobbies.add(new GameLobby("Test Game 1", false));
    lobbies.add(new GameLobby("Test Game 2", false));
  }
  
  public GameLobby createLobby(final String name) {
    GameLobby lobby = new GameLobby(name, true);
    lobbies.add(lobby);
    return lobby;
  }
  
  public List<GameLobbyInfo> getLobbyInfo() {
    List<GameLobbyInfo> info = new ArrayList();
    for(int i=0;i<lobbies.size();i++) {
      info.add(lobbies.get(i).getInfo());
    }
    return info;
  }
  
  public GameLobby getLobby(final String lid) {
    for(int i=0;i<lobbies.size();i++) {
      if(lobbies.get(i).getLid().equals(lid)) {
        return lobbies.get(i);
      }
    }
    return null;
  }
}
