package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketI05 extends Packet {
  private final String ability;
  public PacketI05(final String ability) {
    super("i05");
    this.ability = ability;
  }

  public String getAbility() { return ability; }
}
