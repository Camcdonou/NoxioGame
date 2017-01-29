package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG13 extends Packet {
  private final long oid;
  public PacketG13(final long oid) {
    super("g13");
    this.oid = oid;
  }
  
  public long getOid() { return oid; }
}
