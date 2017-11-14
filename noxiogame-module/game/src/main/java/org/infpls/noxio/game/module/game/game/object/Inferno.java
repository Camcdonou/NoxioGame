package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Oak;

public class Inferno extends Player {
  private final static int TAUNT_COOLDOWN_LENGTH = 30;

  private int tauntCooldown;
  public Inferno(final NoxioGame game, final int oid, final Vec2 position) {
    this(game, oid, position, -1);
  }
  
  public Inferno(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, "obj.mobile.player.inferno", position, team);
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 0.0350f; jumpHeight = 0.250f;
    
    /* Timers */
    tauntCooldown = 0;
  }
  
  /* Performs action. */
  @Override
  public void actions() {
    for(int i=0;i<action.size();i++) {
      switch(action.get(i)) {
        case "tnt" : { taunt(); break; }
        case "jmp" : { jump(); break; }
        default : { Oak.log("Invalid action input::"  + action.get(i) + " @Inferno.actions", 1); break; }
      }
    }
    action.clear();
  }
  
  /* Updates various timers */
  @Override
  public void timers() { 
    if(tauntCooldown > 0) { tauntCooldown--; }
    if(stunTimer > 0) { stunTimer--; }
  }
  
  public void taunt() {
    if(tauntCooldown <= 0) {
      tauntCooldown = TAUNT_COOLDOWN_LENGTH;
      effects.add("tnt");
    }
  }
}
