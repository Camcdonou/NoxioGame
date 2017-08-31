package org.infpls.noxio.game.module.game.game;

/* @TODO: Refactor to PlayerScore/GameScore as we should seperate indiviual stats from game objective state */

public class Score {
  private int kills, deaths, objectives;
  public Score() {
    kills = 0;
    deaths = 0;
    objectives = 0;
  }
  
  public void objective() { objectives++; }
  public void kill() { kills++; }
  public void death() { deaths++; }
  
  public int getObjectives() { return objectives; }
  public int getKills() { return kills; }
  public int getDeaths() { return deaths; }
}
