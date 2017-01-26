package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG05 extends Packet {
  private final long tick, sent;
  
  public PacketG05(final long tick, final long sent) {
    super("g05");
    this.tick = tick;
    this.sent = sent;
  }
  
  public long getTick() { return tick; }
  public long getSent() { return sent; }
}
