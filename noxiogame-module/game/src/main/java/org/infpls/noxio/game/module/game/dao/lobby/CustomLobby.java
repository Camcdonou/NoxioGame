package org.infpls.noxio.game.module.game.dao.lobby;

import java.util.*;
import java.io.IOException;

import org.infpls.noxio.game.module.game.session.NoxioSession;
import org.infpls.noxio.game.module.game.session.ingame.PacketG15;

public class CustomLobby extends GameLobby {
  protected NoxioSession hostPlayer;
  public CustomLobby(final String name) throws IOException {
    super(name);
    hostPlayer = null;
  }
  
  @Override
  protected boolean connect(NoxioSession player) throws IOException {
    if(closed) { return false; }
    if(players.size() >= maxPlayers) { return false; }
    if(players.contains(player)) { player.close("Lobby Doppleganger Error."); return false; }
    players.add(player);
    loading.add(player);
    outDirect.put(player, new ArrayList());
    if(hostPlayer == null) { hostPlayer = player; } /* The very first person to connect to a custom match is always the host. */
    updatePlayerList(player.getUser() + " connected.");
    return true;
  }
  
  @Override
  protected void leave(NoxioSession player) throws IOException {
    if(!players.remove(player)) { return; } /* If the player attempting to leave is not in the game then don't bother with the rest of this. */
    while(loading.remove(player));
    game.leave(player);
    outDirect.remove(player);
    if(players.size() < 1) { close(); return; }
    if(players.size() >= 1) { updatePlayerList(player.getUser() + " left the game."); }
    if(player == hostPlayer) { hostPlayer = players.get(0); sendPacket(new PacketG15(hostPlayer.getUser() + " is now the lobby host.")); }
  }
  
  @Override
  public void remove(NoxioSession player) throws IOException { /* Similar to leave but called from the destroy() of a session. */
    if(!players.remove(player)) { return; } /* If the player attempting to leave is not in the game then don't bother with the rest of this. */
    while(loading.remove(player));
    game.leave(player);
    outDirect.remove(player);
    if(players.size() < 1) { close(); return; }
    if(players.size() >= 1) { updatePlayerList(player.getUser() + " disconnected."); }
    if(player == hostPlayer) { hostPlayer = players.get(0); sendPacket(new PacketG15(hostPlayer.getUser() + " is now the lobby host.")); }
  }
    
  @Override
  public NoxioSession getHost() { return null; }
  
  @Override
  public GameLobbyInfo getInfo() { return new GameLobbyInfo(lid, name, "STUB", hostPlayer == null ? "N/A" : hostPlayer.getUser(), players.size(), maxPlayers); }
}
