package org.infpls.noxio.game.module.game.session.ingame;

import java.util.List;
import org.infpls.noxio.game.module.game.session.Packet;
import org.infpls.noxio.game.module.game.game.NoxioGame;

public class PacketG14 extends Packet {
  private final String gametype, description;
  private final List<NoxioGame.ScoreBoard> scores;
  public PacketG14(final String gametype, final String description, final List<NoxioGame.ScoreBoard> scores) {
    super("g14");
    this.gametype = gametype; this.description = description;
    this.scores = scores;
  }
  
  public String getGametype() { return gametype; }
  public String getDescription() { return description; }
  public List<NoxioGame.ScoreBoard> getScores() { return scores; }
}