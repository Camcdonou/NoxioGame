package org.infpls.noxio.game.module.game.session.login;

import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.util.Settable;

public class PacketL01 extends Packet {
  private final Settable.ServerInfo info;
  public PacketL01(final Settable.ServerInfo info) {
    super("l01");
    this.info = info;
  }
  
  public Settable.ServerInfo getServerInfo() { return info; }
}
