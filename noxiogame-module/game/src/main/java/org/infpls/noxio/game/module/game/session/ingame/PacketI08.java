package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketI08 extends Packet {
  private final int timer;
  public PacketI08(final int timer) {
    super("i08");
    this.timer = timer;
  }
  
  public int getTimer() { return timer; }
}
