package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG15 extends Packet {
  private final String message;
  public PacketG15(final String message) {
    super("g15");
    this.message = message;
  }
  
  public String getMessage() { return message; }
}
