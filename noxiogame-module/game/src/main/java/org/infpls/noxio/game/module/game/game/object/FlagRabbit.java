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
  }
  
  @Override
  public void step() {
    super.step();
    
    if(isHeld()) {
      if(scoreTimer++ > SCORE_TIME_ADJUST) {
        scoreTimer = 0;
        score(held);
      }
    }
    
    if(!isHeld() && !onBase()) { resetCooldown++; }
    else { resetCooldown = 0; }
    if(resetCooldown >= RESET_COOLDOWN_TIME) { kill(); }
  }
  
  @Override
  public boolean touch(Player p) {
    if(isHeld()) { return false; }
    return pickup(p);
  }
  
  @Override
  protected boolean pickup(Player p) {
    if(p.getHolding() != null) { return false; }
    if(dropCooldown > 0 && lastHeld == p.getOid()) { return false; }
    held = p;
    lastHeld = held.getOid();
    if(onBase()) {
      game.announce("ft");
    }
    setVelocity(new Vec2());
    setHeight(0f);
    setVSpeed(0f);
    return true;
  }
  
  @Override
  public void dropped() {
    super.dropped();
    scoreTimer = 0;
  }
  
  @Override
  public void kill() {
    dead = true;
    destroyed = true;
    if(!onBase()) { game.announce("ff"); }
  }
}
