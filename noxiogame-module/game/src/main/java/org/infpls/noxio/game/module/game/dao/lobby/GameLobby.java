package org.infpls.noxio.game.module.game.dao.lobby;

import java.util.*;

import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.util.Salt;

public class GameLobby {
  private final String lid; //Lobby ID
  
  private final String name;
  private final boolean autoClose; //Automatically close this lobby if it's empty.
  
  private final List<NoxioSession> players;
  public GameLobby(final String name, final boolean autoClose) {
    lid = Salt.generate();
    this.name = name;
    this.autoClose = autoClose;
    players = new ArrayList();
  }
  
  public boolean join(NoxioSession player) {
    players.add(player);
    return true;
  }
  
  public void leave(NoxioSession player) {
    players.remove(player);
  }
  
  public String getLid() { return lid; }
  public GameLobbyInfo getInfo() { return new GameLobbyInfo(lid, name, "STUB", "STUB", 0, 0); }
}
