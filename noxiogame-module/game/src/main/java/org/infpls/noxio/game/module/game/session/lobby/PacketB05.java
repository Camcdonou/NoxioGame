package org.infpls.noxio.game.module.game.session.lobby;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketB05 extends Packet {
  private final String message;
  public PacketB05(final String message) {
    super("b05");
    this.message = message;
  }
  
  public String getMessage() { return message; }
}
