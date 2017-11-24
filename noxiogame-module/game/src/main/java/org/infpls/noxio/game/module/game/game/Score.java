package org.infpls.noxio.game.module.game.game;

/* @TODO: Refactor to PlayerScore/GameScore as we should seperate indiviual stats from game objective state */

public class Score {
  private int kills, deaths, objectives, spree;
  private int multi, timer;
  private static final int MULTI_TIMER_LENGTH = 90;
  public Score() {
    kills = 0;
    deaths = 0;
    objectives = 0;
    spree = 0;
    
    multi = 0; timer = 0;
  }
  
  public void objective() { objectives++; }
  public void kill(int frame) {
    kills++; spree++; 
    if(frame - timer <= MULTI_TIMER_LENGTH) { multi++; }
    else { multi = 1; }
    timer = frame;
  }
  public int death() { deaths++; int s = spree; spree = 0; return s;}
  
  public int getMulti() { return multi; }
  public int getSpree() { return spree; }
  
  public int getObjectives() { return objectives; }
  public int getKills() { return kills; }
  public int getDeaths() { return deaths; }
}
