package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG11 extends Packet {
  private final long oid;
  public PacketG11(final long oid) {
    super("g11");
    this.oid = oid;
  }
  
  public long getOid() { return oid; }
}
