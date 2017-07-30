package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG16 extends Packet {
  private final String message;
  public PacketG16(final String message) {
    super("g16");
    this.message = message;
  }
  
  public String getMessage() { return message; }
}