package org.infpls.noxio.game.module.game.dao.lobby;

import java.io.IOException;

import org.infpls.noxio.game.module.game.session.NoxioSession;

public class CustomLobby extends GameLobby {
  protected NoxioSession hostPlayer;
  public CustomLobby(final LobbySettings settings) throws IOException {
    super(settings);
    hostPlayer = null;
  }
  
  @Override
  protected boolean connect(NoxioSession player) throws IOException {
    boolean res = super.connect(player);
    if(res) { if(hostPlayer == null) { hostPlayer = player; } } /* The very first person to connect to a custom match is always the host. */
    return res;
  }
  
  @Override
  protected void leave(NoxioSession player) throws IOException {
    super.leave(player);
    if(players.size() < 1) { close(); return; }
    if(player == hostPlayer) { hostPlayer = players.get(0); game.sendMessage(hostPlayer.getUser() + " is now the lobby host."); }
  }
  
  @Override
  public void remove(NoxioSession player) throws IOException {
    super.remove(player);
    if(players.size() < 1) { close(); return; }
    if(player == hostPlayer) { hostPlayer = players.get(0); game.sendMessage(hostPlayer.getUser() + " is now the lobby host."); }
  }
    
  @Override
  public NoxioSession getHost() { return hostPlayer; }
  
  @Override
  public GameLobbyInfo getInfo() { return new GameLobbyInfo(lid, name, game.gametypeName(), hostPlayer == null ? "N/A" : getHost().getUser(), players.size(), maxPlayers); }
}
