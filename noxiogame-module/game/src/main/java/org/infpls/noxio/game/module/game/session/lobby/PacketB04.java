package org.infpls.noxio.game.module.game.session.lobby;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketB04 extends Packet {
  private final String lid;
  public PacketB04(final String lid) {
    super("b04");
    this.lid = lid;
  }
  
  public String getLid() { return lid; }
}
