package org.infpls.noxio.game.module.game.session.lobby;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketB06 extends Packet {
  private final String lobbyName;
  public PacketB06(final String lobbyName) {
    super("b06");
    this.lobbyName = lobbyName;
  }
  
  public String getLobbyName() { return lobbyName; }
}
