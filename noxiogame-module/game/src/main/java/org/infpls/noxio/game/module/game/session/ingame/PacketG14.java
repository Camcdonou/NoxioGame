package org.infpls.noxio.game.module.game.session.ingame;

import java.util.List;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.game.Score;

public class PacketG14 extends Packet {
  private final Score score;
  public PacketG14(final Score score) {
    super("g14");
    this.score = score;
  }
  
  public Score getScore() { return score; }
}
