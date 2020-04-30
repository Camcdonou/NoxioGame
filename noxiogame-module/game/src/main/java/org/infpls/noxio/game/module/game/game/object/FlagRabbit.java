package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Kalide;

public class FlagRabbit extends Flag {
  private static final int SCORE_TIME_ADJUST = 90;
  private static final int RABBIT_RESET_COOLDOWN_TIME = 450;
  
  private int scoreTimer;
  public FlagRabbit(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, position, team);
    
    /* Settings */
    color = Kalide.compressColors(2, 4, 5, 6, 8);
    
    /* Timers */
    scoreTimer = 0;
    
    /* Settings */
    teamAttack = true; enemyAttack = true;
  }
  
  @Override
  public void step() {    
    if(isHeld()) {
      if(scoreTimer++ > SCORE_TIME_ADJUST) {
        scoreTimer = 0;
        score(held);
      }
    }
    
    super.step();
  }
  
  @Override
  public boolean touch(Player p) {
    if(isHeld()) { return false; }
    return pickup(p);
  }
  
  @Override
  protected boolean pickup(Player p) {
    if(super.pickup(p)) {
      if(onBase()) { announceTaken(0); }
      return true;
    }
    return false;
  }
  
  @Override
  public void dropped() {
    super.dropped();
    scoreTimer = 0;
  }
  
  @Override
  protected void reset() {
    dead = true;
    destroyed = true;
  }
  
  @Override
  public void kill() {
    dead = true;
    destroyed = true;
    if(!onBase()) { announceReset(); }
  }
  
  @Override
  public void announceTaken(int team) {
    game.announce("ft");
  }
  
  @Override
  public void announceReset() {
    game.announce("ff");
  }
  
  @Override
  public void announceReturn() {
    game.announce("ff");
  }
}
