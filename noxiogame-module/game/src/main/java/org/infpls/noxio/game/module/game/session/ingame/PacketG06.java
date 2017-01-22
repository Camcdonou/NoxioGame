package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG06 extends Packet {
  private final String message;
  public PacketG06(final String message) {
    super("g06");
    this.message = message;
  }
  
  private String getMessage() { return message; }
}
