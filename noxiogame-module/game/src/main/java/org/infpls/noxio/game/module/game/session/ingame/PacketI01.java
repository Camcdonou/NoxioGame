package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.game.object.Vec2;
import org.infpls.noxio.game.module.game.session.Packet;

public class PacketI01 extends Packet {
  private final Vec2 pos;
  public PacketI01(final Vec2 pos) {
    super("i01");
    this.pos = pos;
  }
  
  public Vec2 getPos() { return pos; }
}
