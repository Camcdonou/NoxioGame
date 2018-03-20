package org.infpls.noxio.game.module.game.session;

public class PacketH09 extends Packet {
  
  private final String message;
  public PacketH09(final String message) {
    super("h09");
    this.message = message;
  }
  
  public String getMessage() { return message; }
}
