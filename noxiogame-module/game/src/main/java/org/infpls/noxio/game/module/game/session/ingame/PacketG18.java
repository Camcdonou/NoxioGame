package org.infpls.noxio.game.module.game.session.ingame;

import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG18 extends Packet {
  private final int score;
  public PacketG18(final int score) {
    super("g18");
    this.score = score;
  }
  
  public int getScore() { return score; }
}
