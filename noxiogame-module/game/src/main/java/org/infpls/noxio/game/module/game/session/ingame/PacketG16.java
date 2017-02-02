package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG16 extends Packet {
  private final String player;
  public PacketG16(final String player) {
    super("g16");
    this.player = player;
  }
  
  public String getPlayer() { return player; }
}
