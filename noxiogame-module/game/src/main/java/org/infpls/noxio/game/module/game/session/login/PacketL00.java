package org.infpls.noxio.game.module.game.session.login;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketL00 extends Packet {
  private final String user, sid;
  public PacketL00(final String user, final String sid) {
    super("l00");
    this.user = user; this.sid = sid;
  }
  
  public String getUser() { return user; }
  public String getSid() { return sid; }
}
