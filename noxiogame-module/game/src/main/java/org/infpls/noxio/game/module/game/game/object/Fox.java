package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Oak;

public class Fox extends Player {
  private static final int BLIP_COOLDOWN_LENGTH = 10, BLIP_POWER_MAX = 30, BLIP_STUN_TIME = 30;
  private static final int DASH_COOLDOWN_LENGTH = 15, DASH_POWER_MAX = 60, DASH_POWER_ADD = 30, DASH_STUN_TIME = 30;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  private static final float BLIP_IMPULSE = 0.85f, DASH_IMPULSE = 0.25f, BLIP_OUTER_RADIUS = 0.1f;
  
  private int blipCooldown, dashCooldown, tauntCooldown, blipPower, dashPower;
  public Fox(final NoxioGame game, final int oid, final Vec2 position) {
    super(game, oid, "obj.mobile.player.fox", position, -1);
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* Timers */
    blipCooldown = 0;
    dashCooldown = 0;
    tauntCooldown = 0;
    blipPower = BLIP_POWER_MAX;
    dashPower = 0;
  }
  
  public Fox(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, "obj.mobile.player.fox", position, team);
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 1.0f; jumpHeight = 1.0f;
    
    /* Timers */
    blipCooldown = 0;
    dashCooldown = 0;
    tauntCooldown = 0;
    blipPower = BLIP_POWER_MAX;
    dashPower = 0;
  }
  
  /* Performs action. */
  @Override
  public void actions() {
    for(int i=0;i<action.size();i++) {
      switch(action.get(i)) {
        case "atk" : { blip(); break; }
        case "mov" : { dash(); break; }
        case "tnt" : { taunt(); break; }
        case "jmp" : { jump(); break; }
        default : { Oak.log("Invalid action input::"  + action.get(i) + " @Fox.actions", 1); break; }
      }
    }
    action.clear();
  }
  
  /* Updates various timers */
  @Override
  public void timers() { 
    if(blipCooldown > 0) { blipCooldown--; }
    if(dashCooldown > 0) { dashCooldown--; }
    if(tauntCooldown > 0) { tauntCooldown--; }
    if(blipPower < BLIP_POWER_MAX) { blipPower++; }
    if(dashPower > 0) { dashPower--; }
    if(stunTimer > 0) { stunTimer--; }
  }

  public void blip() {
    if(blipCooldown <= 0) {
      blipCooldown = BLIP_COOLDOWN_LENGTH;
      if(getHeight() > -0.5) {
        for(int i=0;i<game.objects.size();i++) {
          GameObject obj = game.objects.get(i);
          if(obj != this && obj.getType().startsWith("obj.mobile")) {
            final Mobile mob = (Mobile)obj;
            if(mob.getPosition().distance(position) < mob.getRadius() + getRadius() + BLIP_OUTER_RADIUS && mob.getHeight() > -0.5) {
              if(obj.getType().startsWith("obj.mobile.player")) {
                final Player ply = (Player)obj;
                ply.stun(BLIP_STUN_TIME*(blipPower/BLIP_POWER_MAX));
              }
              final Vec2 normal = mob.getPosition().subtract(position).normalize();
              mob.knockback(normal.scale(BLIP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f)), this);
              final Controller c = game.getControllerByObject(this);
              if(c != null) { mob.tag(c); }
            }
          }
        }
      }
      blipPower = 0;
      effects.add("atk");
    }
  }
  
  public void dash() {
    if(dashCooldown <= 0 && dashPower < DASH_POWER_MAX) {
      drop();
      dashCooldown = DASH_COOLDOWN_LENGTH;
      dashPower += DASH_POWER_ADD;
      setVelocity(velocity.add(look.scale(DASH_IMPULSE)));
      effects.add("mov");
      if(dashPower >= DASH_POWER_MAX) { stun(DASH_STUN_TIME); }
    }
  }
  
  public void taunt() {
    if(tauntCooldown <= 0) {
      tauntCooldown = TAUNT_COOLDOWN_LENGTH;
      effects.add("tnt");
    }
  }
}
