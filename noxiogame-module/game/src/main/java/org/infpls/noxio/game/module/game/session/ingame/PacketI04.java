package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.game.object.Vec2;
import org.infpls.noxio.game.module.game.session.Packet;

public class PacketI04 extends Packet {
  private final Vec2 pos;
  private final float speed;
  public PacketI04(final Vec2 pos, final float speed) {
    super("i04");
    this.pos = pos;
    this.speed = speed;
  }
  
  public Vec2 getPos() { return pos; }
  public float getSpeed() { return speed; }
}
