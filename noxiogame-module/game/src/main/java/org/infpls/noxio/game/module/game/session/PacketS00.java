package org.infpls.noxio.game.module.game.session;

public class PacketS00 extends Packet {
  
  private final char state;
  public PacketS00(final char state) {
    super("s00");
    this.state = state;
  }
  
  public char getState() { return state; }
}
