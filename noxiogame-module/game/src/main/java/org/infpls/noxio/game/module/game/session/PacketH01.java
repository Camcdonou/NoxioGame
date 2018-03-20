package org.infpls.noxio.game.module.game.session;

import org.infpls.noxio.game.module.game.game.Score;

public class PacketH01 extends Packet {
  
  private final String user;
  private final Score.Stats stats;
  public PacketH01(final String user, final Score.Stats stats) {
    super("h01");
    this.user = user; this.stats = stats;
  }
  
  public String getUser() { return user; }
  public Score.Stats getStats() { return stats; }
}
