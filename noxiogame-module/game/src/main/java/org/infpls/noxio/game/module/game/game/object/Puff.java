package org.infpls.noxio.game.module.game.game.object; 

import org.infpls.noxio.game.module.game.game.*;
import org.infpls.noxio.game.module.game.util.Oak;

public class Puff extends Player {
  private static final int REST_COOLDOWN_LENGTH = 30, POUND_COOLDOWN_LENGTH = 30;
  private static final int TAUNT_COOLDOWN_LENGTH = 30;
  
  private int restCooldown, poundCooldown, tauntCooldown;
  public Puff(final NoxioGame game, final int oid, final Vec2 position) {
    this(game, oid, position, -1);
  }
  
  public Puff(final NoxioGame game, final int oid, final Vec2 position, final int team) {
    super(game, oid, "obj.mobile.player.puff", position, team);
    
    /* Settings */
    radius = 0.5f; weight = 1.0f; friction = 0.725f;
    moveSpeed = 0.0375f; jumpHeight = 0.175f;
    
    /* Timers */
    restCooldown = 0;
    poundCooldown = 0;
    tauntCooldown = 0;
  }
  
  /* Performs action. */
  @Override
  public void actions() {
    for(int i=0;i<action.size();i++) {
      switch(action.get(i)) {
        case "atk" : { rest(); break; }
        case "mov" : { pound(); break; }
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
    if(restCooldown > 0) { restCooldown--; }
    if(poundCooldown > 0) { poundCooldown--; }
    if(stunTimer > 0) { stunTimer--; }
    if(tauntCooldown > 0) { tauntCooldown--; }
  }

  public void rest() {
    effects.add("atk");
//    if(blipCooldown <= 0) {
//      blipCooldown = BLIP_COOLDOWN_LENGTH;
//      if(getHeight() > -0.5) {
//        for(int i=0;i<game.objects.size();i++) {
//          GameObject obj = game.objects.get(i);
//          if(obj != this && obj.getType().startsWith("obj.mobile")) {
//            final Mobile mob = (Mobile)obj;
//            if(mob.getPosition().distance(position) < mob.getRadius() + getRadius() + BLIP_OUTER_RADIUS && mob.getHeight() > -0.5) {
//              if(obj.getType().startsWith("obj.mobile.player")) {
//                final Player ply = (Player)obj;
//                ply.stun(BLIP_STUN_TIME*(blipPower/BLIP_POWER_MAX));
//              }
//              final Vec2 normal = mob.getPosition().subtract(position).normalize();
//              mob.knockback(normal.scale(BLIP_IMPULSE*(((blipPower/BLIP_POWER_MAX)*0.5f)+0.5f)), this);
//              final Controller c = game.getControllerByObject(this);
//              if(c != null) { mob.tag(c); }
//            }
//          }
//        }
//      }
//      blipPower = 0;
//     
//    }
  }
  
  public void pound() {
    effects.add("mov");
//    if(dashCooldown <= 0 && dashPower < DASH_POWER_MAX) {
//      drop();
//      dashCooldown = DASH_COOLDOWN_LENGTH;
//      dashPower += DASH_POWER_ADD;
//      setVelocity(velocity.add(look.scale(DASH_IMPULSE)));
//      
//      if(dashPower >= DASH_POWER_MAX) { stun(DASH_STUN_TIME); }
//    }
  }
  
  public void taunt() {
    if(tauntCooldown <= 0) {
      tauntCooldown = TAUNT_COOLDOWN_LENGTH;
      effects.add("tnt");
    }
  }
}
