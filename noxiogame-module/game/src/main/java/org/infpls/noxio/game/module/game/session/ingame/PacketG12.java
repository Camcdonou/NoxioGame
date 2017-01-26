package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.game.object.Vec2;
import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG12 extends Packet {
  private final long oid;
  private final Vec2 pos, vel;
  public PacketG12(final long oid, final Vec2 pos, final Vec2 vel) {
    super("g12");
    this.oid = oid;
    this.pos = pos; this.vel = vel;
  }
  
  public long getOid() { return oid; }
  public Vec2 getPos() { return pos; }
  public Vec2 getVel() { return vel; }
}
