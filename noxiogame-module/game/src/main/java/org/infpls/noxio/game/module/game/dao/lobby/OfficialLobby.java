package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class OfficialLobby extends GameLobby {
  public OfficialLobby(final LobbySettings settings) throws IOException {
    super(settings);
  }
  
  @Override
  public NoxioSession getHost() { return null; }
  
  @Override
  public GameLobbyInfo getInfo() { return new GameLobbyInfo(lid, name, game.gametypeName(), "Official Server", players.size(), maxPlayers); }
}
