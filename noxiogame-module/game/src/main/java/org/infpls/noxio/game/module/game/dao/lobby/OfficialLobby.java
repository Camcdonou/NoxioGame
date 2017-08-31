package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;
import org.infpls.noxio.game.module.game.session.NoxioSession;

public class OfficialLobby extends GameLobby {
  public OfficialLobby(final GameSettings settings) throws IOException {
    super(settings);
  }
  
  @Override
  protected void leave(NoxioSession player) throws IOException {
    if(!players.remove(player)) { return; } /* If the player attempting to leave is not in the game then don't bother with the rest of this. */
    while(loading.remove(player));
    game.leave(player);
    outDirect.remove(player);
    if(players.size() >= 1) { updatePlayerList(player.getUser() + " left the game."); }
  }
  
  @Override
  public void remove(NoxioSession player) throws IOException { /* Similar to leave but called from the destroy() of a session. */
    if(!players.remove(player)) { return; } /* If the player attempting to leave is not in the game then don't bother with the rest of this. */
    while(loading.remove(player));
    game.leave(player);
    outDirect.remove(player);
    if(players.size() >= 1) { updatePlayerList(player.getUser() + " disconnected."); }
  }
  
  @Override
  public NoxioSession getHost() { return null; }
  
  @Override
  public GameLobbyInfo getInfo() { return new GameLobbyInfo(lid, name, game.gametypeName(), "Official Server", players.size(), maxPlayers); }
}
