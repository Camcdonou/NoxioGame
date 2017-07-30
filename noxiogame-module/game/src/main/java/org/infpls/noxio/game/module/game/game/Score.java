package org.infpls.noxio.game.module.game.game;

/* @TODO: Refactor to PlayerScore/GameScore as we should seperate indiviual stats from game objective state */

public class Score {
  private final String user;
  private int kills, deaths;
  public Score(final String user) {
    this.user = user;
    kills = 0;
    deaths = 0;
  }
  
  public void kill() { kills++; }
  public void death() { deaths++; }
  
  public int getKills() { return kills; }
  public int getDeaths() { return deaths; }
}
