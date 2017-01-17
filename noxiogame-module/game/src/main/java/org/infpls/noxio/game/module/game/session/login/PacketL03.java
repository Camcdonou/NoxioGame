package org.infpls.noxio.game.module.game.session.login;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketL03 extends Packet {
  private final boolean result;
  private final String message;
  public PacketL03(final boolean result, final String message) {
    super("l03");
    this.result = result; this.message = message;
  }
  
  public boolean getResult() { return result; }
  public String getMessage() { return message; }
}
