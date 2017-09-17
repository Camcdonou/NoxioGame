package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketI00 extends Packet {
  private final String data;
  public PacketI00(final String data) {
    super("i00");
    this.data = data;
  }
  
  public String getData() { return data; }
}
