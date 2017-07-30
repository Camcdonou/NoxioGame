package org.infpls.noxio.game.module.game.session.ingame;

import java.util.List;
import org.infpls.noxio.game.module.game.game.Score;
import org.infpls.noxio.game.module.game.session.Packet;

public class PacketG14 extends Packet {
  private final String gametype, description;
  private final List<Score> scores;
  private final int scoreToWin;
  public PacketG14(final String gametype, final String description, final List<Score> scores, final int scoreToWin) {
    super("g14");
    this.gametype = gametype; this.description = description;
    this.scores = scores;
    this.scoreToWin = scoreToWin;
  }
  
  public String getGametype() { return gametype; }
  public String getDescription() { return description; }
  public List<Score> getScores() { return scores; }
  public int getScoreToWin() { return scoreToWin; }
}