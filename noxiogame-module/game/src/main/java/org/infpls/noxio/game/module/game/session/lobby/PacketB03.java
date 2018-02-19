package org.infpls.noxio.game.module.game.session.lobby;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketB03 extends Packet {
  private final String settings;
  public PacketB03(final String settings) {
    super("b03");
    this.settings = settings;
  }
  
  public String getSettings() { return settings; }
}
