package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;

public class Player extends Mobile {
  private static final float SPEED = 0.025f;
  
  private int spawnProtection;
  public Player(final NoxioGame game, final long oid, final Vec2 position) {
    super(game, oid, "obj.mobile.player", position, 0.5f, 1.0f, 0.88f);
    spawnProtection = 66;
  }
  
  public void move(final Vec2 direction) {
    setVelocity(velocity.add(direction.scale(SPEED)));
  }
  
  @Override
  public void step() {
    physics();
    
    /* Timers */
    if(spawnProtection > 0) { spawnProtection--; }
  }
   
  @Override
  public void generateUpdateData(final StringBuilder sb) {
    sb.append("obj"); sb.append(";");
    sb.append(oid); sb.append(";");
    position.toString(sb); sb.append(";");
    velocity.toString(sb); sb.append(";");
  }
  
  @Override
  public void kill(GameObject killer) {
    if(spawnProtection < 1) { dead = true; }
  }
  
  @Override
  public void kill() { dead = true; }
}
