package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;
import java.util.*;

public class LobbyDao {
  private final List<GameLobby> lobbies; /* This is a list of all active user NoxioSessions. */
  
  public LobbyDao() {
    lobbies = new ArrayList();
    try {
      final GameSettings a, b, c, e, f, g;
      a = new GameSettings(); b = new GameSettings(); c = new GameSettings();
      e = new GameSettings(); f = new GameSettings(); g = new GameSettings();
      
      a.set("game_name", "DM Test #1");
      a.set("map_name", "final");
      a.set("gametype", "Deathmatch");
      a.set("max_players", "4");
      a.set("score_to_win", "1");
      a.set("respawn_time", "30");
      
      b.set("game_name", "TDM Test #1");
      b.set("map_name", "final");
      b.set("gametype", "TeamDeathmatch");
      b.set("max_players", "6");
      b.set("score_to_win", "25");
      b.set("respawn_time", "90");
      
      c.set("game_name", "CTF Test #1");
      c.set("map_name", "battle");
      c.set("gametype", "CaptureTheFlag");
      c.set("max_players", "8");
      c.set("score_to_win", "3");
      c.set("respawn_time", "90");
      
      e.set("game_name", "DM Test #2");
      e.set("map_name", "final");
      e.set("gametype", "Deathmatch");
      e.set("max_players", "16");
      e.set("score_to_win", "15");
      e.set("respawn_time", "30");
      
      f.set("game_name", "TDM Test #2");
      f.set("map_name", "battle");
      f.set("gametype", "TeamDeathmatch");
      f.set("max_players", "12");
      f.set("score_to_win", "50");
      f.set("respawn_time", "90");
      
      g.set("game_name", "CTF Test #2");
      g.set("map_name", "battle");
      g.set("gametype", "CaptureTheFlag");
      g.set("max_players", "24");
      g.set("score_to_win", "5");
      g.set("respawn_time", "150");
      
      
      lobbies.add(new OfficialLobby(a)); lobbies.add(new OfficialLobby(b)); lobbies.add(new OfficialLobby(c));
      lobbies.add(new OfficialLobby(e)); lobbies.add(new OfficialLobby(f)); lobbies.add(new OfficialLobby(g));
    }
    catch(IOException ex) {
      ex.printStackTrace();
    }
  }
  
  public GameLobby createLobby(final String name) throws IOException {
    final GameSettings settings = new GameSettings();
    settings.set("game_name", name);
    GameLobby lobby = new CustomLobby(settings);
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
