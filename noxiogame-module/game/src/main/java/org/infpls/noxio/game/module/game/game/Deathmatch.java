package org.infpls.noxio.game.module.game.game;

import java.io.IOException;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobby;

public class Deathmatch extends NoxioGame {
  public Deathmatch(final GameLobby lobby, final String mapName) throws IOException {
    super(lobby, mapName);
  }
}
