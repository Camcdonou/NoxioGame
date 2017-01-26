package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.game.object.Vec2;
import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG10 extends Packet {
  private final long oid;
  private final String otype;
  private final Vec2 pos, vel;
  public PacketG10(final long oid, final String otype, final Vec2 pos, final Vec2 vel) {
    super("g10");
    this.oid = oid;
    this.otype = otype;
    this.pos = pos; this.vel = vel;
  }
  
  public long getOid() { return oid; }
  public String getObjectType() { return otype; }
  public Vec2 getPos() { return pos; }
  public Vec2 getVel() { return vel; }
}
