package org.infpls.noxio.game.module.game.session.login;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketL03 extends Packet {
  private final String message;
  public PacketL03(final String message) {
    super("l03");
    this.message = message;
  }
  
  public String getMessage() { return message; }
}
