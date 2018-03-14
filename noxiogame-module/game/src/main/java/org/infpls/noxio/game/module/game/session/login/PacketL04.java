package org.infpls.noxio.game.module.game.session.login;

import org.infpls.noxio.game.module.game.dao.user.UserData;
import org.infpls.noxio.game.module.game.session.Packet;

public class PacketL04 extends Packet {
  private final UserData data;
  public PacketL04(final UserData data) {
    super("l04");
    this.data = data;
  }
  
  public UserData getData() { return data; }
}
