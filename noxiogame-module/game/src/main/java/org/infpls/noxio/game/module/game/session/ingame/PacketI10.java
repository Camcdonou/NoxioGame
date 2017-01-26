package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.game.object.Vec2;
import org.infpls.noxio.game.module.game.session.Packet;

public class PacketI10 extends Packet {
  private final Vec2 pos;
  private final String action;
  public PacketI10(final Vec2 pos, final String action) {
    super("i10");
    this.pos = pos;
    this.action = action;
  }
  
  public Vec2 getPos() { return pos; }
  public String getAction() { return action; }
}
