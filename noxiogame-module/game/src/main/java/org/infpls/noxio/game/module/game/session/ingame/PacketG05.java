package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG05 extends Packet {
  private final long t;
  public PacketG05(final long t) {
    super("g05");
    this.t = t;
  }
  
  public long getT() { return t; }
}
