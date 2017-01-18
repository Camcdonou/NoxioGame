package org.infpls.noxio.game.module.game.session.lobby;

import java.util.List;

import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.dao.lobby.GameLobbyInfo;

public class PacketB01 extends Packet {
  private final List<GameLobbyInfo> lobbies;
  public PacketB01(List<GameLobbyInfo> lobbies) {
    super("b01");
    this.lobbies = lobbies;
  }
  
  public List<GameLobbyInfo> getLobbies() { return lobbies; }
}
