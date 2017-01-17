package org.infpls.noxio.game.module.game.session.error;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketX00 extends Packet {
  private final String message;
  public PacketX00(final String message) {
    super("x00");
    this.message = message;
  }
  
  public String getMessage() { return message; }
}
