package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketI03 extends Packet {
  private final long oid;
  public PacketI03(final long oid) {
    super("i03");
    this.oid = oid;
  }
  
  public long getOid() { return oid; }
}
