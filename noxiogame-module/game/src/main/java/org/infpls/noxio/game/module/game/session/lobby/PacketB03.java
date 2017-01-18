package org.infpls.noxio.game.module.game.session.lobby;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketB03 extends Packet {
  private final String name;
  public PacketB03(final String name) {
    super("b03");
    this.name = name;
  }
  
  public String getName() { return name; }
}
