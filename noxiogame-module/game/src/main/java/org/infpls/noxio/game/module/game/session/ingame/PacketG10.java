package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG10 extends Packet {
  private final String data;
  public PacketG10(final String data) {
    super("g10");
    this.data = data;
  }

  public String getData() { return data; }
}
