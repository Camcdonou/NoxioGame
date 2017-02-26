package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.game.object.Vec2;
import org.infpls.noxio.game.module.game.session.Packet;

public class PacketI04 extends Packet {
  private final Vec2 pos;
  public PacketI04(final float x, final float y) {
    super("i04");
    this.pos = new Vec2(x, y);
  }
  
  public Vec2 getPos() { return pos; }
}
