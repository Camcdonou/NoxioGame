package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;
import java.util.*;

public class LobbyDao {
  private final List<GameLobby> lobbies; /* This is a list of all active user NoxioSessions. */
  
  public LobbyDao() {
    lobbies = new ArrayList();
    try {
      final GameSettings a, b, c, e, e2, f, g, a2, a3, c2, c3, f2, g2;
      a = new GameSettings(); a2 = new GameSettings(); a3 = new GameSettings(); b = new GameSettings(); c = new GameSettings(); c2 = new GameSettings(); c3 = new GameSettings();
      e = new GameSettings(); e2 = new GameSettings(); f = new GameSettings(); f2 = new GameSettings(); g = new GameSettings(); g2 = new GameSettings();
      
      a.set("game_name", "DM Test #1");
      a.set("map_name", "final");
      a.set("gametype", "Deathmatch");
      a.set("teams", "0");
      a.set("max_players", "3");
      a.set("score_to_win", "15");
      a.set("respawn_time", "30");
      
      a2.set("game_name", "DM Test #2");
      a2.set("map_name", "war");
      a2.set("gametype", "Deathmatch");
      a2.set("teams", "0");
      a2.set("max_players", "8");
      a2.set("score_to_win", "15");
      a2.set("respawn_time", "30");
      
      a3.set("game_name", "DM Test #3");
      a3.set("map_name", "combat");
      a3.set("gametype", "Deathmatch");
      a3.set("teams", "0");
      a3.set("max_players", "5");
      a3.set("score_to_win", "15");
      a3.set("respawn_time", "30");
      
      b.set("game_name", "TDM Test #1");
      b.set("map_name", "war");
      b.set("gametype", "TeamDeathmatch");
      b.set("teams", "2");
      b.set("max_players", "10");
      b.set("score_to_win", "30");
      b.set("respawn_time", "90");
      
      c.set("game_name", "CTF Test #1");
      c.set("map_name", "battle");
      c.set("gametype", "CaptureTheFlag");
      c.set("teams", "2");
      c.set("max_players", "12");
      c.set("score_to_win", "3");
      c.set("respawn_time", "90");
      
      c2.set("game_name", "CTF Test #2");
      c2.set("map_name", "war");
      c2.set("gametype", "CaptureTheFlag");
      c2.set("teams", "2");
      c2.set("max_players", "10");
      c2.set("score_to_win", "3");
      c2.set("respawn_time", "90");
      
      c3.set("game_name", "CTF Test #3");
      c3.set("map_name", "penultimate");
      c3.set("gametype", "CaptureTheFlag");
      c3.set("teams", "2");
      c3.set("max_players", "14");
      c3.set("score_to_win", "3");
      c3.set("respawn_time", "90");
      
      f.set("game_name", "K Test #1");
      f.set("map_name", "war");
      f.set("gametype", "King");
      f.set("teams", "0");
      f.set("max_players", "8");
      f.set("score_to_win", "25");
      f.set("static_hill", "0");
      f.set("score_to_move", "5");
      f.set("respawn_time", "90");
      
      f2.set("game_name", "K Test #2");
      f2.set("map_name", "combat");
      f2.set("gametype", "King");
      f2.set("teams", "0");
      f2.set("max_players", "5");
      f2.set("score_to_win", "25");
      f2.set("static_hill", "1");
      f2.set("respawn_time", "90");
      
      
      g.set("game_name", "UL Test #1");
      g.set("map_name", "final");
      g.set("gametype", "Ultimate");
      g.set("teams", "0");
      g.set("max_players", "4");
      g.set("score_to_win", "25");
      g.set("respawn_time", "90");
      
      g2.set("game_name", "UL Test #2");
      g2.set("map_name", "war");
      g2.set("gametype", "Ultimate");
      g2.set("teams", "0");
      g2.set("max_players", "8");
      g2.set("score_to_win", "25");
      g2.set("respawn_time", "90");
      
      e.set("game_name", "TK Test #1");
      e.set("map_name", "battle");
      e.set("gametype", "TeamKing");
      e.set("teams", "2");
      e.set("max_players", "12");
      e.set("score_to_win", "50");
      e.set("respawn_time", "90");
      
      e2.set("game_name", "TK Test #2");
      e2.set("map_name", "penultimate");
      e2.set("gametype", "TeamKing");
      e2.set("teams", "0");
      e2.set("max_players", "10");
      e2.set("score_to_win", "25");
      e2.set("static_hill", "1");
      e2.set("respawn_time", "90");
      
      
      lobbies.add(new OfficialLobby(a)); lobbies.add(new OfficialLobby(a2)); lobbies.add(new OfficialLobby(a3)); lobbies.add(new OfficialLobby(b)); lobbies.add(new OfficialLobby(c)); lobbies.add(new OfficialLobby(c2)); lobbies.add(new OfficialLobby(c3));
      lobbies.add(new OfficialLobby(f)); lobbies.add(new OfficialLobby(f2)); lobbies.add(new OfficialLobby(e)); lobbies.add(new OfficialLobby(e2)); lobbies.add(new OfficialLobby(g));
      
      for(int i=0;i<lobbies.size();i++) { lobbies.get(i).start(); }
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
